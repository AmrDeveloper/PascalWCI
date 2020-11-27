package intermediate;

public interface TypeSpec {

    public TypeForm getForm();

    public void setIdentifier(SymbolTableEntry identifier);

    public SymbolTableEntry getIdentifier();

    public void setAttribute(TypeKey key, Object value);

    public Object getAttribute(TypeKey key);

    public boolean isPascalString();

    public TypeSpec baseType();

}
