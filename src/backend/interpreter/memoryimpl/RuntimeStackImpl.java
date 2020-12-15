package backend.interpreter.memoryimpl;

import backend.interpreter.ActivationRecord;
import backend.interpreter.MemoryFactory;
import backend.interpreter.RuntimeDisplay;
import backend.interpreter.RuntimeStack;

import java.util.ArrayList;
import java.util.List;

public class RuntimeStackImpl extends ArrayList<ActivationRecord> implements RuntimeStack {

    // Runtime display
    private RuntimeDisplay display;

    public RuntimeStackImpl() {
        display = MemoryFactory.createRuntimeDisplay();
    }

    @Override
    public List<ActivationRecord> records() {
        return this;
    }

    @Override
    public ActivationRecord getTopmost(int nestingLevel) {
        return display.getActivationRecord(nestingLevel);
    }

    @Override
    public int currentNestingLevel() {
        int topIndex = size() - 1;
        return topIndex >= 0 ? get(topIndex).getNestingLevel() : -1;
    }

    @Override
    public void push(ActivationRecord activationRecord) {
        int nestingLevel = activationRecord.getNestingLevel();
        add(activationRecord);
        display.callUpdate(nestingLevel, activationRecord);
    }

    @Override
    public void pop() {
        display.returnUpdate(currentNestingLevel());
        remove(size() - 1);
    }
}
