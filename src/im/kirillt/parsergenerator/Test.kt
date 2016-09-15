package im.kirillt.parsergenerator

import myGen.*

fun main(args: Array<String>) {
    val parser = testParser(testTokenizer("(2 + 2) * 2"))
    val v = parser.start().v
    println("res = $v")
}

