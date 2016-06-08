grammar ParserGenerator;
@parser::header {
import im.kirillt.parsergenerator.Utils;
import im.kirillt.parsergenerator.ParserGenerator;
import im.kirillt.parsergenerator.ParserGenerator.NonTerminal;
import im.kirillt.parsergenerator.ParserGenerator.Terminal;
import im.kirillt.parsergenerator.ParserGenerator.RuleItem;
}
input
    : grammaHeader line*
    ;

line
    : ruleR
    | token
    ;

grammaHeader
    : 'grammar' grmrName ';' {ParserGenerator.grammarName = $grmrName.text;}
    ;

ruleR :
    ruleHead ':' ruleChild[$ruleHead.name] ('|' ruleChild[$ruleHead.name] )* ';'
    ;

ruleHead returns [String name]
    : ruleKeyword ruleParameterList[$ruleKeyword.text] {$name = $ruleKeyword.text;}
    ;

ruleParameterList[String ruleName]
    : (ruleParametr)? ('returns' ruleParametr)? //('locals' ruleParametr)?
    ;

ruleParametr
    : '[' type=ID name=ID ']'
    ;

ruleChild[String ruleName] locals[ArrayList<RuleItem> children]
@init {
    $children = new ArrayList<RuleItem>();
    Utils.p("rule name:"+$ruleName);
    String rulAection = "";
}
@after {
    ParserGenerator.addRule($ruleName, $children, rulAection);
}
    : (ruleChildOptions {$children.add($ruleChildOptions.item);})* (action {rulAection = $action.text;})?
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
    : ACTION
    ;

ACTION
	:	'{'
		(	ACTION
        |	'/*' .*? '*/'
        |	'//' ~[\r\n]*
        |	.
		)*?
		('}'|EOF)
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

tokenKeyword
    : TOKEN_KEYWORD
    ;

grmrName
    : ID
    ;

TOKEN_KEYWORD: ('A'..'Z') ('A'..'Z'|'_')*;

RULE_KEYWORD: ('a'..'z') ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'?')*;

ID: ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'?')*;

stringLiteral
    : STRING_LITERAL
    ;

STRING_LITERAL
   : '\'' ('\'\'' | ~ ('\''))* '\''
   ;

WS : [ \t\r\n]+ -> skip ;