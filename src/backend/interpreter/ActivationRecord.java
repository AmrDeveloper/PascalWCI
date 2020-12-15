package backend.interpreter;

import intermediate.SymbolTableEntry;

import java.util.List;

public interface ActivationRecord {

    public SymbolTableEntry getRoutineId();

    public Cell getCell(String name);

    public List<String> getAllNames();

    public int getNestingLevel();

    public ActivationRecord linkedTo();

    public ActivationRecord makeLinkTo(ActivationRecord activationRecord);
}
