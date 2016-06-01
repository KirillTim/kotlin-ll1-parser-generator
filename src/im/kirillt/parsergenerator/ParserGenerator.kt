package im.kirillt.parsergenerator

import java.util.*

open class RuleItem(val name: String) {
    companion object {
        fun fromString(string: String): RuleItem {
            if (!string.all { it.isLetterOrDigit() || it == '_' })
                throw Exception("bad string: " + string)
            if (string[0].isUpperCase())
                return Terminal(string)
            else
                return NonTerminal(string)
        }
    }
}

class Terminal(n: String) : RuleItem(n)
class NonTerminal(n: String) : RuleItem(n)

data class RuleOption(val derivation: MutableList<RuleItem> = mutableListOf())

object ParserGenerator {
    @JvmField
    var grammarName = ""
    val tokens = mutableMapOf<String, String>()
    private var unnamedTokensCount = 0
    val rules = mutableMapOf<String, MutableList<RuleOption>>()

    @JvmStatic
    fun addToken(string: String, name: String = ""): String {
        if (name.isEmpty()) {
            for (i in tokens) {
                if (i.value.equals(string))
                    return i.key
            }
            unnamedTokensCount++
            val key = "_T_" + unnamedTokensCount;
            tokens.put(key, string)
            return key
        }
        if (tokens.containsKey(name))
            throw Exception("already have token with name '$name'")

        tokens.put(name, string)
        return name
    }

    @JvmStatic
    fun addRule(termName: String, left: ArrayList<String>) {
        if (!rules.containsKey(termName))
            rules.put(termName, mutableListOf<RuleOption>())
        val derivation = RuleOption()
        for (i in left)
            derivation.derivation.add(RuleItem.fromString(i))
        rules[termName]!!.add(derivation)
    }


}
