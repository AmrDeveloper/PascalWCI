package backend.interpreter.memoryimpl;

import backend.interpreter.Cell;
import backend.interpreter.MemoryFactory;
import backend.interpreter.MemoryMap;
import intermediate.*;
import intermediate.typeimpl.TypeFormImpl;

import java.util.*;

import static intermediate.symtabimpl.DefinitionImpl.*;
import static intermediate.typeimpl.TypeKeyImpl.*;

public class MemoryMapImpl extends HashMap<String, Cell> implements MemoryMap {

    public MemoryMapImpl(SymbolTable symbolTable) {
        List<SymbolTableEntry> entries = symbolTable.sortedEntries();

        // Loop for each entry of the symbol table
        for (SymbolTableEntry entry : entries) {
            Definition definition = entry.getDefinition();

            // Not a VAR parameter: Allocate cells for the data type in the hashmap
            String name = entry.getName();
            if ((definition == VARIABLE)
                    || (definition == FUNCTION)
                    || (definition == VALUE_PARM)
                    || (definition == FIELD)) {
                TypeSpec type = entry.getTypeSpec();
                put(name, MemoryFactory.createCell(allocateCellValue(type)));
            }
            // VAR parameter: Allocate a single cell to hold a reference in the hashmap
            else {
                put(name, MemoryFactory.createCell(null));
            }
        }
    }

    @Override
    public Cell getCell(String name) {
        return get(name);
    }

    @Override
    public List<String> getAllNames() {
        Set<String> names = keySet();
        return new ArrayList<>(names);
    }

    private Object allocateCellValue(TypeSpec typeSpec) {
        TypeForm form = typeSpec.getForm();

        switch ((TypeFormImpl) form) {
            case ARRAY: {
                return allocateArrayCells(typeSpec);
            }
            case RECORD: {
                return allocateRecordMap(typeSpec);
            }
            default: {
                // uninitialized scalar value
                return null;
            }
        }
    }

    private Object[] allocateArrayCells(TypeSpec typeSpec) {
        int elementCount = (Integer) typeSpec.getAttribute(ARRAY_ELEMENT_COUNT);
        TypeSpec elementType = (TypeSpec) typeSpec.getAttribute(ARRAY_ELEMENT_TYPE);
        Cell[] allocation = new Cell[elementCount];

        for(int i = 0 ; i < elementCount ; i++) {
            allocation[i] = MemoryFactory.createCell(allocateCellValue(elementType));
        }

        return allocation;
    }

    public MemoryMap allocateRecordMap(TypeSpec typeSpec) {
        SymbolTable symbolTable = (SymbolTable) typeSpec.getAttribute(RECORD_SYMTAB);
        return MemoryFactory.createMemoryMap(symbolTable);
    }
}
