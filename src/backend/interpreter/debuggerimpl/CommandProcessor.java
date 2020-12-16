package backend.interpreter.debuggerimpl;

import backend.interpreter.ActivationRecord;
import backend.interpreter.Cell;
import backend.interpreter.Debugger;
import backend.interpreter.RuntimeStack;
import intermediate.SymbolTable;
import intermediate.SymbolTableEntry;
import message.Message;
import message.MessageType;

import java.util.ArrayList;
import java.util.List;

import static frontend.pascal.PascalTokenType.SEMICOLON;
import static intermediate.symtabimpl.SymbolTableKeyImp.ROUTINE_SYMTAB;

public class CommandProcessor {

    // the debugger
    private Debugger debugger;

    // true when single stepping
    private boolean stepping;

    public CommandProcessor(Debugger debugger) {
        this.debugger = debugger;
        this.stepping = true;
    }

    protected void processMessage(Message message) {
        MessageType type = message.getType();

        switch (type) {
            case SOURCE_LINE: {
                int lineNumber = (Integer) message.getBody();

                if (stepping) {
                    debugger.atStatement(lineNumber);
                    debugger.readCommands();
                } else if (debugger.isBreakpoint(lineNumber)) {
                    debugger.atBreakpoint(lineNumber);
                    debugger.readCommands();
                }
                break;
            }
            case FETCH: {
                Object[] body = (Object[]) message.getBody();
                String variableName = ((String) body[1]).toLowerCase();

                if (debugger.isWatchpoint(variableName)) {
                    int lineNumber = (Integer) body[0];
                    Object value = body[2];
                    debugger.atWatchpointValue(lineNumber, variableName, value);
                }
                break;
            }
            case ASSIGN: {
                Object[] body = (Object[]) message.getBody();
                String variableName = ((String) body[1]).toLowerCase();

                if (debugger.isWatchpoint(variableName)) {
                    int lineNumber = (Integer) body[0];
                    Object value = body[2];

                    debugger.atWatchpointAssignment(lineNumber, variableName, value);
                }
                break;
            }
            case CALL: {
                Object[] body = (Object[]) message.getBody();
                int lineNumber = (Integer) body[0];
                String routineName = (String) body[1];

                debugger.callRoutine(lineNumber, routineName);
                break;
            }
            case RETURN: {
                Object[] body = (Object[]) message.getBody();
                int lineNumber = (Integer) body[0];
                String routineName = (String) body[1];

                debugger.returnRoutine(lineNumber, routineName);
                break;
            }
            case RUNTIME_ERROR: {
                Object[] body = (Object[]) message.getBody();
                String errorMessage = (String) body[0];
                Integer lineNumber = (Integer) body[1];

                debugger.runtimeError(errorMessage, lineNumber);
                break;
            }
        }
    }

    protected boolean parseCommand() {
        boolean anotherCommand = true;

        // Parse a command
        try {
            debugger.nextToken();
            String command = debugger.getWord("Command expected");
            anotherCommand = executeCommand(command);
        } catch (Exception ex) {
            debugger.commandError(ex.getMessage());
        }

        // Skip to the next command
        try {
            debugger.skipToNextCommand();
        } catch (Exception ex) {
            debugger.commandError(ex.getMessage());
        }

        return anotherCommand;
    }

    private boolean executeCommand(String command) throws Exception {
        stepping = false;

        switch (command) {
            case "step": {
                stepping = true;
                checkForSemicolon();
                return false;
            }
            case "break": {
                Integer lineNumber = debugger.getInteger("Line number expected");
                checkForSemicolon();
                debugger.setBreakpoint(lineNumber);
                return true;
            }
            case "unbreak": {
                Integer lineNumber = debugger.getInteger("Line number expected");
                checkForSemicolon();
                debugger.unsetBreakpoint(lineNumber);
                return true;
            }
            case "watch": {
                String name = debugger.getWord("Variable name expcted");
                checkForSemicolon();
                debugger.setWatchpoint(name);
                return true;
            }
            case "unwatch": {
                String name = debugger.getWord("Variable name expcted");
                checkForSemicolon();
                debugger.unsetWatchpoint(name);
                return true;
            }
            case "stack": {
                checkForSemicolon();
                stack();
                return true;
            }
            case "show": {
                show();
                return true;
            }
            case "assign": {
                assign();
                return true;
            }
            case "go": {
                checkForSemicolon();
                return false;
            }
            case "quit": {
                checkForSemicolon();
                debugger.quit();
            }
            default: {
                throw new Exception("Invalid commands: '" + command + "'");
            }
        }
    }

    private void stack() {
        List callStack = new ArrayList();

        // Loop over the activation records on the runtime stack
        // starting at the top of stack
        RuntimeStack runtimeStack = debugger.getRuntimeStack();
        List<ActivationRecord> activationRecordList = runtimeStack.records();
        for (int i = activationRecordList.size() - 1; i >= 0; --i) {
            ActivationRecord activationRecord = activationRecordList.get(i);
            SymbolTableEntry routineId = activationRecord.getRoutineId();

            // Add the symbol table entry of the procedure or function
            callStack.add(routineId);

            // Create and add a name-value pair for each local variable.
            for (String name : activationRecord.getAllNames()) {
                Object value = activationRecord.getCell(name).getValue();
                callStack.add(new NameValuePair(name, value));
            }
        }

        // Display the call stack
        debugger.displayCallStack(callStack);
    }

    private void show() throws Exception {
        CellTypePair pair = createCellTypePair();
        Cell cell = pair.getCell();

        checkForSemicolon();
        debugger.displayValue(NameValuePair.valueString(cell.getValue()));
    }

    private void assign() throws Exception {
        CellTypePair pair = createCellTypePair();
        Object newValue = debugger.getValue("Invalid value.");

        checkForSemicolon();
        pair.setValue(newValue);
    }

    private CellTypePair createCellTypePair() throws Exception {
        RuntimeStack runtimeStack = debugger.getRuntimeStack();
        int currentLevel = runtimeStack.currentNestingLevel();
        ActivationRecord activationRecord = null;
        Cell cell = null;

        // Parse the variable name.
        String variableName = debugger.getWord("Variable Name expected.");

        // Find the variable's cell in the call stack.
        for (int level = currentLevel; (cell == null) && (level > 0); --level) {
            activationRecord = runtimeStack.getTopmost(level);
            cell = activationRecord.getCell(variableName);
        }

        if(cell == null) {
            throw new Exception("Undeclared variable name '" + variableName + "'");
        }

        // VAR parameter
        if(cell.getValue() instanceof Cell) {
            cell = (Cell) cell.getValue();
        }

        SymbolTable symbolTable = (SymbolTable) activationRecord.getRoutineId().getAttribute(ROUTINE_SYMTAB);
        SymbolTableEntry id = symbolTable.lookup(variableName);

        return new CellTypePair(id.getTypeSpec(), cell, debugger);
    }

    private void checkForSemicolon() throws Exception {
        if (debugger.currentToken().getType() != SEMICOLON) {
            throw new Exception("Invalid command syntax.");
        }
    }
}
