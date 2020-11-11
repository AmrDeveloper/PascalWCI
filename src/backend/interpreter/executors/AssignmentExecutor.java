package backend.interpreter.executors;

import backend.interpreter.Executor;
import intermediate.ICodeNode;
import intermediate.SymbolTableEntry;
import message.Message;

import java.util.List;

import static intermediate.icodeimpl.ICodeKeyImpl.ID;
import static intermediate.icodeimpl.ICodeKeyImpl.LINE;
import static intermediate.symtabimpl.SymbolTableKeyImp.DATA_VALUE;
import static message.MessageType.ASSIGN;

public class AssignmentExecutor extends StatementExecutor{

    public AssignmentExecutor(Executor executor) {
        super(executor);
    }

    @Override
    public Object execute(ICodeNode node) {
        List<ICodeNode> children = node.getChildren();
        ICodeNode variableNode = children.get(0);
        ICodeNode expressionNode = children.get(1);

        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        Object value = expressionExecutor.execute(expressionNode);

        SymbolTableEntry variableId = (SymbolTableEntry) variableNode.getAttribute(ID);
        variableId.setAttribute(DATA_VALUE, value);

        sendMessage(node, variableId.getName(), value);

        ++executionCount;
        return null;
    }

    private void sendMessage(ICodeNode node, String variableName, Object value) {
        Object lineNumber = node.getAttribute(LINE);

        if(lineNumber != null) {
            sendMessage(new Message(ASSIGN, new Object[] {lineNumber, variableName, value}));
        }
    }
}
