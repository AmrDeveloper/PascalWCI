package backend.interpreter.executors;

import backend.BackendFactory;
import backend.interpreter.ActivationRecord;
import backend.interpreter.Cell;
import backend.interpreter.Executor;
import intermediate.*;
import intermediate.icodeimpl.ICodeNodeTypeImpl;
import intermediate.symtabimpl.Predefined;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import static backend.interpreter.RuntimeErrorCode.DIVISION_BY_ZERO;
import static backend.interpreter.RuntimeErrorCode.UNINITIALIZED_VALUE;
import static intermediate.icodeimpl.ICodeKeyImpl.ID;
import static intermediate.icodeimpl.ICodeKeyImpl.VALUE;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static intermediate.symtabimpl.RoutineCodeImpl.DECLARED;
import static intermediate.symtabimpl.SymbolTableKeyImp.DATA_VALUE;
import static intermediate.symtabimpl.SymbolTableKeyImp.ROUTINE_CODE;
import static intermediate.typeimpl.TypeFormImpl.SUBRANGE;
import static intermediate.typeimpl.TypeKeyImpl.*;

public class ExpressionExecutor extends StatementExecutor {

    // Set of arithmetic operator node types.
    private static final EnumSet<ICodeNodeTypeImpl> ARITH_OPS = EnumSet.of(ADD, SUBTRACT, MULTIPLY, FLOAT_DIVIDE, INTEGER_DIVIDE);

    public ExpressionExecutor(Executor executor) {
        super(executor);
    }

    @Override
    public Object execute(ICodeNode node) {
        ICodeNodeTypeImpl nodeType = (ICodeNodeTypeImpl) node.getType();
        switch (nodeType) {
            case VARIABLE: {
                //SymbolTableEntry entry = (SymbolTableEntry) node.getAttribute(ID);
                //return entry.getAttribute(DATA_VALUE);
                return executeValue(node);
            }
            case INTEGER_CONSTANT: {
                TypeSpec type = node.getTypeSpec();
                Integer value = (Integer) node.getAttribute(VALUE);
                return type == Predefined.booleanType ? value == 1 : value;
            }
            case REAL_CONSTANT: {
                return (Float) node.getAttribute(VALUE);
            }
            case STRING_CONSTANT: {
                return (String) node.getAttribute(VALUE);
            }
            case NEGATE: {
                List<ICodeNode> children = node.getChildren();
                ICodeNode expressionNode = children.get(0);

                Object value = execute(expressionNode);
                if (value instanceof Integer) {
                    return -((Integer) value);
                } else {
                    return -((Float) value);
                }
            }
            case NOT: {
                List<ICodeNode> children = node.getChildren();
                ICodeNode expressionNode = children.get(0);

                boolean value = (Boolean) execute(expressionNode);
                return !value;
            }
            case CALL: {
                //Execute a function call
                SymbolTableEntry functionId = (SymbolTableEntry) node.getAttribute(ID);
                RoutineCode routineCode = (RoutineCode) functionId.getAttribute(ROUTINE_CODE);
                CallExecutor callExecutor = new CallExecutor(this);
                Object value = callExecutor.execute(node);

                // If it was a declared function, obtain the function value
                // form its name
                if (routineCode == DECLARED) {
                    String functionName = functionId.getName();
                    int nestingLevel = functionId.getSymbolTable().getNestingLevel();
                    ActivationRecord activationRecord = runtimeStack.getTopmost(nestingLevel);
                    Cell functionValueCell = activationRecord.getCell(functionName);
                    value = functionValueCell.getValue();
                    sendFetchMessage(node, functionId.getName(), value);
                }

                // Return the function value
                return value;
            }
            default:
                return executeBinaryOperator(node, nodeType);
        }
    }

