grammar expr;
start : e {var i = 0; i++; println("in start, i="+i);};

e : t eP;
eP : '\+' t eP | ;
t : f tP;
tP : MUL f tP | ;
f : NUM | '\(' e '\)' ;

PLUS: '\+' ;
MUL: '\*' ;
LB: '\(';
RB: '\)';
NUM: '[0-9]+';