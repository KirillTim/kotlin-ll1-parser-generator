grammar simple;

start
    : num["num1"] '_' num1[$num.text] {println($num.r + 10); println("nums: " + $num.text + ", " + $num1.text);}
    | let["let1"] '_' let1["let2"] {println("let: " + $let.text + ", " + $let1.text);}
    ;

num[arg: String] returns [r: Int]
    : NUM {println("num get arg:" + $arg); $r = 100500;}
    ;

num1[arg: String]
    : NUM {println("num1 get arg:" + $arg)}
    ;

let[arg: String]
    : LET {println("let get arg:" + $arg)}
    ;

let1[arg: String]
    : LET {println("let1 get arg:" + $arg)}
    ;

LET : '[a-zA-Z]+';

NUM : '[0-9]+';