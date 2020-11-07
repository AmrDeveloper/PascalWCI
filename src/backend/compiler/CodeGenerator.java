package backend.compiler;

import backend.Backend;
import intermediate.ICode;
import intermediate.SymbolTable;
import intermediate.SymbolTableStack;
import message.Message;

import static message.MessageType.COMPILER_SUMMARY;

public class CodeGenerator extends Backend {

    @Override
    public void process(ICode iCode, SymbolTableStack symbolTableStack) throws Exception {
        long startTime = System.currentTimeMillis();
        float elapsedTime = (System.currentTimeMillis() - startTime) / 1000f;
        int instructionCount = 0;

        sendMessage(new Message(COMPILER_SUMMARY, new Number[] {
                instructionCount,
                elapsedTime
        }));
    }
}
