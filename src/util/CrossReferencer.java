package util;

import intermediate.*;
import intermediate.symtabimpl.DefinitionImpl;
import intermediate.typeimpl.TypeFormImpl;

import java.util.ArrayList;
import java.util.List;

import static intermediate.symtabimpl.SymbolTableKeyImp.*;
import static intermediate.typeimpl.TypeKeyImpl.*;

public class CrossReferencer {

    private static final int NAME_WIDTH = 16;
    private static final String NAME_FORMAT = "%-" + NAME_WIDTH + "s";
    private static final String NUMBERS_LABEL = " Line numbers    ";
    private static final String NUMBERS_UNDERLINE = " ------------    ";
    private static final String NUMBER_FORMAT = "%03d";
    private static final String ENUM_CONST_FORMAT = "%" + NAME_WIDTH + "s = %s";

    public static final int LABEL_WIDTH = NUMBERS_LABEL.length();
    public static final int INDENT_WIDTH = NAME_WIDTH + LABEL_WIDTH;

    public static final StringBuilder INDENT = new StringBuilder(INDENT_WIDTH);

    static {
        for (int i = 0; i < INDENT_WIDTH; i++) INDENT.append(" ");
    }

    //Print the cross-reference table
    public void print(SymbolTableStack symbolTableStack) {
        System.out.println("\n===== CROSS-REFERENCE TABLE =====");
        SymbolTableEntry programId = symbolTableStack.getProgramId();
        printRoutine(programId);
    }

    private void printRoutine(SymbolTableEntry routineId) {
        Definition definition = routineId.getDefinition();
        System.out.println("\n*** " + definition.toString() + " " + routineId.getName() + " ***");
        printColumnHeadings();

        // Print the entries in the routine&apos;s symbol table.
        SymbolTable symbolTable = (SymbolTable) routineId.getAttribute(ROUTINE_SYMTAB);
        List<TypeSpec> newRecordTypes = new ArrayList<>();
        printSymbolTable(symbolTable, newRecordTypes);

        // Print cross-reference tables for any records defined in the routine.
        if (newRecordTypes.size() > 0) {
            printRecords(newRecordTypes);
        }

        // Print any procedures and functions defined in the routine.
        List<SymbolTableEntry> routineIds
                = (ArrayList<SymbolTableEntry>) routineId.getAttribute(ROUTINE_ROUTINES);
        if (routineIds != null) {
            for (SymbolTableEntry rtnId : routineIds) {
                printRoutine(rtnId);
            }
        }
    }

    private void printColumnHeadings() {
        System.out.println();
        System.out.println(String.format(NAME_FORMAT, "Identifier") + NUMBERS_LABEL + "Type specification");
        System.out.println(String.format(NAME_FORMAT, "----------") + NUMBERS_UNDERLINE + "------------------");
    }

    private void printSymbolTable(SymbolTable table, List<TypeSpec> recordTypes) {
        // Loop over the sorted list of symbol table entries
        List<SymbolTableEntry> sorted = table.sortedEntries();
        for (SymbolTableEntry entry : sorted) {
            List<Integer> lineNumbers = entry.getLineNumbers();

            System.out.printf(NAME_FORMAT, entry.getName());
            if (lineNumbers != null) {
                for (Integer lineNumber : lineNumbers) {
                    System.out.printf(NUMBER_FORMAT, lineNumber);
                }
            }
            System.out.println();
            printEntry(entry, recordTypes);
        }
    }

    private void printEntry(SymbolTableEntry entry, List<TypeSpec> recordTypes) {
        Definition definition = entry.getDefinition();
        int nestingLevel = entry.getSymbolTable().getNestingLevel();
        System.out.println(INDENT + "Defined as: " + definition.getText());
        System.out.println(INDENT + "Scope nesting level: " + nestingLevel);

        // Print the type specification
        TypeSpec type = entry.getTypeSpec();
        printType(type);

        switch ((DefinitionImpl) definition) {
            case CONSTANT: {
                Object value = entry.getAttribute(CONSTANT_VALUE);
                System.out.println(INDENT + "Value = " + toString(value));

                // Print the type details only if the type is unnamed.
                if (type.getIdentifier() == null) {
                    printTypeDetail(type, recordTypes);
                }
                break;
            }
            case ENUMERATION_CONSTANT: {
                Object value = entry.getAttribute(CONSTANT_VALUE);
                System.out.println(INDENT + "Value = " + toString(value));
                break;
            }
            case TYPE: {
                // Print the type details only when the type is first defined.
                if (entry == type.getIdentifier()) {
                    printTypeDetail(type, recordTypes);
                }
                break;
            }
            case VARIABLE: {
                // Print the type details only if the type is unnamed.
                if (type.getIdentifier() == null) {
                    printTypeDetail(type, recordTypes);
                }
                break;
            }
        }
    }

