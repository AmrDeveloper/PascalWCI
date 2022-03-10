PROGRAM IO;{console run}
{find the square root of a number }
VAR
    x: real;
FUNCTION root(x : real) : real;
    VAR
        r : real;

    BEGIN
        r := 1;
        REPEAT
            r := (x/r + r)/2;
        UNTIL abs(x/sqr(r) - 1) < 0.00001;
        root := r;
    END;

BEGIN
    WHILE true DO
        BEGIN
            write('input a number: ');
            readln(x);
            writeln('root of ', x, ' is ', root(x));
        END;
END.
