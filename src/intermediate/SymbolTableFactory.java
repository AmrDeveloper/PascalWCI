package intermediate;

import intermediate.symtabimpl.SymbolTableEntryImp;
import intermediate.symtabimpl.SymbolTableImp;
import intermediate.symtabimpl.SymbolTableStackImp;

public class SymbolTableFactory {

    public static SymbolTableStack createSymbolTableStack() {
        return new SymbolTableStackImp();
    }

    public static SymbolTable createSymbolTable(int nestingLevel) {
        return new SymbolTableImp(nestingLevel);
    }

    public static SymbolTableEntry createSymbolTableEntry(String name, SymbolTable symbolTable) {
        return new SymbolTableEntryImp(name, symbolTable);
    }
}
