package im.kirillt.parsergenerator

import im.kirillt.parsergenerator.base.EOF
import im.kirillt.parsergenerator.grammar.ParserGeneratorLexer
import im.kirillt.parsergenerator.grammar.ParserGeneratorParser
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File


fun main(args: Array<String>) {
    val input = File("data/test.g4").readText()
    //val input = "test:'LAL'chld'BLAH'\n|'kek';\nchld:'123';"
    //val input = "CHLD:'child!';chld:CHLD;test:'LAL'chld'LAL'|'a'chld;"
    //val input = "start : 'var' L ;"
    val stream = ANTLRInputStream(input);
    val lexer = ParserGeneratorLexer(stream)
    val tokens = CommonTokenStream(lexer)
    val parser = ParserGeneratorParser(tokens)
    val tree = parser.input()
    println("tokens:")
    for (i in ParserGenerator.tokens) {
        println(i.key+" -> "+i.value)
    }
    println("=====================\nrules:")
    for (i in ParserGenerator.rules) {
        print(i.key.name+"\t:\t")
        for (j in i.value[0].children) {
            print(j.name+" ")
        }
        println()
        for (j in 1 .. i.value.size-1) {
            print("\t\t|\t")
            for (jj in i.value[j].children)
                print(jj.name+" ")
            println()
        }
    }
    println("=====================\nFIRST:")
    ParserGenerator.constructFirstAndFollow()
    for (i in ParserGenerator.FIRST) {
        print(i.key.name+" : ")
        for (j in i.value) {
            var str = ParserGenerator.tokens[j.name]
            if (str == null)
                str = j.toString()
            print(str + " ")
        }
        println()
    }
    println("=====================\nFOLLOW:")
    for (i in ParserGenerator.FOLLOW) {
        print(i.key.name+" : ")
        for (j in i.value) {
            var str = ParserGenerator.tokens[j.name]
            if (str == null)
                str = j.toString()
            print(str + " ")
        }
        println()
    }

    println(ParserGenerator.rules)

    /*ParserGenerator.generateTokenizer()
    val tok = myGen.Tokenizer("2+(2)*(3+45)")
    while(true) {
        val t = tok.nextToken()
        if (t is EOF)
            break
        println(t)
    }*/
}