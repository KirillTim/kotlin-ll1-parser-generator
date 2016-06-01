grammar ParserGenerator;
@parser::header {
import im.kirillt.parsergenerator.Utils;
import im.kirillt.parsergenerator.ParserGenerator;
}
input
    : line*
    ;

line
    : ruleR
    | token
    ;

grammaHeader
    : 'grammar ' grmrName ';' {ParserGenerator.grammarName = $grmrName.text;}
    ;

ruleR :
    ruleHead ':' ruleChild[$ruleHead.name] ('|' ruleChild[$ruleHead.name] )* ';'
    ;

ruleHead returns [String name]
    : ruleKeyword ruleParameterList {$name = $ruleKeyword.text;}
    ;

ruleParameterList
    : (ruleParametr)? ('returns' ruleParametr)? ('locals' ruleParametr)?
    ;

ruleParametr
    : '[' ANY_STRING ']'
    ;

ruleChild[String ruleName] locals[ArrayList<String> children]
@init {
    $children = new ArrayList<String>();
    Utils.p("rule name:"+$ruleName);
}
@after {
    ParserGenerator.addRule($ruleName, $children);
}
    : (ruleChildOptions {$children.add($ruleChildOptions.text);})+ (action)?
    ;

ruleChildOptions returns[String text]
    : ruleKeyword {$text = $ruleKeyword.text; Utils.p("rule");}
    | tokenKeyword {$text = $tokenKeyword.text; Utils.p("token");}
    | stringLiteral {$text = ParserGenerator.addToken($stringLiteral.text, ""); Utils.p("literal");}
    ;

action
    : '{' ANY_STRING '}'
    ;

token
    : tokenKeyword ':' stringLiteral + ';'
    {
        ParserGenerator.addToken($stringLiteral.text, $tokenKeyword.text);
    }
    ;

ruleKeyword
    : RULE_KEYWORD
    ;

RULE_KEYWORD
    : ('a' .. 'z')('a' .. 'z' | 'A' .. 'Z' | '_')*
    ;

tokenKeyword
    : TOKEN_KEYWORD
    ;

TOKEN_KEYWORD
    : ('A' .. 'Z')('a' .. 'z' | 'A' .. 'Z' | '_')*
    ;

grmrName
    : ANY_STRING
    ;

ANY_STRING
    : .
    ;

stringLiteral
    : STRING_LITERAL
    ;

STRING_LITERAL
   : '\'' ('\'\'' | ~ ('\''))* '\''
   ;

WS : [ \t \r \n]+ -> skip ;