package backend.interpreter.debuggerimpl;

import backend.Backend;
import backend.interpreter.Debugger;
import backend.interpreter.RuntimeStack;
import ide.IDEControl;
import intermediate.Definition;
import intermediate.SymbolTableEntry;
import message.Message;

import java.util.List;

import static ide.IDEControl.*;

public class GUIDebugger extends Debugger {

    private CommandProcessor commandProcessor;

    public GUIDebugger(Backend backend, RuntimeStack runtimeStack) {
        super(backend, runtimeStack);
        commandProcessor = new CommandProcessor(this);
    }

    @Override
    public void processMessage(Message message) {
        commandProcessor.processMessage(message);
    }

    @Override
    public void promptForCommand() {

    }

    @Override
    public boolean parseCommand() {
        return commandProcessor.parseCommand();
    }

    @Override
    public void atStatement(Integer lineNumber) {
        System.out.println(DEBUGGER_AT_TAG + lineNumber);
    }

    @Override
    public void atBreakpoint(Integer lineNumber) {
        System.out.println(DEBUGGER_BREAK_TAG + lineNumber);
    }

    @Override
    public void atWatchpointValue(Integer lineNumber, String name, Object value) {

    }

    @Override
    public void atWatchpointAssignment(Integer lineNumber, String name, Object value) {

    }

    @Override
    public void callRoutine(Integer lineNumber, String routineName) {

    }

    @Override
    public void returnRoutine(Integer lineNumber, String routineName) {

    }

    @Override
    public void displayValue(String valueString) {
        System.out.println(valueString);
    }

    @Override
    public void displayCallStack(List stack) {
// Call stack header.
        System.out.println(DEBUGGER_ROUTINE_TAG + -1);

        for (Object item : stack) {

            // Name of a procedure or function.
            if (item instanceof SymbolTableEntry) {
                SymbolTableEntry routineId = (SymbolTableEntry) item;
                String routineName = routineId.getName();
                int level = routineId.getSymbolTable().getNestingLevel();
                Definition definition = routineId.getDefinition();

                System.out.println(DEBUGGER_ROUTINE_TAG + level + ":" +
                        definition.getText().toUpperCase() + " " +
                        routineName);
            }

            // Variable name-value pair.
            else if (item instanceof NameValuePair) {
                NameValuePair pair = (NameValuePair) item;
                System.out.print(DEBUGGER_VARIABLE_TAG +
                        pair.getVariableName() + ":");
                displayValue(pair.getValueString());
            }
        }

        // Call stack footer.
        System.out.println(DEBUGGER_ROUTINE_TAG + -2);
    }

    @Override
    public void quit() {
        System.out.println("!INTERPRETER:Program terminated.");
        System.exit(-1);
    }

    @Override
    public void commandError(String errorMessage) {
        runtimeError(errorMessage, 0);
    }

    @Override
    public void runtimeError(String errorMessage, Integer lineNumber) {
        System.out.print(IDEControl.RUNTIME_ERROR_TAG);
        if (lineNumber != null) {
            System.out.print("AT LINE " +
                    String.format("%03d", lineNumber));
        }
        System.out.println(": " + errorMessage);
    }
}
