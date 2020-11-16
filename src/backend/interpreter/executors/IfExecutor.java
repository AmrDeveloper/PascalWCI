package backend.interpreter.executors;

import backend.interpreter.Executor;
import intermediate.ICodeNode;

import java.util.List;

public class IfExecutor extends StatementExecutor {

    public IfExecutor(Executor executor) {
        super(executor);
    }

    @Override
    public Object execute(ICodeNode node) {
        // Get the IF node's children
        List<ICodeNode> children = node.getChildren();
        ICodeNode exprNode = children.get(0);
        ICodeNode thenStmtNode = children.get(1);
        ICodeNode elseStmtNode = children.size() > 2 ? children.get(2) : null;

        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        StatementExecutor statementExecutor = new StatementExecutor(this);

        // Evaluate the expression to determine which statement to execute
        boolean condition = (Boolean) expressionExecutor.execute(exprNode);
        if (condition) {
            statementExecutor.execute(thenStmtNode);
        } else if (elseStmtNode != null) {
            statementExecutor.execute(elseStmtNode);
        }

        // count the IF statement itself
        ++executionCount;
        return null;
    }
}
