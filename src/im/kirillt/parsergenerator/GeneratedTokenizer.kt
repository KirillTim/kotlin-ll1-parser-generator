package im.kirillt.parsergenerator

import im.kirillt.parsergenerator.Token

/**
 * Created by kirill on 07.06.16.
 */

private object namespace {
   fun tokens(): Map<String, String> {
       val map = mutableMapOf<String, String>()
       map.put("","")
       return map
   }
}

class TokenT1(text:String) : Token("T1", text)

class GeneratedTokenizer(input:String) : Tokenizer(namespace.tokens(), input) {
    override fun nextToken(): Token {
        var t = super.nextToken()
        t = TokenT1(t.text)
        return t
    }
}
