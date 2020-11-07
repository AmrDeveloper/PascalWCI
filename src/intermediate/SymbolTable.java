package intermediate;

import java.util.List;

public interface SymbolTable {

    public int getNestingLevel();

    //Create and enter a new entry
    public SymbolTableEntry enter(String name);

    //Loop up an existing symbol table entry
    public SymbolTableEntry lookup(String name);

    //Return a list of symbol table entries sorted by name
    public List<SymbolTableEntry> sortedEntries();
}
