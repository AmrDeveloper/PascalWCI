package backend.interpreter;

public interface RuntimeDisplay {

    /**
     * Get the activation record at a given nesting level
     * @param nestingLevel the nesting level
     * @return the activation record
     */
    public ActivationRecord getActivationRecord(int nestingLevel);

    /**
     * Update the display for a call to a routine at a given nesting level
     * @param nestingLevel the nesting level
     * @param activationRecord the activation record for the routine
     */
    public void callUpdate(int nestingLevel, ActivationRecord activationRecord);

    /**
     * Update the display for a return from a routine at a given nesting level
     * @param nestingLevel the nesting level.
     */
    public void returnUpdate(int nestingLevel);
}
