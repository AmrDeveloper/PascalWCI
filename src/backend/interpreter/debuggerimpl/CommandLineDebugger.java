package backend.interpreter.debuggerimpl;

import backend.Backend;
import backend.interpreter.Debugger;
import backend.interpreter.RuntimeStack;
import intermediate.Definition;
import intermediate.SymbolTableEntry;
import message.Message;

import java.util.List;

public class CommandLineDebugger extends Debugger {

    private CommandProcessor commandProcessor;

    public CommandLineDebugger(Backend backend, RuntimeStack runtimeStack) {
        super(backend, runtimeStack);
        commandProcessor = new CommandProcessor(this);
    }

    @Override
    public void processMessage(Message message) {
        commandProcessor.processMessage(message);
    }

    @Override
    public void promptForCommand() {
        System.out.print(">>> Command? ");
    }

    @Override
    public boolean parseCommand() {
        return commandProcessor.parseCommand();
    }

    @Override
    public void atStatement(Integer lineNumber) {
        System.out.println("\n>>> At line " + lineNumber);
    }

    @Override
    public void atBreakpoint(Integer lineNumber) {
        System.out.println("\n>>> Breakpoint at line " + lineNumber);
    }

    @Override
    public void atWatchpointValue(Integer lineNumber, String name, Object value) {
        System.out.println("\n>>> At line " + lineNumber + ": " + name + ": " + value.toString());
    }

    @Override
    public void atWatchpointAssignment(Integer lineNumber, String name, Object value) {
        System.out.println("\n>>> At line " + lineNumber + ": " + name + " := " + value.toString());
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
        for (Object item : stack) {
            // Name of procedure or function
            if (item instanceof SymbolTableEntry) {
                SymbolTableEntry routineId = (SymbolTableEntry) item;
                String routineName = routineId.getName();
                int level = routineId.getSymbolTable().getNestingLevel();
                Definition definition = routineId.getDefinition();

                System.out.println(level + ": " + definition.getText().toUpperCase() + " " + routineName);
            }
            // Variable name-value pair
            else if (item instanceof NameValuePair) {
                NameValuePair pair = (NameValuePair) item;
                System.out.print("  " + pair.getVariableName() + ": ");
                displayValue(pair.getValueString());
            }
        }
    }

    @Override
    public void quit() {
        System.out.println("Program terminated.");
        System.exit(-1);
    }

    @Override
    public void commandError(String errorMessage) {
        System.out.println("!!! ERROR: " + errorMessage);
    }

    @Override
    public void runtimeError(String errorMessage, Integer lineNumber) {
        System.out.print("!!! RUNTIME ERROR");
        if (lineNumber != null) {
            System.out.print(" at line " + String.format("%03d", lineNumber));
        }
        System.out.println(": " + errorMessage);
    }
}
