package im.kirillt.parsergenerator

import myGen.*

fun main(args: Array<String>) {
    val tokenizer = testTokenizer("10*4+1*(2+3)")
    val parser = testParser(tokenizer)
    println(parser.start().v)
}

