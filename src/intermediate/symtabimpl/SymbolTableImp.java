package intermediate.symtabimpl;

import intermediate.SymbolTable;
import intermediate.SymbolTableEntry;
import intermediate.SymbolTableFactory;

import java.util.*;

public class SymbolTableImp
        extends TreeMap<String, SymbolTableEntry>
        implements SymbolTable {

    private int nestingLevel;

    public SymbolTableImp(int nestingLevel) {
       this.nestingLevel = nestingLevel;
    }

    @Override
    public int getNestingLevel() {
        return nestingLevel;
    }

    @Override
    public SymbolTableEntry enter(String name) {
        SymbolTableEntry entry =
                SymbolTableFactory.createSymbolTableEntry(name, this);
        put(name, entry);
        return entry;
    }

    @Override
    public SymbolTableEntry lookup(String name) {
        return get(name);
    }

    @Override
    public List<SymbolTableEntry> sortedEntries() {
        Collection<SymbolTableEntry> entries = values();
        Iterator<SymbolTableEntry> iter = entries.iterator();
        List<SymbolTableEntry> list = new ArrayList<>(size());

        while(iter.hasNext()) {
           list.add(iter.next());
        }

        return list;
    }
}
