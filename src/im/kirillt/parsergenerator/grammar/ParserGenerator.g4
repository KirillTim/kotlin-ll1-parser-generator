grammar ParserGenerator;
@parser::header {
import im.kirillt.parsergenerator.Utils;
import im.kirillt.parsergenerator.ParserGenerator;
import im.kirillt.parsergenerator.ParserGenerator.NonTerminal;
import im.kirillt.parsergenerator.ParserGenerator.Terminal;
import im.kirillt.parsergenerator.ParserGenerator.RuleItem;
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

ruleChild[String ruleName] locals[ArrayList<RuleItem> children]
@init {
    $children = new ArrayList<RuleItem>();
    Utils.p("rule name:"+$ruleName);
}
@after {
    ParserGenerator.addRule($ruleName, $children);
}
    : (ruleChildOptions {$children.add($ruleChildOptions.item);})* (action)?
    ;

ruleChildOptions returns[RuleItem item]
    : ruleKeyword {$item = new NonTerminal($ruleKeyword.text); Utils.p("rule");}
    | tokenKeyword {$item = new Terminal($tokenKeyword.text); Utils.p("token");}
    | stringLiteral {
                        String name = ParserGenerator.addToken($stringLiteral.text, "");
                        $item = new Terminal(name);
                        Utils.p("literal");
                    }
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
    : [a-z]+
    ;

stringLiteral
    : STRING_LITERAL
    ;

STRING_LITERAL
   : '\'' ('\'\'' | ~ ('\''))* '\''
   ;

WS : [ \t\r\n]+ -> skip ;