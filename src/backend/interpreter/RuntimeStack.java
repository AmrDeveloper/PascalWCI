package backend.interpreter;

import java.util.List;

/**
 * Interface for the interpreter's runtime stack
 */
public interface RuntimeStack {

    public List<ActivationRecord> records();

    public ActivationRecord getTopmost(int nestingLevel);

    public int currentNestingLevel();

    public void pop();

    public void push(ActivationRecord activationRecord);
}
