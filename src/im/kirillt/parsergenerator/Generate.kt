package im.kirillt.parsergenerator

import im.kirillt.parsergenerator.base.BaseRuleContext
import im.kirillt.parsergenerator.grammar.ParserGeneratorLexer
import im.kirillt.parsergenerator.grammar.ParserGeneratorParser
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

fun main(args:Array<String>) {
    val input = File("data/test.g4").readText()
    val stream = ANTLRInputStream(input)
    val lexer = ParserGeneratorLexer(stream)
    val tokens = CommonTokenStream(lexer)
    val parser = ParserGeneratorParser(tokens)
    val tree = parser.input()
    ParserGenerator.generateTokenizer()
    ParserGenerator.generateParser()
//    for ((rule, direvs) in ParserGenerator.rules) {
//        println("Rule: $rule")
//        /*for ((ind, d) in direvs.withIndex()) {
//            println(ind.toString()+" : source")
//            println(d.generateBlock(ind))
//            println("---------------------")
//
//        }*/
//        println(ParserGenerator.generateMethod(rule, direvs))
//        println("---------------------")
//    }
}