    private String toString(Object value) {
        return value instanceof String ? "'" + value + "'" : value.toString();
    }

    private void printType(TypeSpec type) {
        if (type != null) {
            TypeForm form = type.getForm();
            SymbolTableEntry typeId = type.getIdentifier();
            String typeName = typeId != null ? typeId.getName() : "<unnamed>";
            System.out.println(INDENT + "Type form = " + form + ", Type id = " + typeName);
        }
    }

    private void printTypeDetail(TypeSpec type, List<TypeSpec> recordTypes) {
        TypeForm form = type.getForm();
        switch ((TypeFormImpl) form) {
            case ENUMERATION: {
                List<SymbolTableEntry> constantIds = (ArrayList<SymbolTableEntry>) type.getAttribute(ENUMERATION_CONSTANTS);
                System.out.println(INDENT + "--- Enumeration constants ---");

                // Print each enumeration constant end its value
                for (SymbolTableEntry constantId : constantIds) {
                    String name = constantId.getName();
                    Object value = constantId.getAttribute(CONSTANT_VALUE);
                    System.out.println(INDENT + String.format(ENUM_CONST_FORMAT, name, value));
                }
                break;
            }
            case SUBRANGE: {
                Object minValue = type.getAttribute(SUBRANGE_MIN_VALUE);
                Object maxValue = type.getAttribute(SUBRANGE_MAX_VALUE);
                TypeSpec baseTypeSpec = (TypeSpec) type.getAttribute(SUBRANGE_BASE_TYPE);

                System.out.println(INDENT + "--- Base type ---");
                printType(baseTypeSpec);

                // Print the base type details only if the type is unnamed.
                if (baseTypeSpec.getIdentifier() == null) {
                    printTypeDetail(baseTypeSpec, recordTypes);
                }
                System.out.print(INDENT + "Range = ");
                System.out.println(toString(minValue) + ".." + toString(maxValue));
                break;
            }
            case ARRAY: {
                TypeSpec indexType = (TypeSpec) type.getAttribute(ARRAY_INDEX_TYPE);
                TypeSpec elementType = (TypeSpec) type.getAttribute(ARRAY_ELEMENT_TYPE);
                int count = (Integer) type.getAttribute(ARRAY_ELEMENT_COUNT);

                System.out.println(INDENT + "--- INDEX TYPE ---");
                printType(indexType);

                // Print the index type details only if the type is unnamed.
                if (indexType.getIdentifier() == null) {
                    printTypeDetail(indexType, recordTypes);
                }

                System.out.println(INDENT + "--- ELEMENT TYPE ---");
                printType(elementType);
                System.out.println(INDENT.toString() + count + " elements");

                // Print the element type details only if the type is unnamed.
                if (elementType.getIdentifier() == null) {
                    printTypeDetail(elementType, recordTypes);
                }
                break;
            }
            case RECORD: {
                recordTypes.add(type);
                break;
            }
        }
    }

    private void printRecords(List<TypeSpec> recordTypes) {
        for (TypeSpec recordType : recordTypes) {
            SymbolTableEntry recordId = recordType.getIdentifier();
            String name = recordId != null ? recordId.getName() : "<unnamed>";
            System.out.println("\n--- RECORD " + name + " ---");
            printColumnHeadings();

            // Print the entries in the record&apos;s symbol table.
            SymbolTable symbolTable = (SymbolTable) recordType.getAttribute(RECORD_SYMTAB);
            List<TypeSpec> newRecordTypes = new ArrayList<>();
            printSymbolTable(symbolTable, newRecordTypes);

            // Print cross-reference tables for any nested records.
            if (newRecordTypes.size() > 0) {
                printRecords(newRecordTypes);
            }
        }
    }
}
