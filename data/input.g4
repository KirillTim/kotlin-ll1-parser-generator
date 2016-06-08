
start : 'var' rule_L ;

rule_L : rule_D ';' rule_Lp ;

rule_Lp  : EPS
    | rule_L
    ;

rule_D : rule_N ':' Type ;

rule_N : Variable rule_Np ;

rule_Np  : EPS
    | ',' rule_N
    ;

Type : 'type' ;

Variable : 'variable' ;

