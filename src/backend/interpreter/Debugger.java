package backend.interpreter;

import backend.Backend;
import frontend.Scanner;
import frontend.Source;
import frontend.Token;
import frontend.TokenType;
import frontend.pascal.PascalScanner;
import frontend.pascal.PascalTokenType;
import message.Message;
import message.MessageListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static frontend.pascal.PascalTokenType.*;

public abstract class Debugger {

    // runtime stack
    private RuntimeStack runtimeStack;

    // set of breakpoints
    private Set<Integer> breakpoints;

    // set of watchpoints
    private Set<String> watchpoints;

    // input source for commands
    private Scanner commandInput;

    public Debugger(Backend backend, RuntimeStack runtimeStack) {
        this.runtimeStack = runtimeStack;

        backend.addMessageListener(new BackendMessageListener());

        breakpoints = new HashSet<>();
        watchpoints = new HashSet<>();

        // Create the command input from the standard input
        try {
            commandInput = new PascalScanner(new Source(new BufferedReader(new InputStreamReader(System.in))));
        } catch (IOException ignore) {

        }
    }

    private class BackendMessageListener implements MessageListener {

        @Override
        public void messageReceived(Message message) {
            processMessage(message);
        }
    }

    public void readCommands() {
        do {
            promptForCommand();
        } while (parseCommand());
    }

    public Token currentToken() throws Exception {
        return commandInput.currentToken();
    }

    public Token nextToken() throws Exception {
        return commandInput.nextToken();
    }

    public String getWord(String errorMessage) throws Exception {
        Token token = currentToken();
        TokenType type = token.getType();

        if (type == IDENTIFIER) {
            String word = token.getText().toLowerCase();
            nextToken();
            return word;
        } else {
            throw new Exception(errorMessage);
        }
    }

    public Integer getInteger(String errorMessage) throws Exception {
        Token token = currentToken();
        TokenType type = token.getType();

        if (type == INTEGER) {
            Integer value = (Integer) token.getValue();
            nextToken();
            return value;
        } else {
            throw new Exception(errorMessage);
        }
    }

    public Object getValue(String errorMessage) throws Exception {
        Token token = currentToken();
        TokenType tokenType = token.getType();
        boolean sign = false;
        boolean minus = false;

        // Unary plus or minus sign
        if ((tokenType == MINUS) | (tokenType == PLUS)) {
            sign = true;
            minus = tokenType == MINUS;
            token = nextToken();
            tokenType = token.getType();
        }

        switch ((PascalTokenType) tokenType) {
            case INTEGER: {
                Integer value = (Integer) token.getValue();
                nextToken();
                return minus ? -value : value;
            }
            case REAL: {
                Float value = (Float) token.getValue();
                nextToken();
                return minus ? -value : value;
            }
            case STRING: {
                if(sign) {
                    throw new Exception(errorMessage);
                }
                else {
                    String value = (String) token.getValue();
                    nextToken();
                    return value.charAt(0);
                }
            }
            case IDENTIFIER: {
                if (sign) {
                    throw new Exception(errorMessage);
                }
                else {
                    String name = token.getText();
                    nextToken();

                    if("true".equalsIgnoreCase(name)) {
                        return (Boolean) true;
                    }
                    else if("false".equalsIgnoreCase(name)) {
                        return (Boolean) false;
                    }
                    else {
                        throw new Exception(errorMessage);
                    }
                }
            }
            default: {
                throw new Exception(errorMessage);
            }
        }
    }

    public void skipToNextCommand() throws Exception {
        commandInput.skipToNextLine();
    }

    public void setBreakpoint(Integer lineNumber) {
        breakpoints.add(lineNumber);
    }

    public void unsetBreakpoint(Integer lineNumber) {
        breakpoints.remove(lineNumber);
    }

    public boolean isBreakpoint(Integer lineNumber) {
        return breakpoints.contains(lineNumber);
    }

    public void setWatchpoint(String name) {
        watchpoints.add(name);
    }

    public void unsetWatchpoint(String name) {
        watchpoints.remove(name);
    }

    public boolean isWatchpoint(String name) {
        return watchpoints.contains(name);
    }

    public abstract void processMessage(Message message);

    public abstract void promptForCommand();

    public abstract boolean parseCommand();

    public RuntimeStack getRuntimeStack() {
        return runtimeStack;
    }

    /*
     * Process a source statement.
     */
    public abstract void atStatement(Integer lineNumber);

    /*
     * Process a breakpoint at a statement.
     */
    public abstract void atBreakpoint(Integer lineNumber);

    /*
     * Process the current value of a watchpoint variable.
     */
    public abstract void atWatchpointValue(Integer lineNumber, String name, Object value);

    /*
     * Process the assigning a new value to a watchpoint variable.
     */
    public abstract void atWatchpointAssignment(Integer lineNumber, String name, Object value);

    /*
     * Process calling a declared procedure or function.
     */
    public abstract void callRoutine(Integer lineNumber, String routineName);

    /*
     * Process returning from a declared procedure or function.
     */
    public abstract void returnRoutine(Integer lineNumber, String routineName);

    /*
     * Display a value
     */
    public abstract void displayValue(String valueString);

    /*
     * Display the call stack.
     */
    public abstract void displayCallStack(List stack);

    /*
     * Terminate execution of the source program.
     */
    public abstract void quit();

    /*
     * Handle a debugger command error.
     */
    public abstract void commandError(String errorMessage);

    /*
     * Handle a source program runtime error.
     */
    public abstract void runtimeError(String errorMessage, Integer lineNumber);

}
