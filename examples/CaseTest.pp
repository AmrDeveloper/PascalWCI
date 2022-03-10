{Test Case Statement }
PROGRAM CaseTst;
VAR
    ch : char;
    num : integer;
BEGIN
    {Test case 1}
    CASE 10 DIV 4 OF{10/4 The result is real }
        1, 3, 5, 7: writeln('odd');
        2, 4, 6, 8: writeln('even')
    END;
    {Test case 2}
    ch := 'B';
    CASE ch OF
        'A', 'B', 'C': writeln('UpperCase');
        'a', 'b', 'c': writeln('LowerCase')
    END;

    {Test case 3}
    num := 3;
    CASE num OF
        1, 3, 5: writeln('not my answer!');
        2, 4, 6:
        CASE num OF
            2: writeln(2);
            4: writeln(4);
            6: writeln(6);
        END;
    END;
END.