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

    // Set the symbol table entry for the main program identifier
    public void setProgramId(SymbolTableEntry entry);

    // Return the symbol table entry for the main program identifier
    public SymbolTableEntry getProgramId();

    // Push new SymbolTable onto the stack
    public SymbolTable push();

    // Push SymbolTable(table) onto the stack
    public SymbolTable push(SymbolTable table);

    // Pop a SymbolTable off the stack
    public SymbolTable pop();
}
