package backend.interpreter.debuggerimpl;

import backend.interpreter.Cell;
import backend.interpreter.Debugger;
import frontend.pascal.PascalTokenType;
import intermediate.SymbolTable;
import intermediate.SymbolTableEntry;
import intermediate.TypeForm;
import intermediate.TypeSpec;
import intermediate.symtabimpl.Predefined;
import intermediate.typeimpl.TypeFormImpl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import static frontend.pascal.PascalTokenType.*;
import static intermediate.typeimpl.TypeFormImpl.ENUMERATION;
import static intermediate.typeimpl.TypeFormImpl.SUBRANGE;
import static intermediate.typeimpl.TypeKeyImpl.*;

public class CellTypePair {

    // memory cell
    private Cell cell;

    // data type
    private TypeSpec type;

    // parent debugger
    private Debugger debugger;

    protected CellTypePair(TypeSpec type, Cell cell, Debugger debugger) throws Exception {
        this.type = type;
        this.cell = cell;
        this.debugger = debugger;

        parseVariable();
    }

    // Synchronization set for variable modifiers.
    protected static final EnumSet<PascalTokenType> MODIFIER_SET = EnumSet.of(LEFT_BRACKET, DOT);

    protected void parseVariable() throws Exception {
        TypeForm form = type.getForm();
        Object value = cell.getValue();

        // Loop to process array subscripts and record fields
        while (MODIFIER_SET.contains(debugger.currentToken().getType())) {
            if (form == TypeFormImpl.ARRAY) {
                parseArrayVariable((Cell[]) value);
            } else if (form == TypeFormImpl.RECORD) {
                parseRecordVariable((HashMap) value);
            }

            value = cell.getValue();
            form = type.getForm();
        }
    }

    private void parseArrayVariable(Cell[] array) throws Exception {
        debugger.nextToken();

        int index = debugger.getInteger("Integer index expected.");
        int minValue = 0;
        TypeSpec indexType = (TypeSpec) type.getAttribute(ARRAY_INDEX_TYPE);

        rangeCheck(index, indexType, "Index out of range.");
        type = (TypeSpec) type.getAttribute(SUBRANGE_MIN_VALUE);

        if (indexType.getForm() == SUBRANGE) {
            minValue = (Integer) indexType.getAttribute(SUBRANGE_MIN_VALUE);
        }

        cell = array[index - minValue];

        if (debugger.currentToken().getType() == RIGHT_BRACKET) {
            debugger.nextToken();
        } else {
            throw new Exception("] expected.");
        }
    }

    private void parseRecordVariable(HashMap record) throws Exception {
        debugger.nextToken();

        String fieldName = debugger.getWord("Field name expected");

        if (record.containsKey(fieldName)) {
            cell = (Cell) record.get(fieldName);
        } else {
            throw new Exception("Invalid field name");
        }

        SymbolTable symbolTable = (SymbolTable) type.getAttribute(RECORD_SYMTAB);
        SymbolTableEntry id = symbolTable.lookup(fieldName);
        type = id.getTypeSpec();
    }

    protected void setValue(Object value)
            throws Exception {
        if (((type.baseType() == Predefined.integerType)
                && (value instanceof Integer))
                || ((type == Predefined.realType)
                && (value instanceof Float))
                || ((type == Predefined.booleanType)
                && (value instanceof Boolean))
                || ((type == Predefined.charType)
                && (value instanceof Character))) {
            if (type.baseType() == Predefined.integerType) {
                rangeCheck((Integer) value, type, "Value out of range.");
            }
            cell.setValue(value);
        } else {
            throw new Exception("Type mismatch.");
        }
    }

    private void rangeCheck(int value, TypeSpec type, String errorMessage) throws Exception {
        TypeForm form = type.getForm();
        Integer minValue = null;
        Integer maxValue = null;

        if (form == SUBRANGE) {
            minValue = (Integer) type.getAttribute(SUBRANGE_MIN_VALUE);
            maxValue = (Integer) type.getAttribute(SUBRANGE_MAX_VALUE);
        } else if (form == ENUMERATION) {
            List<SymbolTableEntry> constants = (ArrayList<SymbolTableEntry>) type.getAttribute(ENUMERATION_CONSTANTS);

            minValue = 0;
            maxValue = constants.size() - 1;
        }

        if ((minValue != null) && ((value < minValue) || (value > maxValue))) {
            throw new Exception(errorMessage);
        }
    }

    public Cell getCell() {
        return cell;
    }

    public TypeSpec getType() {
        return type;
    }
}
