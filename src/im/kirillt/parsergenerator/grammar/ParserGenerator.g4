grammar ParserGenerator;
@parser::header {
import im.kirillt.parsergenerator.Utils;
import im.kirillt.parsergenerator.ParserGenerator;
import im.kirillt.parsergenerator.ParserGenerator.NonTerminal;
import im.kirillt.parsergenerator.ParserGenerator.Terminal;
import im.kirillt.parsergenerator.ParserGenerator.RuleItem;
import im.kirillt.parsergenerator.ParserGenerator.RuleChild;
import im.kirillt.parsergenerator.ParserGenerator.Variable;
import im.kirillt.parsergenerator.ParserGenerator.TokenString;
}

input
    : grammaHeader line*
    ;

line
    : ruleR
    | token
    ;

grammaHeader
    : 'grammar' id ';' {ParserGenerator.grammarName = $id.text;}
    ;

ruleR :
    ruleHead ':' ruleChild[$ruleHead.name] ('|' ruleChild[$ruleHead.name] )* ';'
    ;

ruleHead returns [String name]
    : RULE_KEYWORD ruleParameterList[$RULE_KEYWORD.text] {$name = $RULE_KEYWORD.text;}
    ;

ruleParameterList[String ruleName]
    : (ruleParametr{ParserGenerator.addRuleArg($ruleName, new Variable($ruleParametr.name, $ruleParametr.type));})?
        ('returns' ruleParametr {ParserGenerator.addRuleReturns($ruleName, new Variable($ruleParametr.name, $ruleParametr.type));})?
    ;
ruleParametr returns[String type, String name]
    : '[' name_str=id ':' type_str=id ']' {
        $type = $type_str.text;
        $name = $name_str.text;
        Utils.p("type="+$type_str.text);
    }
    ;


ruleChild[String ruleName] locals[ArrayList<RuleChild> children]
@init {
    $children = new ArrayList<RuleChild>();
    Utils.p("rule name:"+$ruleName);
    String ruleAction = "";
    String ruleArg = "";
}
@after {
    ParserGenerator.addRule($ruleName, $children, ruleAction);
}
    : (ruleChildOptions {$children.add($ruleChildOptions.item);})* (ACTION {ruleAction = $ACTION.text;})?
    ;

ruleChildOptions returns[RuleChild item]
    : ruleOption { $item = $ruleOption.item; Utils.p("rule");}
    | TOKEN_KEYWORD {$item = new RuleChild(new Terminal($TOKEN_KEYWORD.text), ""); Utils.p("token");}
    | rStingLiteral {
                        String name = ParserGenerator.addToken($rStingLiteral.item, "");
                        $item = new RuleChild(new Terminal(name), "");
                        Utils.p("literal");
                    }
    ;


ruleOption returns[RuleChild item]
@init {
    //$item = new RuleChild();
}
    : (RULE_KEYWORD{$item = new RuleChild(new NonTerminal($RULE_KEYWORD.text), "");})
                ('[' RULE_ARGUMENT ']' {$item.setArg($RULE_ARGUMENT.text);})?
    ;

token
    : TOKEN_KEYWORD ':' rStingLiteral + ';'
    {
        ParserGenerator.addToken($rStingLiteral.item, $TOKEN_KEYWORD.text);
        Utils.p("token add");
    }
    ;

id : RULE_KEYWORD | TOKEN_KEYWORD;

rStingLiteral returns[TokenString item]
@init {
    $item = new TokenString();
}
    : ('r' {$item.setRegex(true);})? STRING_LITERAL {$item.setString($STRING_LITERAL.text);}
    ;

RULE_KEYWORD: LOWCASE (LOWCASE|CAPITAL| '_' | DIGIT)* ;
TOKEN_KEYWORD : CAPITAL (LOWCASE|CAPITAL| '_' | DIGIT)* ;
RULE_ARGUMENT
    : ('$' (LOWCASE|CAPITAL|'_') (LOWCASE|CAPITAL|'_'|DIGIT)* ('.' (LOWCASE|CAPITAL|'_') (LOWCASE|CAPITAL|'_'|DIGIT)*)*)
    | DIGIT+
    | STRING_LITERAL_2
    ;

CAPITAL:    'A'..'Z';
LOWCASE:    'a'..'z';
DIGIT:      [0-9];

ACTION
	:	'{'
		(	ACTION
        |	'/*' .*? '*/'
        |	'//' ~[\r\n]*
        |	.
		)*?
		('}'|EOF)
	;

STRING_LITERAL
   : '\'' ('\'\'' | ~ ('\''))* '\''
   ;

STRING_LITERAL_2
   : '"' ('""' | ~ ('"'))* '"'
   ;

WS : [ \t\r\n]+ -> skip ;
