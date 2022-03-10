{The loop structure in test 3 }
PROGRAM LoopTest;

VAR
    root, number : real;
    i, sum : integer;
    n, pi : real;

BEGIN
    {Newton's method for square root: testing while loop }
    number := 2;
    root := number;
    WHILE root*root - number > 0.00001 DO BEGIN
        root := (number/root + root)/2;
    END;
    writeln('root of 2: ', root);

    {Find Factorial 5!: Testing FOR Loops }
    sum := 1;
    FOR i := 1 TO 5 DO BEGIN
        sum := sum * i;
    END;
    writeln('5! = ', sum);{120}

    {Using Formulas to Find Pi: Testing the REPEAT Loop }
    n := 1;
    pi := 0;
    REPEAT
        pi := pi + (1/(4*n-3) - 1/(4*n-1));
        n := n+1;
    UNTIL n >= 1000;
    pi := 4*pi;
    writeln('PI = ', pi);

END.