PROGRAM Scope;
VAR
    test : integer;

PROCEDURE layer1;
    VAR
        test : real;
    PROCEDURE layer2;
        VAR
            test : char;
        BEGIN
            test := 'A';
            writeln(test);
        END;
    BEGIN
        layer2;
        test := 2.0;
        writeln(test);
    END;
BEGIN
    layer1;
    test := 1;
    writeln(test);
END.