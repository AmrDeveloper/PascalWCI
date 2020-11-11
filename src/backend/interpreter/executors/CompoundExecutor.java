package backend.interpreter.executors;

import backend.interpreter.Executor;
import intermediate.ICodeNode;

import java.util.List;

public class CompoundExecutor extends StatementExecutor{

    public CompoundExecutor(Executor executor) {
        super(executor);
    }

    @Override
    public Object execute(ICodeNode node) {
        StatementExecutor statementExecutor = new StatementExecutor(this);
        List<ICodeNode> children = node.getChildren();
        for(ICodeNode child : children) {
            statementExecutor.execute(child);
        }
        return null;
    }
}
