package intermediate.symtabimpl;

import intermediate.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SymbolTableEntryImp
        extends HashMap<SymbolTableKey, Object>
        implements SymbolTableEntry {

    //Entry Name
    private String name;

    //Parent Symbol Table
    private SymbolTable symbolTable;

    //Source line numbers
    private List<Integer> lineNumbers;

    // how the identifier is defined
    private Definition definition;

    // type specification
    private TypeSpec typeSpec;

    public SymbolTableEntryImp(String name, SymbolTable symbolTable) {
        this.name = name;
        this.symbolTable = symbolTable;
        this.lineNumbers = new ArrayList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    @Override
    public void appendLineNumber(int lineNumber) {
        lineNumbers.add(lineNumber);
    }

    @Override
    public List<Integer> getLineNumbers() {
        return lineNumbers;
    }

    @Override
    public void setAttribute(SymbolTableKey key, Object value) {
         put(key, value);
    }

    @Override
    public Object getAttribute(SymbolTableKey key) {
        return get(key);
    }

    @Override
    public void setDefinition(Definition definition) {
         this.definition = definition;
    }

    @Override
    public Definition getDefinition() {
        return definition;
    }

    @Override
    public void setTypeSpec(TypeSpec typeSpec) {
        this.typeSpec = typeSpec;
    }

    @Override
    public TypeSpec getTypeSpec() {
        return typeSpec;
    }
}
