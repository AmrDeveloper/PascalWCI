package backend.interpreter.memoryimpl;

import backend.interpreter.ActivationRecord;
import backend.interpreter.Cell;
import backend.interpreter.MemoryFactory;
import backend.interpreter.MemoryMap;
import intermediate.SymbolTable;
import intermediate.SymbolTableEntry;

import java.util.List;

import static intermediate.symtabimpl.SymbolTableKeyImp.ROUTINE_SYMTAB;

public class ActivationRecordImpl implements ActivationRecord {

    // SymbolTable entry for the routine's name
    private SymbolTableEntry routineId;

    // Dynamic link to the previous record
    private ActivationRecord link;

    // scope nesting level to this record
    private int nestingLevel;

    // memory map of this stack record
    private MemoryMap memoryMap;

    public ActivationRecordImpl(SymbolTableEntry routineId) {
        SymbolTable symbolTable = (SymbolTable) routineId.getAttribute(ROUTINE_SYMTAB);

        this.routineId = routineId;
        this.nestingLevel = symbolTable.getNestingLevel();
        this.memoryMap = MemoryFactory.createMemoryMap(symbolTable);
    }

    @Override
    public SymbolTableEntry getRoutineId() {
        return routineId;
    }

    @Override
    public Cell getCell(String name) {
        return memoryMap.getCell(name);
    }

    @Override
    public List<String> getAllNames() {
        return memoryMap.getAllNames();
    }

    @Override
    public int getNestingLevel() {
        return nestingLevel;
    }

    @Override
    public ActivationRecord linkedTo() {
        return link;
    }

    @Override
    public ActivationRecord makeLinkTo(ActivationRecord activationRecord) {
        link = activationRecord;
        return this;
    }
}
