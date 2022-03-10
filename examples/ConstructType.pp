{Advanced data types supported}
PROGRAM ConstructType;

VAR
    enum : (one, two, three, four);{enumerate}
    subrange : 1..100;{子域}
    arr : ARRAY[1..10] OF integer;{array}
    rec : RECORD{Record type }
        name : ARRAY[1..10] OF char;
        age : integer;
        isStudent : boolean;
    END;

BEGIN
    enum := two;
    writeln(three < enum);{Enumeration cannot be output directly }
    enum := four;
    writeln(three < enum);

    subrange := 10;{Note that assignments cannot be out of bounds }
    writeln(subrange);
    
    arr[1] := 11;{Note starting from 1}
    writeln(arr[1]);{Can only operate on defined values, output arr[2] will cause an error }
    rec.name := 'Tony';
    rec.age := 10;
    writeln('My name is ', rec.name);
    writeln('I am ', rec.age, ' years old.');
END.