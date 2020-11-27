package intermediate.symtabimpl;

import intermediate.SymbolTableEntry;
import intermediate.SymbolTableStack;
import intermediate.TypeFactory;
import intermediate.TypeSpec;

import java.util.ArrayList;

import static intermediate.symtabimpl.SymbolTableKeyImp.CONSTANT_VALUE;
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

    public static void initialize(SymbolTableStack symbolTableStack) {
        initializeTypes(symbolTableStack);
        initializeConstants(symbolTableStack);
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
