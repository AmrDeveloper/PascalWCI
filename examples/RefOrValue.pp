PROGRAM RefOrValue;
VAR
    x, y : integer;

PROCEDURE func(num1:integer; VAR num2:integer);
    BEGIN
        num1 := num1 * 10;
        num2 := num2 * 10;
    END;

BEGIN
    x := 1;
    y := 2;
    func(x, y);
    writeln('Pass by value:    ', x);
    writeln('Pass by reference:    ', y);
END.
