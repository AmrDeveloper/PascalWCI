package backend.interpreter.memoryimpl;

import backend.interpreter.ActivationRecord;
import backend.interpreter.RuntimeDisplay;

import java.util.ArrayList;

public class RuntimeDisplayImpl extends ArrayList<ActivationRecord> implements RuntimeDisplay {

    public RuntimeDisplayImpl() {
        // dummy element 0 that wll never used
        add(null);
    }

    @Override
    public ActivationRecord getActivationRecord(int nestingLevel) {
        return get(nestingLevel);
    }

    @Override
    public void callUpdate(int nestingLevel, ActivationRecord activationRecord) {
        if(nestingLevel >= size()) {
            add(activationRecord);
        }
        else {
            ActivationRecord prevActivationRecord = get(nestingLevel);
            set(nestingLevel, activationRecord.makeLinkTo(prevActivationRecord));
        }
    }

    @Override
    public void returnUpdate(int nestingLevel) {
        int topIndex = size() - 1;
        ActivationRecord activationRecord = get(nestingLevel);
        ActivationRecord previousActivationRecord = activationRecord.linkedTo();

        if(previousActivationRecord != null) {
            set(nestingLevel, previousActivationRecord);
        }
        else if(nestingLevel == topIndex) {
            remove(topIndex);
        }
    }
}
