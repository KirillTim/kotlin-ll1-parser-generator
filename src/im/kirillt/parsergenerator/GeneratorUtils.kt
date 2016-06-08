package im.kirillt.parsergenerator

object GeneratorUtils {
    fun doubleBackSlash(input:String) = input.replace("\\", "\\\\")

    fun generateMapStringStringSource(what:Map<String, String>, varName: String): List<String> {
        val result = mutableListOf<String>()
        result += "val $varName = mutableMapOf<String, String>()"
        for ((key, value) in what) {
            result += "$varName.put(\"${doubleBackSlash(key)}\", \"${doubleBackSlash(value)}\")"
        }
        return result
    }

    class Indenter(val result:StringBuilder, var indent :Int = 0) {
        fun writeln(s: String) {
            for (i in 0..indent - 1) result.append("    ");
            result.append("$s\n")
        }
        fun indented(block: () -> Unit) {
            indent++; block(); indent--
        }

    }

    fun contextClassName(ruleName: String) =
        "${ruleName.first().toUpperCase()+ruleName.drop(1)}Context"
}
