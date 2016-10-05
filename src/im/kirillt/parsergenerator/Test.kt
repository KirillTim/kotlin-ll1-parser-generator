package im.kirillt.parsergenerator

import myGen.*

fun main(args: Array<String>) {
    val data = "a = (2);\nb = a + 2;\nc = a + b * (b-3);\na = 3;\nc = a + b * (b-3);"
    val parser = mathParser(mathTokenizer(data))
    parser.start()
}

