package im.kirillt.parsergenerator

import im.kirillt.parsergenerator.ParserGenerator.TokenString

object GeneratorUtils {
    fun doubleBackSlash(input: String) = input.replace("\\", "\\\\")

    fun generateMapStringTokenStringSource(what: Map<String, TokenString>, varName: String): List<String> {
        val result = mutableListOf<String>()
        result += "val $varName = mutableMapOf<String, TokenString>()"
        for ((key, value) in what) {
            result += "$varName.put(\"${doubleBackSlash(key)}\", TokenString(\"${doubleBackSlash(value.string)}\", ${value.isRegex}))"
        }
        return result
    }

    class Indenter(val result: StringBuilder, var indent: Int = 0) {
        fun writeln(s: String) {
            for (i in 0..indent - 1) result.append("    ");
            result.append("$s\n")
        }

        fun indented(block: () -> Unit) {
            indent++; block(); indent--
        }

    }

    fun contextClassName(ruleName: String) =
            "${ruleName.first().toUpperCase() + ruleName.drop(1)}Context"
}
