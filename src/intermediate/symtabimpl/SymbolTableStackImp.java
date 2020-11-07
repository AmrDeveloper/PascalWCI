package intermediate.symtabimpl;

import intermediate.SymbolTable;
import intermediate.SymbolTableEntry;
import intermediate.SymbolTableFactory;
import intermediate.SymbolTableStack;

import java.util.ArrayList;

public class SymbolTableStackImp
        extends ArrayList<SymbolTable>
        implements SymbolTableStack {

    private int currentNestingLevel;

    public SymbolTableStackImp() {
        this.currentNestingLevel = 0;
        add(SymbolTableFactory.createSymbolTable(currentNestingLevel));
    }

    @Override
    public int getCurrentNestingLevel() {
        return currentNestingLevel;
    }

    @Override
    public SymbolTable getLocalSymbolTable() {
        return get(currentNestingLevel);
    }

    @Override
    public SymbolTableEntry enterLocal(String name) {
        return get(currentNestingLevel).enter(name);
    }

    @Override
    public SymbolTableEntry lookupLocal(String name) {
        return get(currentNestingLevel).lookup(name);
    }

    @Override
    public SymbolTableEntry lookup(String name) {
        return lookupLocal(name);
    }
}
