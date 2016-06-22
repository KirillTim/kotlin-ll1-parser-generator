grammar test;

start returns [v: Int]
    : t e { $v = $t.v + $e.v; println("start: t="+$t.v);}
    ;

e returns [v: Int]
    : '\+' t e { $v = $t.v + $e.v; }
    | { $v = 0; }
    ;

t returns [v: Int]
    : f d { $v = $f.v * $d.v; println("in t");}
    ;

d returns [v: Int]
    : '\*' f d { $v = $f.v * $d.v; }
    | { $v = 1; println("EPS in d"); }
    ;

f returns [v: Int]
    : num { $v = $num.text.toInt(); println("get num="+$v);}
    | '\(' start '\)' { $v = $start.v; }
    ;

num : NUM;

NUM : '[0-9]+';
