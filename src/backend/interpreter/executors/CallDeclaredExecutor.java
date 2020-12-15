package backend.interpreter.executors;

import backend.interpreter.ActivationRecord;
import backend.interpreter.Cell;
import backend.interpreter.Executor;
import backend.interpreter.MemoryFactory;
import intermediate.*;

import java.util.List;

import static intermediate.icodeimpl.ICodeKeyImpl.ID;
import static intermediate.symtabimpl.DefinitionImpl.VALUE_PARM;
import static intermediate.symtabimpl.SymbolTableKeyImp.ROUTINE_ICODE;
import static intermediate.symtabimpl.SymbolTableKeyImp.ROUTINE_PARMS;

public class CallDeclaredExecutor extends CallExecutor {

    public CallDeclaredExecutor(Executor executor) {
        super(executor);
    }

    /*
     * Execute a call to a declared procedure or function
     */
    public Object execute(ICodeNode node) {
        SymbolTableEntry routineId = (SymbolTableEntry) node.getAttribute(ID);
        ActivationRecord newActivationRecord = MemoryFactory.createActivationRecord(routineId);

        // Execute any actual parameters and initialize
        // the formal parameters in the new activation record
        if (node.getChildren().size() > 0) {
            ICodeNode parmsNode = node.getChildren().get(0);
            List<ICodeNode> actualNodes = parmsNode.getChildren();
            List<SymbolTableEntry> formalIds = (List<SymbolTableEntry>) routineId.getAttribute(ROUTINE_PARMS);
            executeActualParms(actualNodes, formalIds, newActivationRecord);
        }

        // Push the new activation record
        runtimeStack.push(newActivationRecord);

        sendCallMessage(node, routineId.getName());

        // Get the root node of the routine's intermediate code
        ICode iCode = (ICode) routineId.getAttribute(ROUTINE_ICODE);
        ICodeNode rootNode = iCode.getRoot();

        // Execute the routine
        StatementExecutor statementExecutor = new StatementExecutor(this);
        Object value = statementExecutor.execute(rootNode);

        // Pop off the activation record
        runtimeStack.pop();

        sendReturnMessage(node, routineId.getName());
        return value;
    }

    /*
     * Execute the actual parameters of a call.
     */
    private void executeActualParms(List<ICodeNode> actualNodes,
                                    List<SymbolTableEntry> formalIds,
                                    ActivationRecord newActivationRecord) {
        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        AssignmentExecutor assignmentExecutor = new AssignmentExecutor(this);

        for (int i = 0; i < formalIds.size(); ++i) {
            SymbolTableEntry formalId = formalIds.get(i);
            Definition formalDefinition = formalId.getDefinition();
            Cell formalCell = newActivationRecord.getCell(formalId.getName());
            ICodeNode actualNode = actualNodes.get(i);

            // Value parameter
            if (formalDefinition == VALUE_PARM) {
                TypeSpec formalType = formalId.getTypeSpec();
                TypeSpec valueType = actualNode.getTypeSpec().baseType();
                Object value = expressionExecutor.execute(actualNode);

                assignmentExecutor.assignValue(actualNode, formalId,
                        formalCell, formalType,
                        value, valueType);
            }
            // Var parameter
            else {
                Cell actualCell = (Cell) expressionExecutor.executeVariable(actualNode);
                formalCell.setValue(actualCell);
            }
        }
    }
}
