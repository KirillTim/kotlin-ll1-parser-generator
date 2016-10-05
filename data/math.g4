grammar math;

@header {
import im.kirillt.parsergenerator.MathHelper.map
}

start
    : line cont;

cont
    : start
    |
    ;

line returns [res : Int]
    : variable '=' expr ';' {$res = $expr.v; map[$variable.text] = $expr.v; println($variable.text+" = "+$expr.v)};

expr returns[v : Int]: item exprCont[$item.res] {$v = $exprCont.x};

exprCont[fst : Int] returns[x : Int]
    : '+' expr {$x = $fst + $expr.v;}
    | '-' expr {$x = $fst - $expr.v;}
    | '*' expr {$x = $fst * $expr.v;}
    | '/' expr {$x = $fst / $expr.v;}
    | {$x = $fst;}
    ;

item returns [res : Int]
    : variable {$res = map[$variable.text]}
    | num {$res = ($num.text).toInt()}
    ;

variable : Var;
Var : r'[a-zA-Z]+[a-zA-Z0-9]*';

num : Num;
Num : r'[0-9]+';