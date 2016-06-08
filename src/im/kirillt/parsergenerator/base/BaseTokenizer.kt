package im.kirillt.parsergenerator.base

import java.util.regex.Pattern

open class Token(val name: String, val text: String)

class EOF(): Token("$", "")

open class BaseTokenizer(val tokens: Map<String, String>, val input: String) {
    private val patterns = tokens.map { Pair(Pattern.compile(it.value), it.key) }.toMap()
    var curPos = 0
        private set
    var curToken: Token? = null
        private set

    open fun nextToken(): Token {
        if (curPos == input.length) {
            curToken = EOF()
            return curToken as Token
        }
        var longestToken = Token("", "")
        for ((pattern, name) in patterns) {
            val matcher = pattern.matcher(input)
            if (matcher.find(curPos) && matcher.start() == curPos) {
                val len = matcher.end() - matcher.start()
                val str = input.substring(matcher.start(), matcher.end())
                if (len > longestToken.text.length)
                    longestToken = Token(name, str)
            }
        }
        curToken = longestToken
        curPos += longestToken.text.length
        return longestToken
    }
}
