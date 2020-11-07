package intermediate;

public interface SymbolTableStack {

    public int getCurrentNestingLevel();

    //Return the local symbol table which is at the top of the stack
    public SymbolTable getLocalSymbolTable();

    //Create and push a new entry into the local symbol table
    public SymbolTableEntry enterLocal(String name);

    //Look up an existing symbol table entry in the local symbol table
    public SymbolTableEntry lookupLocal(String name);

    //Loop up an existing symbol table entry throughout the stack
    public SymbolTableEntry lookup(String name);
}
