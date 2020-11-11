package backend.interpreter;

import backend.Backend;
import backend.interpreter.executors.StatementExecutor;
import intermediate.ICode;
import intermediate.ICodeNode;
import intermediate.SymbolTableStack;
import message.Message;

import static message.MessageType.INTERPRETER_SUMMARY;

public class Executor extends Backend {

    protected static int executionCount;
    protected static RuntimeErrorHandler errorHandler;

    static {
        errorHandler = new RuntimeErrorHandler();
    }

    public Executor() {

    }

    public Executor(Executor parent) {
        super();
    }

    @Override
    public void process(ICode iCode, SymbolTableStack symbolTableStack) throws Exception {
        this.iCode = iCode;
        this.symbolTable = symbolTableStack;

        long startTime = System.currentTimeMillis();

        ICodeNode rootNode = iCode.getRoot();
        StatementExecutor statementExecutor = new StatementExecutor(this);
        statementExecutor.execute(rootNode);

        float elapsedTime = (System.currentTimeMillis() - startTime) / 1000f;

        int runtimeErrors = errorHandler.getErrorCounter();

        sendMessage(new Message(INTERPRETER_SUMMARY, new Number[] {
                executionCount,
                runtimeErrors,
                elapsedTime
        }));
    }
}
