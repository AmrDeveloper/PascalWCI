package intermediate.symtabimpl;

import intermediate.*;

import java.util.ArrayList;

import static intermediate.symtabimpl.DefinitionImpl.FUNCTION;
import static intermediate.symtabimpl.DefinitionImpl.PROCEDURE;
import static intermediate.symtabimpl.RoutineCodeImpl.*;
import static intermediate.symtabimpl.SymbolTableKeyImp.CONSTANT_VALUE;
import static intermediate.symtabimpl.SymbolTableKeyImp.ROUTINE_CODE;
import static intermediate.typeimpl.TypeFormImpl.ENUMERATION;
import static intermediate.typeimpl.TypeFormImpl.SCALAR;
import static intermediate.typeimpl.TypeKeyImpl.ENUMERATION_CONSTANTS;

public class Predefined {

    // Predefined types.
    public static TypeSpec integerType;
    public static TypeSpec realType;
    public static TypeSpec booleanType;
    public static TypeSpec charType;
    public static TypeSpec undefinedType;

    // Predefined identifiers.
    public static SymbolTableEntry integerId;
    public static SymbolTableEntry realId;
    public static SymbolTableEntry booleanId;
    public static SymbolTableEntry charId;
    public static SymbolTableEntry falseId;
    public static SymbolTableEntry trueId;

    // Predefined Standard procedures and functions
    public static SymbolTableEntry readId;
    public static SymbolTableEntry readlnId;
    public static SymbolTableEntry writeId;
    public static SymbolTableEntry writelnId;
    public static SymbolTableEntry absId;
    public static SymbolTableEntry arctanId;
    public static SymbolTableEntry chrId;
    public static SymbolTableEntry cosId;
    public static SymbolTableEntry eofId;
    public static SymbolTableEntry eolnId;
    public static SymbolTableEntry expId;
    public static SymbolTableEntry lnId;
    public static SymbolTableEntry oddId;
    public static SymbolTableEntry ordId;
    public static SymbolTableEntry predId;
    public static SymbolTableEntry roundId;
    public static SymbolTableEntry sinId;
    public static SymbolTableEntry sqrId;
    public static SymbolTableEntry sqrtId;
    public static SymbolTableEntry succId;
    public static SymbolTableEntry truncId;

    public static void initialize(SymbolTableStack symbolTableStack) {
        initializeTypes(symbolTableStack);
        initializeConstants(symbolTableStack);
        initializeStandardRoutines(symbolTableStack);
    }

    private static void initializeStandardRoutines(SymbolTableStack symbolTableStack) {
        readId = enterStandard(symbolTableStack, PROCEDURE, "read", READ);
        readlnId = enterStandard(symbolTableStack, PROCEDURE, "readln", READLN);
        writeId = enterStandard(symbolTableStack, PROCEDURE, "write", WRITE);
        writelnId = enterStandard(symbolTableStack, PROCEDURE, "writeln", WRITELN);
        absId = enterStandard(symbolTableStack, FUNCTION, "abs", ABS);
        arctanId = enterStandard(symbolTableStack, FUNCTION, "arctan", ARCTAN);
        chrId = enterStandard(symbolTableStack, FUNCTION, "chr", CHR);
        cosId = enterStandard(symbolTableStack, FUNCTION, "cos", COS);
        eofId = enterStandard(symbolTableStack, FUNCTION, "eof", EOF);
        eolnId = enterStandard(symbolTableStack, FUNCTION, "eoln", EOLN);
        expId = enterStandard(symbolTableStack, FUNCTION, "exp", EXP);
        lnId = enterStandard(symbolTableStack, FUNCTION, "ln", LN);
        oddId = enterStandard(symbolTableStack, FUNCTION, "odd", ODD);
        ordId = enterStandard(symbolTableStack, FUNCTION, "ord", ODD);
        predId = enterStandard(symbolTableStack, FUNCTION, "pred", PRED);
        roundId = enterStandard(symbolTableStack, FUNCTION, "round", ROUND);
        sinId = enterStandard(symbolTableStack, FUNCTION, "sin", SIN);
        sqrId = enterStandard(symbolTableStack, FUNCTION, "sqr", SQR);
        sqrtId = enterStandard(symbolTableStack, FUNCTION, "sqrt", SQRT);
        succId = enterStandard(symbolTableStack, FUNCTION, "succ", SUCC);
        truncId = enterStandard(symbolTableStack, FUNCTION, "trunc", TRUNC);
    }

    private static SymbolTableEntry enterStandard(SymbolTableStack symbolTableStack,
                                                  Definition definition,
                                                  String name,
                                                  RoutineCode routineCode) {
        SymbolTableEntry procId = symbolTableStack.enterLocal(name);
        procId.setDefinition(definition);
        procId.setAttribute(ROUTINE_CODE, routineCode);

        return procId;
    }

    private static void initializeTypes(SymbolTableStack symbolTableStack) {
        // Type integer.
        integerId = symbolTableStack.enterLocal("integer");
        integerType = TypeFactory.createType(SCALAR);
        integerType.setIdentifier(integerId);
        integerId.setDefinition(DefinitionImpl.TYPE);
        integerId.setTypeSpec(integerType);

        // Type real.
        realId = symbolTableStack.enterLocal("real");
        realType = TypeFactory.createType(SCALAR);
        realType.setIdentifier(realId);
        realId.setDefinition(DefinitionImpl.TYPE);
        realId.setTypeSpec(realType);

        // Type boolean.
        booleanId = symbolTableStack.enterLocal("boolean");
        booleanType = TypeFactory.createType(ENUMERATION);
        booleanType.setIdentifier(booleanId);
        booleanId.setDefinition(DefinitionImpl.TYPE);
        booleanId.setTypeSpec(booleanType);

        // Type char.
        charId = symbolTableStack.enterLocal("char");
        charType = TypeFactory.createType(SCALAR);
        charType.setIdentifier(charId);
        charId.setDefinition(DefinitionImpl.TYPE);
        charId.setTypeSpec(charType);

        // Undefined type.
        undefinedType = TypeFactory.createType(SCALAR);
    }

    private static void initializeConstants(SymbolTableStack symbolTableStack) {
        // Boolean enumeration constant false.
        falseId = symbolTableStack.enterLocal("false");
        falseId.setDefinition(DefinitionImpl.ENUMERATION_CONSTANT);
        falseId.setTypeSpec(booleanType);
        falseId.setAttribute(CONSTANT_VALUE, 0);

        // Boolean enumeration constant true.
        trueId = symbolTableStack.enterLocal("true");
        trueId.setDefinition(DefinitionImpl.ENUMERATION_CONSTANT);
        trueId.setTypeSpec(booleanType);
        trueId.setAttribute(CONSTANT_VALUE, 1);

        // Add false and true to the boolean enumeration type.
        ArrayList<SymbolTableEntry> constants = new ArrayList<>();
        constants.add(falseId);
        constants.add(trueId);
        booleanType.setAttribute(ENUMERATION_CONSTANTS, constants);
    }
}
