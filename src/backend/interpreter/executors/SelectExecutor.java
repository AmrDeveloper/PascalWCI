package backend.interpreter.executors;

import backend.interpreter.Executor;
import intermediate.ICodeNode;

import java.util.HashMap;
import java.util.List;

import static intermediate.icodeimpl.ICodeKeyImpl.VALUE;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.STRING_CONSTANT;

public class SelectExecutor extends StatementExecutor {

    // Jump table cache: entry key is a SELECT node,
    //                   entry value is the jump table.
    // Jump table: entry key is a selection value,
    //             entry value is the branch statement.
    private static final HashMap<ICodeNode, HashMap<Object, ICodeNode>> jumpCache = new HashMap<>();

    public SelectExecutor(Executor executor) {
        super(executor);
    }

    @Override
    public Object execute(ICodeNode node) {

        // Is there already an entry for this SELECT node in the jump table cache ?
        // If not create a new jump table entry
        HashMap<Object, ICodeNode> jumpTable = jumpCache.get(node);
        if(jumpTable == null) {
            jumpTable = createJumpTable(node);
            jumpCache.put(node, jumpTable);
        }

        // Get the SELECT node's children
        List<ICodeNode> selectChildren = node.getChildren();
        ICodeNode exprNode = selectChildren.get(0);

        // Evaluate the SELECT expression
        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        Object selectValue = expressionExecutor.execute(exprNode);

        // If there is a selection, execute the SELECT_BRANCH's statement
        ICodeNode statementNode = jumpTable.get(selectValue);
        if(statementNode != null) {
            StatementExecutor statementExecutor = new StatementExecutor(this);
            statementExecutor.execute(statementNode);
        }

        ++executionCount;
        return null;
    }

    private HashMap<Object, ICodeNode> createJumpTable(ICodeNode node) {
        HashMap<Object, ICodeNode> jumpTable = new HashMap<>();

        //Loop over children that are SELECT_BRANCH nodes
        List<ICodeNode> selectChildren = node.getChildren();
        for(int i = 1 ; i < selectChildren.size() ; ++i) {
            ICodeNode branchNode = selectChildren.get(i);
            ICodeNode constantsNode = branchNode.getChildren().get(0);
            ICodeNode statementNode = branchNode.getChildren().get(1);

            // Loop over the constants children of the branch's CONSTANTS_NODE
            List<ICodeNode> constantsList = constantsNode.getChildren();
            for(ICodeNode constantNode : constantsList) {
                // Create a jump table entry
                // Convert a single-character string constant to a character.
                Object value = constantNode.getAttribute(VALUE);
                if(constantNode.getType() == STRING_CONSTANT) {
                    value = ((String) value).charAt(0);
                }
                jumpTable.put(value, statementNode);
            }
        }
        return jumpTable;
    }
}
