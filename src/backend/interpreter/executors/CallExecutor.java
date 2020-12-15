package backend.interpreter.executors;

import backend.interpreter.Executor;
import intermediate.ICodeNode;
import intermediate.RoutineCode;
import intermediate.SymbolTableEntry;

import static intermediate.icodeimpl.ICodeKeyImpl.ID;
import static intermediate.symtabimpl.RoutineCodeImpl.DECLARED;
import static intermediate.symtabimpl.SymbolTableKeyImp.ROUTINE_CODE;

public class CallExecutor extends StatementExecutor{

    public CallExecutor(Executor executor) {
        super(executor);
    }

    public Object execute(ICodeNode node) {
        SymbolTableEntry routineId = (SymbolTableEntry) node.getAttribute(ID);
        RoutineCode routineCode = (RoutineCode) routineId.getAttribute(ROUTINE_CODE);
        CallExecutor callExecutor = routineCode == DECLARED
                ? new CallDeclaredExecutor(this)
                : new CallStandardExecutor(this);

        // count the call statement
        ++executionCount;
        return callExecutor.execute(node);
    }
}
