PROGRAM test;

VAR
    sum, n:integer;

PROCEDURE recurSum(VAR sum, n: integer); forward;{forward declaration }

PROCEDURE recurSum;
    BEGIN
        IF n > 0 THEN BEGIN
	    sum := sum + n;
            n := n - 1;
            recurSum(sum, n);{recursive call }
        END;
    END;


BEGIN{主程序}
    sum := 0;
    n := 100;
    recurSum(sum, n);
    writeln('1+2+...+100 = ', sum);
END.