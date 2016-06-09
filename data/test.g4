grammar expr;
start : e[10] {var i = 0; i++; println("in start, i="+i);};

e[int count] returns [int other]: t eP {println("get count="+count);};
eP : '\+' t eP | ;
t : f tP;
tP : MUL f tP | ;
f : NUM | '\(' e '\)' ;

PLUS: '\+' ;
MUL: '\*' ;
LB: '\(';
RB: '\)';
NUM: '[0-9]+';