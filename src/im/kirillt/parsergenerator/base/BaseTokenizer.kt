package im.kirillt.parsergenerator.base

import im.kirillt.parsergenerator.ParserGenerator.TokenString
import java.util.regex.Pattern

open class Token(val name: String, val text: String)

class EOF() : Token("$", "")

open class BaseTokenizer(tokens: Map<String, TokenString>, val input: String) {
    private val planeStrings = mutableMapOf<String, String>()
    private val patterns = mutableMapOf<Pattern, String>()

    init {
        for ((name, token) in tokens) {
            if (token.isRegex)
                patterns[Pattern.compile(token.string)] = name
            else
                planeStrings[token.string] = name
        }
    }

    //private val patterns = tokens.map { Pair(Pattern.compile(it.value), it.key) }.toMap()
    var curPos = 0
        private set
    var curToken: Token? = null
        private set

    open fun nextToken(): Token {
        if (curPos == input.length) {
            curToken = EOF()
            return curToken as Token
        }
        while (curPos < input.length && isBlank(input[curPos]))
            curPos++

        var longestToken = Token("", "")
        //try plane strings at first
        for ((string, name) in planeStrings) {
            if (input.substring(curPos).startsWith(string)) {
                if (string.length > longestToken.text.length)
                    longestToken = Token(name, string)
            }
        }

        if (longestToken.name.isEmpty()) {
            for ((pattern, name) in patterns) {
                val matcher = pattern.matcher(input)
                if (matcher.find(curPos) && matcher.start() == curPos) {
                    val len = matcher.end() - matcher.start()
                    val str = input.substring(matcher.start(), matcher.end())
                    if (len > longestToken.text.length)
                        longestToken = Token(name, str)
                }
            }
        }
        curToken = longestToken
        curPos += longestToken.text.length
        return longestToken
    }

    private fun isBlank(c: Char) = listOf(' ', '\n', '\t', '\r').contains(c)
}
