package intermediate;

import java.util.List;

public interface SymbolTableEntry {

    public String getName();

    //Return the symbol table that contains this entry
    public SymbolTable getSymbolTable();

    //Append a source line number to the entry
    public void appendLineNumber(int lineNumber);

    public List<Integer> getLineNumbers();

    public void setAttribute(SymbolTableKey key, Object value);

    public Object getAttribute(SymbolTableKey key);
}
