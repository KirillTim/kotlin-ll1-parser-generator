package im.kirillt.parsergenerator

import im.kirillt.parsergenerator.GeneratorUtils.Indenter
import im.kirillt.parsergenerator.GeneratorUtils.contextClassName
import java.io.File
import java.util.*

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

    data class RuleChild(var item: RuleItem, var arg: String)

    val rules = mutableMapOf<NonTerminal, MutableList<Derivation>>()
    val ruleArgs = mutableMapOf<String, Variable>()
    val ruleReturns = mutableMapOf<String, Variable>()


    @JvmStatic
    fun addRuleArg(rule: String, v: Variable) = ruleArgs.put(rule, v)

    @JvmStatic
    fun addRuleReturns(rule: String, v: Variable) = ruleReturns.put(rule, v)

    class Derivation(val from: NonTerminal, val children: List<RuleChild>, val sourceCode: String = "") {

        fun generateCodeBlock(check: Boolean = true): String {
            val resultCtxName = "__resultCtx"

            val indenter = Indenter(StringBuilder())
            indenter.indented {
                indenter.writeln("val $resultCtxName = ${contextClassName(from.name)}()")
                for ((index, data) in children.withIndex()) {
                    val (child, args) = data
                    if (child is NonTerminal) {
                        val varName = "__" + child.name
                        val funCall = child.name
                        indenter.writeln("val $varName = $funCall(${replacePlaceHolders(from, args)})")
                        indenter.writeln("$resultCtxName.text += $varName.text")
                    } else if (child is Terminal) {
                        if (check) {
                            indenter.writeln("if (tokenizer.curToken!!.name != \"${child.name}\") {")
                            indenter.indented {
                                indenter.writeln("throw Exception(\"expected token '${child.name}', but get \"+tokenizer.curToken!!.name)")
                            }
                            indenter.writeln("}")
                            indenter.writeln("$resultCtxName.text += tokenizer.curToken!!.text")
                            indenter.writeln("tokenizer.nextToken()")
                        }
                    }
                }
                val action = replacePlaceHolders(from, sourceCode.dropWhile { it == '{' }.dropLastWhile { it == '}' })
                indenter.writeln(action)
                indenter.writeln("return $resultCtxName")
            }
            indenter.writeln("}")
            return indenter.result.toString()
        }


        fun first(): List<Terminal> {
            val first = children.first().item
            if (first is Terminal)
                return listOf(first);
            else
                return rules[first]!!.flatMap { it.first() }
        }

        companion object {
            fun replacePlaceHolders(from: NonTerminal, code: String, resultCtxName: String = "__resultCtx"): String {
                var result = code
                val ruleArgName = ruleArgs[from.name]?.name
                if (ruleArgName != null) {
                    //result = code.replace("$$ruleArgName.")
                    result = code.replace("$$ruleArgName", "__$ruleArgName")
                }
                val ruleReturns = ruleReturns[from.name]?.name
                if (ruleReturns != null)
                    result = result.replace("$$ruleReturns", "$resultCtxName.$ruleReturns")
                result = result.replace("$", "__")
                return result
            }
        }
    }


    val START = NonTerminal("start")
    val EOFTerminal = Terminal("$")
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
            for ((name, token_regexp) in tokens) {
                if (token_regexp.equals(fixed))
                    return name
            }
            unnamedTokensCount++
            val key = "_T_" + unnamedTokensCount;
            tokens[key] = fixed
            return key
        }
        if (tokens.containsKey(name))
            throw Exception("already have token with name '$name'")

        tokens[name] = fixed
        return name
    }

    @JvmStatic
            //add eps rule if left is empty
    fun addRule(nonTermName: String, left: ArrayList<RuleChild>, action: String) {
        val from = NonTerminal(nonTermName)
        nonTerminals.add(from)
        val children = when (left.isEmpty()) {
            true -> listOf(RuleChild(EPS, ""))
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
                    val add = first(alternative.children.first().item)
                    FIRST[it.key]!! += add
                }
            }
            changed = FIRST.map { it.value.size }.sum() != oldSize
        }

        rules.keys.forEach { FOLLOW[it] = mutableSetOf() }
        FOLLOW[START] = mutableSetOf(EOFTerminal)
        changed = true
        while (changed) {
            val oldSize = FOLLOW.map { it.value.size }.sum()
            rules.forEach {
                val A = it.key
                for (alternative in it.value) {
                    for (i in alternative.children.indices) {
                        val B = alternative.children[i].item
                        if (B !is NonTerminal)
                            continue
                        if (i == alternative.children.size - 1)
                            FOLLOW[B]!! += FOLLOW[A]!!
                        else {
                            val gamma = alternative.children[i + 1].item
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

    fun generateContextClasses(): List<String> {
        val result = mutableListOf<String>()
        for (rule in rules.keys) {
            val ret = ruleReturns[rule.name]
            val arg = if (ret != null) "var ${ret.name}: ${ret.type}? = null, " else ""
            result += "class ${contextClassName(rule.name)}(${arg}text: String = \"\") : BaseRuleContext(text)"
        }
        return result
    }


    fun generateMethod(rule: NonTerminal, derivations: MutableList<Derivation>): String {
        val indenter = GeneratorUtils.Indenter(StringBuilder())
        var arg = ""
        val v = ruleArgs[rule.name]
        if (v != null)
            arg += "__${v.name} : ${v.type}"
        indenter.writeln("fun ${rule.name}($arg): ${GeneratorUtils.contextClassName(rule.name)}{")
        indenter.indented {
            var epsRuleInd = -1
            for ((ind, option) in derivations.withIndex()) {
                if (option.children.size == 1 && option.children.first().item == EPS) {
                    epsRuleInd = ind
                    //indenter.writeln(Derivation.replacePlaceHolders(rule, option.sourceCode.dropWhile { it == '{' }.dropLastWhile { it == '}' }))
                    continue;
                }
                val sj = StringJoiner(" || ", "if (", ") {")
                for (tok in option.first()) {
                    sj.add("tokenizer.curToken!!.name == \"${tok.name}\"")
                }
                indenter.writeln(sj.toString())
                indenter.writeln(option.generateCodeBlock())
            }
            if (epsRuleInd != -1) {
                indenter.writeln("//check FOLLOW")
                val sj = StringJoiner(" || ", "if (", ") {")
                for (tok in FOLLOW[rule]!!)
                    sj.add("tokenizer.curToken!!.name == \"${tok.name}\"")
                indenter.writeln(sj.toString())
                indenter.indented {
                    indenter.writeln(derivations[epsRuleInd].generateCodeBlock(false).dropLast(2))
                    //indenter.writeln("return ${GeneratorUtils.contextClassName(rule.name)}()")
                }
                indenter.writeln("}")
            }
            indenter.writeln("throw Exception(\"Unexpected token: \"+tokenizer.curToken!!.name)")
        }
        indenter.writeln("}")
        return indenter.result.toString()
    }

    fun generateParser(folder: String = "myGen", packageName: String = folder) {
        val className = "${grammarName}Parser"
        val file = File("$folder/$className.kt")
        var str = "package $packageName\nimport im.kirillt.parsergenerator.base.BaseRuleContext\nimport $folder.${grammarName}Tokenizer\n"
        constructFirstAndFollow()
        generateContextClasses().forEach { str += it + "\n" }
        str += "class $className(val tokenizer: ${grammarName}Tokenizer) {\n"
        str += "init {tokenizer.nextToken()}\n"
        for ((rule, derivations) in rules) {
            str += generateMethod(rule, derivations)
        }
        str += "}\n"
        file.writeText(str)
    }

    private fun reachable(): MutableSet<NonTerminal> {
        val rv = mutableSetOf<NonTerminal>()
        var oldSize = -1
        while (oldSize != rv.size) {
            oldSize = rv.size
            rules.forEach {
                if (rv.contains(it.key)) {
                    it.value.forEach {
                        it.children.forEach { if (it.item is NonTerminal) rv.add(it.item as NonTerminal) }
                    }
                }
            }
        }
        return rv
    }


}
