PROGRAM Expression;
VAR
    num1, num2, num3 : integer;
    num4 : real;
    b1, b2, b3 : boolean;

BEGIN
    num1 := -1;
    writeln(num1);

    num2 := -num1;
    writeln(num2);

    num3 := (10 + 10) DIV 4 * 5;
    writeln(num3);

    writeln(num3 MOD 3);

    num4 := (1.4 + 3.6) * 5 / 4;
    writeln(num4);
    
    b1 := true;
    b2 := true;
    b3 := false;

    writeln(NOT b1);
    writeln(b1 AND b2);
    writeln(b1 AND b3);
    writeln(b1 OR b2);
    writeln(NOT b1 OR (b2 AND NOT b3));
    
END.