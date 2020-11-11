package backend.interpreter.executors;

import backend.interpreter.Executor;
import intermediate.ICodeNode;
import intermediate.icodeimpl.ICodeNodeTypeImpl;
import message.Message;
import message.MessageType;

import static backend.interpreter.RuntimeErrorCode.UNIMPLEMENTED_FEATURE;
import static intermediate.icodeimpl.ICodeKeyImpl.LINE;

public class StatementExecutor extends Executor{

    public StatementExecutor(Executor executor) {
        super(executor);
    }

    public Object execute(ICodeNode node) {
        ICodeNodeTypeImpl nodeType = (ICodeNodeTypeImpl) node.getType();

        //ignore this message for now but later use it on debugger and tracking
        sendSourceLineMessage(node);

        switch (nodeType) {
            case COMPOUND: {
                CompoundExecutor compoundExecutor = new CompoundExecutor(this);
                return compoundExecutor.execute(node);
            }
            case ASSIGN: {
                AssignmentExecutor assignmentExecutor = new AssignmentExecutor(this);
                return assignmentExecutor.execute(node);
            }

            case NO_OP: {
                return null;
            }
            default: {
                errorHandler.flag(node, UNIMPLEMENTED_FEATURE, this);
                return null;
            }
        }
    }

    private void sendSourceLineMessage(ICodeNode node) {
        Object lineNumber = node.getAttribute(LINE);

        if(lineNumber != null) {
            sendMessage(new Message(MessageType.SOURCE_LINE, lineNumber));
        }
    }
}
