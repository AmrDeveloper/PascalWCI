package backend.interpreter.executors;

import backend.interpreter.Executor;
import intermediate.ICodeNode;

import java.util.List;

import static intermediate.icodeimpl.ICodeKeyImpl.VALUE;

public class SelectExecutor extends StatementExecutor{

    public SelectExecutor(Executor executor) {
        super(executor);
    }

    @Override
    public Object execute(ICodeNode node) {
        // Get the SELECT node's children
        List<ICodeNode> selectChildren = node.getChildren();
        ICodeNode exprNode = selectChildren.get(0);

        // Evaluate the SELECT expression
        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        Object selectValue = expressionExecutor.execute(exprNode);

        // Attempt to select a SELECT_BRANCH
        ICodeNode selectBranchNode = searchBranches(selectValue, selectChildren);

        // if there was a selection, execute the SELECT_BRANCH statement
        if(selectBranchNode != null) {
            ICodeNode stmtNode = selectBranchNode.getChildren().get(1);
            StatementExecutor statementExecutor = new StatementExecutor(this);
            statementExecutor.execute(stmtNode);
        }

        // count the SELECT statement itself
        ++executionCount;
        return null;
    }

    private ICodeNode searchBranches(Object selectValue, List<ICodeNode> selectChildren) {
        // Loop over the SELECT_BRANCH\s to find a match
        for(int i = 1 ; i < selectChildren.size() ; ++i) {
            ICodeNode branchNode = selectChildren.get(1);

            if(searchConstants(selectValue, branchNode)) {
                return branchNode;
            }
        }

        return null;
    }

    private boolean searchConstants(Object selectValue, ICodeNode branchNode) {
        // Are the values integer or string
        boolean integerMode = selectValue instanceof Integer;

        // Get the list of SELECT_CONSTANTS values
        ICodeNode constantsNode = branchNode.getChildren().get(0);
        List<ICodeNode> constantsList = constantsNode.getChildren();

        // Search the list of constants
        if(integerMode) {
            for(ICodeNode constantNode : constantsList) {
                int constant = (Integer) constantNode.getAttribute(VALUE);

                if(((Integer) selectValue) == constant) {
                    return true;
                }
            }
        }
        else {
            for(ICodeNode constantNode : constantsList) {
                String constant = (String) constantNode.getAttribute(VALUE);

                if(((String) selectValue).equals(constant)) {
                    return true;
                }
            }
        }

        // no match
        return false;
    }
}
