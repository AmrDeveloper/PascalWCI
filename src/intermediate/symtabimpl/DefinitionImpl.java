package intermediate.symtabimpl;

import intermediate.Definition;

public enum DefinitionImpl implements Definition {

    CONSTANT, ENUMERATION_CONSTANT("enumeration constant"),
    TYPE, VARIABLE, FIELD("record field"),
    VALUE_PARM("value parameter"), VAR_PARM("VAR parameter"),
    PROGRAM_PARM("program parameter"),
    PROGRAM,
    PROCEDURE, FUNCTION,
    UNDEFINED;

    private String text;

    DefinitionImpl() {
        text = this.toString().toLowerCase();
    }

    DefinitionImpl(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }
}
