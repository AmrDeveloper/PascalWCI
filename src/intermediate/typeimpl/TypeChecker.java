package intermediate.typeimpl;

import intermediate.TypeForm;
import intermediate.TypeSpec;
import intermediate.symtabimpl.Predefined;

import static intermediate.typeimpl.TypeFormImpl.ENUMERATION;
import static intermediate.typeimpl.TypeFormImpl.SCALAR;

public class TypeChecker {

    public static boolean isInteger(TypeSpec type) {
        return (type != null) && (type.baseType() == Predefined.integerType);
    }

    public static boolean areBothInteger(TypeSpec type1, TypeSpec type2) {
        return isInteger(type1) && isInteger(type2);
    }

    public static boolean isReal(TypeSpec type) {
        return (type != null) && (type.baseType() == Predefined.realType);
    }

    public static boolean isIntegerOrReal(TypeSpec type) {
        return isInteger(type) || isReal(type);
    }

    public static boolean isAtLeastOneReal(TypeSpec type1, TypeSpec type2) {
        return (isReal(type1) && isReal(type2)) ||
                (isReal(type1) && isInteger(type2)) ||
                (isInteger(type1) && isReal(type2));
    }

    public static boolean isBoolean(TypeSpec type) {
        return (type != null) && (type.baseType() == Predefined.booleanType);
    }

    public static boolean areBothBoolean(TypeSpec type1, TypeSpec type2) {
        return isBoolean(type1) || isBoolean(type2);
    }

    public static boolean isChar(TypeSpec type) {
        return (type != null) && (type.baseType() == Predefined.charType);
    }

    public static boolean areAssignmentCompatible(TypeSpec target, TypeSpec value) {
        if((target == null) || (value == null)) return false;

        target = target.baseType();
        value = value.baseType();

        boolean isCompatible = false;

        // Identical types.
        if(target == value) isCompatible = true;
        else if(isReal(target) && isInteger(value)) isCompatible = true;
        else isCompatible = target.isPascalString() && value.isPascalString();

        return isCompatible;
    }

    public static boolean areComparisonCompatible(TypeSpec type1, TypeSpec type2) {
        if((type1 == null) || (type2 == null)) return false;

        type1 = type1.baseType();
        type2 = type2.baseType();
        TypeForm form = type1.getForm();

        boolean isCompatible = false;

        if((type1 == type2) && ((form == SCALAR) || (form == ENUMERATION))) isCompatible = true;
        else if(isAtLeastOneReal(type1, type2)) isCompatible = true;
        else isCompatible = type1.isPascalString() && type2.isPascalString();

        return isCompatible;
    }
}