    private Object executeValue(ICodeNode node) {
        SymbolTableEntry variableId = (SymbolTableEntry) node.getAttribute(ID);
        String variableName = variableId.getName();
        TypeSpec variableType = variableId.getTypeSpec();

        // Get the variable's value
        Cell variableCell = executeVariable(node);
        Object value = variableCell.getValue();

        if (value != null) {
            value = toJava(variableType, value);
        }

        // Uninitialized value error: User a default value
        else {
            errorHandler.flag(node, UNINITIALIZED_VALUE, this);

            value = BackendFactory.defaultValue(variableType);
            variableCell.setValue(value);
        }

        sendFetchMessage(node, variableName, value);
        return value;
    }

    // Execute a variable and return the reference to its cell.
    public Cell executeVariable(ICodeNode node) {
        SymbolTableEntry variableId = (SymbolTableEntry) node.getAttribute(ID);
        String variableName = variableId.getName();
        TypeSpec variableType = variableId.getTypeSpec();
        int nestingLevel = variableId.getSymbolTable().getNestingLevel();

        // Get the variable reference from the appropriate activation record.
        ActivationRecord activationRecord = runtimeStack.getTopmost(nestingLevel);
        Cell variableCell = activationRecord.getCell(variableName);

        List<ICodeNode> modifiers = node.getChildren();

        // Reference to a reference: User the original reference
        if (variableCell.getValue() instanceof Cell) {
            variableCell = (Cell) variableCell.getValue();
        }

        // Execute any array subscripts or record fields.
        for (ICodeNode modifier : modifiers) {
            ICodeNodeType nodeType = modifier.getType();

            // Subscripts
            if (nodeType == SUBSCRIPTS) {
                List<ICodeNode> subscripts = modifier.getChildren();

                // Compute a reference for each subscript
                for (ICodeNode subscript : subscripts) {
                    TypeSpec indexType = (TypeSpec) variableType.getAttribute(ARRAY_INDEX_TYPE);
                    int minIndex = indexType.getForm() == SUBRANGE
                            ? (Integer) indexType.getAttribute(SUBRANGE_MIN_VALUE)
                            : 0;
                    int value = (Integer) execute(subscript);
                    value = (Integer) checkRange(node, indexType, value);

                    int index = value - minIndex;
                    variableCell = ((Cell[]) variableCell.getValue())[index];
                    variableType = (TypeSpec) variableType.getAttribute(ARRAY_ELEMENT_TYPE);
                }
            }
            // Field
            else if (nodeType == FIELD) {
                SymbolTableEntry fieldId = (SymbolTableEntry) modifier.getAttribute(ID);
                String fieldName = fieldId.getName();

                // Compute a new reference for the field
                HashMap<String, Cell> map = (HashMap<String, Cell>) variableCell.getValue();
                variableCell = map.get(fieldName);
                variableType = fieldId.getTypeSpec();
            }
        }

        return variableCell;
    }

