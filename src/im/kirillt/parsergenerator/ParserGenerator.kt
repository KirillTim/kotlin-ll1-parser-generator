package im.kirillt.parsergenerator

import java.io.File
import java.util.*

import im.kirillt.parsergenerator.GeneratorUtils.Indenter
import im.kirillt.parsergenerator.GeneratorUtils.contextClassName
import im.kirillt.parsergenerator.base.BaseRuleContext

object ParserGenerator {
    open class RuleItem(val name: String) {
        override fun equals(other: Any?) = other is RuleItem && toString().equals(other.toString())

        override fun hashCode() = toString().hashCode()

        override fun toString() = "RuleItem($name)"
    }

    class Terminal(n: String) : RuleItem(n) {
        override fun toString() = "Terminal(${super.name})"
    }

    class NonTerminal(n: String) : RuleItem(n) {
        override fun toString() = "NonTerminal(${super.name})"
    }

    data class Variable(val name: String, val type: String)

    data class RuleParams(val argument: Variable? = null, val returns: Variable? = null)

    val rules = mutableMapOf<NonTerminal, MutableList<Derivation>>()
    val ruleParams = mutableMapOf<String, RuleParams>()

    class Derivation(val from: NonTerminal, val children: List<RuleItem>, val sourceCode: String = "") {
        fun generateMethod(number: Int): String {
            val result = StringBuilder()
            val indenter = Indenter(result)
            indenter.writeln("fun ${from.name}_$number() {")
            indenter.indented {
                indenter.writeln("var __text = \"\"")
                for ((index, child) in children.withIndex()) {
                    if (child is NonTerminal) {
                        val typeName = contextClassName(child.name)
                        val varName = "__"+child.name+"_"+index
                        val funCall = child.name+"()"
                        indenter.writeln("$typeName $varName = $funCall")
                        indenter.writeln("__text += $varName.text")
                    } else if (child is Terminal) {
                        indenter.writeln("if (tokenizer.curToken().name != \"${child.name}\" {")
                        indenter.indented {
                            indenter.writeln("throw Exception(\"expected token '${child.name}', but get 'tokenizer.curToken().name'\")")
                        }
                        indenter.writeln("}")
                        indenter.writeln("__text += tokenizer.curToken().text")
                        indenter.writeln("tokenizer.nextToken()")
                    }
                }
                indenter.writeln(sourceCode)
                indenter.writeln("return ${contextClassName(from.name)}(text = __text)")
            }
            indenter.writeln("}")
            return result.toString()
        }

        fun first(): List<Terminal> {
            val first = children.first()
            if (first is Terminal)
                return listOf(first);
            else
                return rules[from]!!.flatMap { it.first() }
        }
    }


    val START = NonTerminal("start")
    val EOF = Terminal("$")
    val EPS = Terminal("")
    @JvmField
    var grammarName = ""
    val tokens = mutableMapOf<String, String>()
    private var unnamedTokensCount = 0
    val nonTerminals = mutableSetOf<NonTerminal>()
    val FIRST = mutableMapOf<NonTerminal, MutableSet<Terminal>>()
    val FOLLOW = mutableMapOf<NonTerminal, MutableSet<Terminal>>()

    @JvmStatic
    fun addToken(string: String, name: String = ""): String {
        var fixed = string
        if (fixed.startsWith("'"))
            fixed = fixed.drop(1)
        if (fixed.endsWith("'"))
            fixed = fixed.dropLast(1)
        if (name.isEmpty()) {
            for (i in tokens) {
                if (i.value.equals(fixed))
                    return i.key
            }
            unnamedTokensCount++
            val key = "_T_" + unnamedTokensCount;
            tokens.put(key, fixed)
            return key
        }
        if (tokens.containsKey(name))
            throw Exception("already have token with name '$name'")

        tokens.put(name, fixed)
        return name
    }

    @JvmStatic
            //add eps rule if left is empty
    fun addRule(nonTermName: String, left: ArrayList<RuleItem>, action: String) {
        val from = NonTerminal(nonTermName)
        nonTerminals.add(from)
        val children = when (left.isEmpty()) {
            true -> listOf(EPS)
            false -> left.toList()
        }
        rules.getOrPut(from, { mutableListOf<Derivation>() }).add(Derivation(from, children, action))
    }

    private fun clean() {
        if (!nonTerminals.contains(START))
            throw Exception("Can't find '$START' terminal")
        val reachable = reachable()
        for (i in rules.keys) {
            if (!reachable.contains(i))
                rules.remove(i)
        }
    }

    fun constructFirstAndFollow() {
        fun first(alpha: RuleItem) = when (alpha) {
            is NonTerminal -> FIRST[alpha]!!
            is Terminal -> mutableSetOf(alpha)
            else -> throw Exception("WTF?")
        }

        rules.keys.forEach { FIRST[it] = mutableSetOf() }
        var changed = true
        while (changed) {
            val oldSize = FIRST.map { it.value.size }.sum()
            rules.forEach {
                for (alternative in it.value) {
                    val add = first(alternative.children.first())
                    FIRST[it.key]!! += add
                }
            }
            changed = FIRST.map { it.value.size }.sum() != oldSize
        }

        rules.keys.forEach { FOLLOW[it] = mutableSetOf() }
        FOLLOW[START] = mutableSetOf(EOF)
        changed = true
        while (changed) {
            val oldSize = FOLLOW.map { it.value.size }.sum()
            rules.forEach {
                val A = it.key
                for (alternative in it.value) {
                    for (i in alternative.children.indices) {
                        val B = alternative.children[i]
                        if (B !is NonTerminal)
                            continue
                        if (i == alternative.children.size - 1)
                            FOLLOW[B]!! += FOLLOW[A]!!
                        else {
                            val gamma = alternative.children[i + 1]
                            FOLLOW[B]!! += first(gamma) - EPS
                            if (first(gamma).contains(EPS))
                                FOLLOW[B]!! += FOLLOW[A]!!
                        }
                    }
                }
            }
            changed = FOLLOW.map { it.value.size }.sum() != oldSize
        }
    }

    fun generateTokenizer() {
        TokenizerGenerator.generate(grammarName, tokens)
    }

    fun generateContextClasses(ruleAttrs: Map<String, RuleParams>): List<String> {
        val result = mutableListOf<String>()
        for ((rule, attr) in ruleAttrs) {
            var arg = ""
            if (attr.argument != null)
                arg = "val ${attr.argument.name}: ${attr.argument.type}, "
            result += " class ${contextClassName(rule)}(${arg}text: String) : BaseRuleContext(text)"
        }
        return result
    }

    fun generateMethod() {

    }

    private fun reachable(): MutableSet<NonTerminal> {
        val rv = mutableSetOf<NonTerminal>()
        var oldSize = -1
        while (oldSize != rv.size) {
            oldSize = rv.size
            rules.forEach {
                if (rv.contains(it.key)) {
                    it.value.forEach {
                        it.children.forEach { if (it is NonTerminal) rv.add(it) }
                    }
                }
            }
        }
        return rv
    }


}
