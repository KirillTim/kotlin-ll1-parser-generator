package im.kirillt.parsergenerator

import im.kirillt.parsergenerator.ParserGenerator.TokenString

import java.io.File

object TokenizerGenerator {
    private fun generateTokensClasses(tokenNames: List<String>): List<String> {
        return tokenNames.map { "class Token$it(text:String) : Token(\"$it\", text)" }
    }

    fun generate(grammarName: String, tokens: Map<String, TokenString>, folder: String = "myGen",
                 packageName: String = folder) {
        val className = "${grammarName}Tokenizer"
        val file = File("$folder/$className.kt")
        val indenter = GeneratorUtils.Indenter(StringBuilder())
        indenter.writeln("package $packageName")
        indenter.writeln("import im.kirillt.parsergenerator.base.BaseTokenizer")
        indenter.writeln("import im.kirillt.parsergenerator.base.Token")
        indenter.writeln("import im.kirillt.parsergenerator.ParserGenerator.TokenString")
        indenter.writeln("")
        generateTokensClasses(tokens.keys.toList()).forEach { indenter.writeln(it) }
        indenter.writeln("")
        indenter.writeln("private object namespace {")
        indenter.indented {
            indenter.writeln("fun tokens(): Map<String, TokenString> {")
            indenter.indented {
                GeneratorUtils.generateMapStringTokenStringSource(tokens, "tokens").forEach { indenter.writeln(it) }
                indenter.writeln("return tokens")
            }
            indenter.writeln("}")
        }
        indenter.writeln("}")
        indenter.writeln("class $className (input:String) : BaseTokenizer(namespace.tokens(), input) {")
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
        file.writeText(indenter.result.toString())
    }
}