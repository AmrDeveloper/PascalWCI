package backend.interpreter.executors;

import backend.interpreter.Cell;
import backend.interpreter.Executor;
import intermediate.ICodeNode;
import intermediate.SymbolTableEntry;
import intermediate.TypeSpec;
import intermediate.symtabimpl.Predefined;
import message.Message;

import java.util.List;

import static intermediate.icodeimpl.ICodeKeyImpl.ID;
import static intermediate.icodeimpl.ICodeKeyImpl.LINE;
import static intermediate.symtabimpl.SymbolTableKeyImp.DATA_VALUE;
import static intermediate.typeimpl.TypeKeyImpl.ARRAY_ELEMENT_COUNT;
import static message.MessageType.ASSIGN;

public class AssignmentExecutor extends StatementExecutor {

    public AssignmentExecutor(Executor executor) {
        super(executor);
    }

    @Override
    public Object execute(ICodeNode node) {
        List<ICodeNode> children = node.getChildren();
        ICodeNode variableNode = children.get(0);
        ICodeNode expressionNode = children.get(1);
        SymbolTableEntry variableId = (SymbolTableEntry) variableNode.getAttribute(ID);

        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        Cell targetCell = (Cell) expressionExecutor.executeVariable(variableNode);

        TypeSpec targetType = variableNode.getTypeSpec();
        TypeSpec valueType = expressionNode.getTypeSpec().baseType();
        Object value = expressionExecutor.execute(expressionNode);

        assignValue(node, variableId, targetCell, targetType, value, valueType);

        ++executionCount;
        return null;
    }

    protected void assignValue(ICodeNode node, SymbolTableEntry targetId,
                               Cell targetCell, TypeSpec targetType,
                               Object value, TypeSpec valueType) {
        // Range Check
        value = checkRange(node, targetType, value);

        // Set the target's value
        // Convert an integer value to real if necessary
        if((targetType == Predefined.realType)
                && (valueType == Predefined.integerType)) {
            targetCell.setValue(new Float(((Integer) value).intValue()));
        }

        // String assignment
        //   target length < value length: truncate the value
        //   target length > value length: blank pad the value
        else if (targetType.isPascalString()) {
            int targetLength = (Integer) targetType.getAttribute(ARRAY_ELEMENT_COUNT);
            int valueLength = (Integer) valueType.getAttribute(ARRAY_ELEMENT_COUNT);

            String stringValue = (String) value;

            // Truncate the value string.
            if(targetLength < valueLength) {
                stringValue = stringValue.substring(0, targetLength);
            }
            // Pad the value string with blanks at the right end
            else if(targetLength > valueLength) {
                StringBuilder buffer = new StringBuilder(stringValue);

                for(int i = valueLength ; i < targetLength; ++i) {
                    buffer.append(" ");
                }

                stringValue = buffer.toString();
            }

            targetCell.setValue(copyOf(toPascal(targetType, stringValue), node));
        }
        // Simple assignment
        else {
            targetCell.setValue(copyOf(toPascal(targetType, value), node));
        }

        sendAssignMessage(node, targetId.getName(), value);
    }
}
