BEGIN {Calculate the square root of 4 using Newton&apos;s method.}
    i := 3;  ch := 'b';

    CASE i+1 OF
        1:           j := i;
        4:           j := 4*i;
        5, 2, 3:     j := 523*i;
    END;
END.