    private Object executeBinaryOperator(ICodeNode node, ICodeNodeTypeImpl nodeType) {
        List<ICodeNode> children = node.getChildren();

        ICodeNode operandNode1 = children.get(0);
        ICodeNode operandNode2 = children.get(1);

        Object operand1 = execute(operandNode1);
        Object operand2 = execute(operandNode2);

        boolean integerMode = false;
        boolean characterMode = false;
        boolean stringMode = false;

        if ((operand1 instanceof Integer) && (operand2 instanceof Integer)) {
            integerMode = true;
        } else if (((operand1 instanceof Character) ||
                ((operand1 instanceof String) &&
                        (((String) operand1).length() == 1))
        ) &&
                ((operand2 instanceof Character) ||
                        ((operand2 instanceof String) &&
                                (((String) operand2).length() == 1))
                )
        ) {
            characterMode = true;
        } else if ((operand1 instanceof String) && (operand2 instanceof String)) {
            stringMode = true;
        }

        // Arithmetic operators
        if (ARITH_OPS.contains(nodeType)) {
            if (integerMode) {
                int value1 = (Integer) operand1;
                int value2 = (Integer) operand2;

                switch (nodeType) {
                    case ADD:
                        return value1 + value2;
                    case SUBTRACT:
                        return value1 - value2;
                    case MULTIPLY:
                        return value1 * value2;

                    case FLOAT_DIVIDE: {
                        if (value2 != 0) {
                            return ((float) value1 / (float) value2);
                        } else {
                            errorHandler.flag(node, DIVISION_BY_ZERO, this);
                            return 0;
                        }
                    }
                    case INTEGER_DIVIDE: {
                        if (value2 != 0) {
                            return value1 / value2;
                        } else {
                            errorHandler.flag(node, DIVISION_BY_ZERO, this);
                            return 0;
                        }
                    }
                    case MOD: {
                        if (value2 != 0) {
                            return value1 % value2;
                        } else {
                            errorHandler.flag(node, DIVISION_BY_ZERO, this);
                            return 0;
                        }
                    }
                }
            } else {
                float value1 = operand1 instanceof Integer ? (Integer) operand1 : (Float) operand1;
                float value2 = operand2 instanceof Integer ? (Integer) operand2 : (Float) operand2;

                switch (nodeType) {
                    case ADD:
                        return value1 + value2;
                    case SUBTRACT:
                        return value1 - value2;
                    case MULTIPLY:
                        return value1 * value2;
                    case FLOAT_DIVIDE: {
                        if (value2 != 0.0f) {
                            return value1 / value2;
                        } else {
                            errorHandler.flag(node, DIVISION_BY_ZERO, this);
                            return 0;
                        }
                    }
                }
            }
        }
        //AND and OR Operators
        else if ((nodeType == AND) || (nodeType == OR)) {
            boolean value1 = (Boolean) operand1;
            boolean value2 = (Boolean) operand2;

            switch (nodeType) {
                case AND:
                    return value1 && value2;
                case OR:
                    return value1 || value2;
            }
        }
        // Relational Operators
        else if (integerMode) {
            int value1 = (Integer) operand1;
            int value2 = (Integer) operand2;

            switch (nodeType) {
                case EQ:
                    return value1 == value2;
                case NE:
                    return value1 != value2;
                case LT:
                    return value1 < value2;
                case LE:
                    return value1 <= value2;
                case GT:
                    return value1 > value2;
                case GE:
                    return value1 >= value2;
            }
        } else if (characterMode) {
            int value1 = operand1 instanceof Character
                    ? (Character) operand1
                    : ((String) operand1).charAt(0);
            int value2 = operand2 instanceof Character
                    ? (Character) operand2
                    : ((String) operand2).charAt(0);

            // Character operands.
            switch (nodeType) {
                case EQ:
                    return value1 == value2;
                case NE:
                    return value1 != value2;
                case LT:
                    return value1 < value2;
                case LE:
                    return value1 <= value2;
                case GT:
                    return value1 > value2;
                case GE:
                    return value1 >= value2;
            }
        } else if (stringMode) {
            String value1 = (String) operand1;
            String value2 = (String) operand2;

            // String operands.
            int comp = value1.compareTo(value2);
            switch (nodeType) {
                case EQ:
                    return comp == 0;
                case NE:
                    return comp != 0;
                case LT:
                    return comp < 0;
                case LE:
                    return comp <= 0;
                case GT:
                    return comp > 0;
                case GE:
                    return comp >= 0;
            }
        } else {
            float value1 = (operand1 instanceof Integer) ? (Integer) operand1 : (Float) operand1;
            float value2 = (operand2 instanceof Integer) ? (Integer) operand2 : (Float) operand2;

            switch (nodeType) {
                case EQ:
                    return value1 == value2;
                case NE:
                    return value1 != value2;
                case LT:
                    return value1 < value2;
                case LE:
                    return value1 <= value2;
                case GT:
                    return value1 > value2;
                case GE:
                    return value1 >= value2;
            }
        }

        // should never get here
        return 0;
    }
}
