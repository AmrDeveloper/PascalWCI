package intermediate.typeimpl;

import intermediate.SymbolTableEntry;
import intermediate.TypeForm;
import intermediate.TypeKey;
import intermediate.TypeSpec;
import intermediate.symtabimpl.Predefined;

import java.util.HashMap;

import static intermediate.typeimpl.TypeFormImpl.ARRAY;
import static intermediate.typeimpl.TypeFormImpl.SUBRANGE;
import static intermediate.typeimpl.TypeKeyImpl.*;

public class TypeSpecImpl
        extends HashMap<TypeKey, Object>
        implements TypeSpec {

    // type form
    private TypeForm form;

    // type identifier
    private SymbolTableEntry identifier;

    /**
     * Constructor.
     * @param form the type form.
     */
    public TypeSpecImpl(TypeForm form) {
        this.form = form;
        this.identifier = null;
    }

    /**
     * Constructor.
     * @param value a string value.
     */
    public TypeSpecImpl(String value) {
        this.form = ARRAY;

        TypeSpec indexType = new TypeSpecImpl(SUBRANGE);
        indexType.setAttribute(SUBRANGE_BASE_TYPE, Predefined.integerType);
        indexType.setAttribute(SUBRANGE_MIN_VALUE, 1);
        indexType.setAttribute(SUBRANGE_MAX_VALUE, value.length());

        setAttribute(ARRAY_INDEX_TYPE, indexType);
        setAttribute(ARRAY_ELEMENT_TYPE, Predefined.charType);
        setAttribute(ARRAY_ELEMENT_COUNT, value.length());
    }


    @Override
    public TypeForm getForm() {
        return form;
    }

    @Override
    public void setIdentifier(SymbolTableEntry identifier) {
         this.identifier = identifier;
    }

    @Override
    public SymbolTableEntry getIdentifier() {
        return identifier;
    }

    @Override
    public void setAttribute(TypeKey key, Object value) {
         this.put(key, value);
    }

    @Override
    public Object getAttribute(TypeKey key) {
        return get(key);
    }

    @Override
    public boolean isPascalString() {
        if(form == ARRAY) {
            TypeSpec elementType = (TypeSpec) getAttribute(ARRAY_ELEMENT_TYPE);
            TypeSpec indexType = (TypeSpec) getAttribute(ARRAY_INDEX_TYPE);

            return (elementType.baseType() == Predefined.charType)
                    && (indexType.baseType() == Predefined.integerType);
        }
        return false;
    }

    @Override
    public TypeSpec baseType() {
        return form == SUBRANGE
                ? (TypeSpec) getAttribute(SUBRANGE_BASE_TYPE): this;
    }
}
