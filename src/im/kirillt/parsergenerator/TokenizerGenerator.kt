package im.kirillt.parsergenerator

import java.io.File

object TokenizerGenerator {
    private fun generateTokensClasses(tokens: Map<String, String>): List<String> {
        val result = mutableListOf<String>()
        for (tokenName in tokens.keys) {
            result += "class Token$tokenName(text:String) : Token(\"$tokenName\", text)"
        }
        return result
    }

    fun generate(grammarName: String, tokens: Map<String, String>, folder:String = "myGen/") {
        val className = "${grammarName}Tokenizer"
        val file = File("$folder$className.kt")
        val result = StringBuilder()
        val indenter = Utils.Indenter(result)
        indenter.writeln("import im.kirillt.parsergenerator.Tokenizer")
        indenter.writeln("import im.kirillt.parsergenerator.Token")
        indenter.writeln("")
        generateTokensClasses(tokens).forEach { indenter.writeln(it) }
        indenter.writeln("")
        indenter.writeln("private object namespace {")
        indenter.indented {
            indenter.writeln("fun tokens(): Map<String, String> {")
            indenter.indented {
                Utils.generateMapStringStringSource(tokens, "tokens").forEach { indenter.writeln(it) }
                indenter.writeln("return tokens")
            }
            indenter.writeln("}")
        }
        indenter.writeln("}")
        indenter.writeln("class $className (input:String) : Tokenizer(namespace.tokens(), input) {")
        indenter.indented {
            indenter.writeln("override fun nextToken(): Token {")
            indenter.indented {
                indenter.writeln("var tok = super.nextToken()")
                for (name in tokens.keys) {
                    indenter.writeln("if (tok.name == \"$name\") { tok = Token$name(tok.text) }")
                }
                indenter.writeln("return tok")
            }
            indenter.writeln("}")
        }
        indenter.writeln("}")
        file.writeText(result.toString())
    }
}