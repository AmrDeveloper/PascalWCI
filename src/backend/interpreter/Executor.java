package backend.interpreter;

import backend.Backend;
import intermediate.ICode;
import intermediate.SymbolTableStack;
import message.Message;

import static message.MessageType.INTERPRETER_SUMMARY;

public class Executor extends Backend {

    @Override
    public void process(ICode iCode, SymbolTableStack symbolTable) throws Exception {
        long startTime = System.currentTimeMillis();
        float elapsedTime = (System.currentTimeMillis() - startTime) / 1000f;
        int instructionCount = 0;
        int runtimeErrors = 0;

        sendMessage(new Message(INTERPRETER_SUMMARY, new Number[] {
                instructionCount,
                runtimeErrors,
                elapsedTime
        }));
    }
}
