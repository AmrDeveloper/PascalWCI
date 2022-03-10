{Basic data types supported}
PROGRAM BasicType;

{The type of the constant is determined by the rvalue }
CONST
    WEEKDAY = 7;{Integer constant }
    PI = 3.1415926;{floating point constant}
    X = 'x';{character constant }
    ERROR = 'There is an error!';{string constant }
    YES = true;{布尔常量}
{TYPE can name new constants, similar to C language typedef }
TYPE
    string = char;
    bool = boolean;
{The type must be specified after the variable definition }
VAR
    message : char;
    grade : real;
    age : integer;
    result : boolean;

BEGIN
    writeln(WEEKDAY);
    writeln(PI);
    writeln(X);
    writeln(ERROR);
    writeln(YES);
    message := X;
    writeln(message);   
END.