package backend.interpreter;

import backend.interpreter.memoryimpl.*;
import intermediate.SymbolTable;
import intermediate.SymbolTableEntry;

public class MemoryFactory {

    public static RuntimeStack createRuntimeStack() {
        return new RuntimeStackImpl();
    }

    public static RuntimeDisplay createRuntimeDisplay() {
        return new RuntimeDisplayImpl();
    }

    public static ActivationRecord createActivationRecord(SymbolTableEntry symbolTableEntry) {
        return new ActivationRecordImpl(symbolTableEntry);
    }

    public static MemoryMap createMemoryMap(SymbolTable symbolTable) {
        return new MemoryMapImpl(symbolTable);
    }

    public static Cell createCell(Object value) {
        return new CellImpl(value);
    }
}
