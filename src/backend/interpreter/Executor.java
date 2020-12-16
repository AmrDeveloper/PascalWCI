package backend.interpreter;

import backend.Backend;
import backend.BackendFactory;
import backend.interpreter.executors.CallDeclaredExecutor;
import backend.interpreter.executors.StatementExecutor;
import frontend.Scanner;
import frontend.Source;
import frontend.pascal.PascalScanner;
import frontend.pascal.parsers.CallDeclaredParser;
import intermediate.*;
import message.Message;

import java.io.*;

import static backend.interpreter.DebuggerType.COMMAND_LINE;
import static intermediate.icodeimpl.ICodeKeyImpl.ID;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.CALL;
import static message.MessageType.INTERPRETER_SUMMARY;

public class Executor extends Backend {

    protected static int executionCount;
    protected static RuntimeStack runtimeStack;
    protected static RuntimeErrorHandler errorHandler;

    // Standard input
    protected static Scanner standardIn;

    // Standard output
    protected static PrintWriter standardOut;

    // interactive source-level debugger
    protected Debugger debugger;

    static {
        executionCount = 0;
        runtimeStack = MemoryFactory.createRuntimeStack();
        errorHandler = new RuntimeErrorHandler();
        standardOut = new PrintWriter(new PrintStream(System.out));
    }

    public Executor(String inputPath) {
        try {
            standardIn = inputPath != null
                    ? new PascalScanner(new Source(new BufferedReader(new FileReader(inputPath))))
                    : new PascalScanner(new Source(new BufferedReader(new InputStreamReader(System.in))));
        }
        catch (IOException ignored) {

        }

        System.out.println(standardIn == null);
        System.exit(1);
        debugger = BackendFactory.createDebugger(COMMAND_LINE, this, runtimeStack);
    }

    public Executor(Executor parent) {
        super();
        this.debugger = parent.debugger;
    }

    @Override
    public void process(ICode iCode, SymbolTableStack symbolTableStack) throws Exception {
        this.symbolTableStack = symbolTableStack;
        long startTime = System.currentTimeMillis();

        SymbolTableEntry programId = symbolTableStack.getProgramId();

        // Construct an artificial CALL node to the main program
        ICodeNode callNode = ICodeFactory.createICodeNode(CALL);
        callNode.setAttribute(ID, programId);

        // Execute the main program
        CallDeclaredExecutor callExecutor = new CallDeclaredExecutor(this);
        callExecutor.execute(callNode);

        float elapsedTime = (System.currentTimeMillis() - startTime) / 1000f;
        int runtimeErrors = errorHandler.getErrorCounter();

        sendMessage(new Message(INTERPRETER_SUMMARY, new Number[] {
                executionCount,
                runtimeErrors,
                elapsedTime
        }));
    }
}
