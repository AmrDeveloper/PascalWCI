package backend.interpreter.executors;

import backend.interpreter.Executor;
import intermediate.ICodeNode;
import intermediate.icodeimpl.ICodeNodeTypeImpl;

import java.util.List;

import static intermediate.icodeimpl.ICodeNodeTypeImpl.TEST;

public class LoopExecutor extends StatementExecutor {

    public LoopExecutor(Executor executor) {
        super(executor);
    }

    @Override
    public Object execute(ICodeNode node) {
        boolean exitLoop = false;
        ICodeNode exprNode = null;
        List<ICodeNode> loopChildren = node.getChildren();

        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        StatementExecutor statementExecutor = new StatementExecutor(this);

        // Loop utils TEST expression value is true
        while(!exitLoop) {
            // count the loop statement itself
            ++executionCount;

            // Execute the children of the LOOP Node
            for(ICodeNode child : loopChildren) {
                ICodeNodeTypeImpl childType = (ICodeNodeTypeImpl) child.getType();

                // TEST node
                if(childType == TEST) {
                    if(exprNode == null) {
                        exprNode = child.getChildren().get(0);
                    }
                    exitLoop = (Boolean) expressionExecutor.execute(exprNode);
                }

                // Statement node
                else {
                    statementExecutor.execute(child);
                }

                // Exit if the TEST expression value is true
                if(exitLoop) break;
            }
        }

        return null;
    }
}
