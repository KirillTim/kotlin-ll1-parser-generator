grammar Pascal;
varBlock : VAR rule_L ;

rule_L : rule_D SEMICOLON rule_Lp ;

rule_Lp
    : rule_L
    |
    ;

rule_D :
    rule_N COLON type
    {
        for (i in $rule_N.ans.split("|"))
            println(i +" of type "+$type.text)
    };

rule_N returns[ans: String]:
    variable rule_Np {$ans = $variable.text + $rule_Np.ans};

rule_Np returns[ans: String]
    : ',' rule_N {$ans = "|"+$rule_N.ans}
    | {$ans = ""}
    ;

type : str {println("get type="+$str.text)};

variable : str {println("get var="+$str.text)};

str : STR;

STR : '[A-Z]+';

VAR : 'var';

SEMICOLON : ';';

COLON : ':';