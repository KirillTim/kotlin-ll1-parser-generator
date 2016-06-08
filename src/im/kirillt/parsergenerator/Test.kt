package im.kirillt.parsergenerator
import myGen.*
fun main(args:Array<String>) {
    val tokenizer = Tokenizer("2+3")
    val parser  = Parser(tokenizer)
    parser.start()
}
