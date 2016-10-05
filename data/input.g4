grammar input;

start : vaaaar variable ':' Type ';';

vaaaar : Var {println("get var keyword")};

Var : 'var';

Type : 'Integer' ;

variable : Variable {println("get variable tokenName")};

Variable : r'[a-zA-Z]+' ;

