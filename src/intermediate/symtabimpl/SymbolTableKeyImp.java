package intermediate.symtabimpl;

import intermediate.SymbolTableKey;

public enum  SymbolTableKeyImp implements SymbolTableKey {

    // Constant.
    CONSTANT_VALUE,

    // Procedure or function.
    ROUTINE_CODE, ROUTINE_SYMTAB, ROUTINE_ICODE,    ROUTINE_PARMS, ROUTINE_ROUTINES,

    //Variable or record field value.
    DATA_VALUE
}
