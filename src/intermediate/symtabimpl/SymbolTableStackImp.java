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

    // entry for the main program id
    private SymbolTableEntry programId;

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
        SymbolTableEntry foundEntry = null;

        // Search the current and enclosing scopes
        for(int i = currentNestingLevel ; (i >= 0) && (foundEntry == null) ;--i) {
            foundEntry = get(i).lookup(name);
        }

        return foundEntry;
    }

    @Override
    public void setProgramId(SymbolTableEntry entry) {
        this.programId = entry;
    }

    @Override
    public SymbolTableEntry getProgramId() {
        return programId;
    }

    @Override
    public SymbolTable push() {
        SymbolTable symbolTable = SymbolTableFactory.createSymbolTable(++currentNestingLevel);
        add(symbolTable);
        return symbolTable;
    }

    @Override
    public SymbolTable push(SymbolTable table) {
        ++currentNestingLevel;
        add(table);
        return table;
    }

    @Override
    public SymbolTable pop() {
        SymbolTable symbolTable = get(currentNestingLevel);
        remove(currentNestingLevel--);
        return symbolTable;
    }
}
