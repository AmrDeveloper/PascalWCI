package backend.interpreter;

import backend.Backend;
import intermediate.ICodeNode;
import message.Message;
import message.MessageType;

import static intermediate.icodeimpl.ICodeKeyImpl.LINE;

public class RuntimeErrorHandler {

    private static final int MAX_ERRORS = 5;
    private static int errorCounter = 0;

    public void flag(ICodeNode node, RuntimeErrorCode errorCode, Backend backend) {
        String lineNum = null;

        while((node != null) && (node.getAttribute(LINE) == null)) {
            node = node.getParent();
        }

        backend.sendMessage(new Message(MessageType.RUNTIME_ERROR, new Object[]{
            errorCode,
            (Integer) node.getAttribute(LINE)
        }));

        if(++errorCounter > MAX_ERRORS) {
            System.out.println("*** ABORTED AFTER TOO MANY RUNTIME ERRORS.");
            System.exit(-1);
        }
    }

}
