package backend.interpreter.memoryimpl;

import backend.interpreter.Cell;

public class CellImpl implements Cell {

    // value contained in the memory cell
    private Object value;

    public CellImpl(Object value) {
        this.value = value;
    }

    @Override
    public void setValue(Object newValue) {
        this.value = newValue;
    }

    @Override
    public Object getValue() {
        return value;
    }
}
