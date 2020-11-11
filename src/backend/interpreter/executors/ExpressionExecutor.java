package backend.interpreter.executors;

import backend.interpreter.Executor;
import intermediate.ICodeNode;
import intermediate.SymbolTableEntry;
import intermediate.icodeimpl.ICodeNodeTypeImpl;

import java.util.EnumSet;
import java.util.List;

import static backend.interpreter.RuntimeErrorCode.DIVISION_BY_ZERO;
import static intermediate.icodeimpl.ICodeKeyImpl.ID;
import static intermediate.icodeimpl.ICodeKeyImpl.VALUE;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static intermediate.symtabimpl.SymbolTableKeyImp.DATA_VALUE;

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
                SymbolTableEntry entry = (SymbolTableEntry) node.getAttribute(ID);
                return entry.getAttribute(DATA_VALUE);
            }
            case INTEGER_CONSTANT: {
                 return (Integer) node.getAttribute(VALUE);
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
                 if(value instanceof Integer) {
                     return -((Integer) value);
                 }else{
                     return -((Float) value);
                 }
            }
            case NOT: {
                List<ICodeNode> children = node.getChildren();
                ICodeNode expressionNode = children.get(0);

                boolean value = (Boolean) execute(expressionNode);
                return !value;
            }
            default:
                return executeBinaryOperator(node, nodeType);
        }
    }

    private Object executeBinaryOperator(ICodeNode node, ICodeNodeTypeImpl nodeType) {
        List<ICodeNode> children = node.getChildren();

        ICodeNode operandNode1 = children.get(0);
        ICodeNode operandNode2 = children.get(1);

        Object operand1 = execute(operandNode1);
        Object operand2 = execute(operandNode2);

        boolean integerMode = (operand1 instanceof Integer)
                && (operand2 instanceof Integer);

        // Arithmetic operators
        if(ARITH_OPS.contains(nodeType)) {
            if(integerMode) {
                int value1 = (Integer) operand1;
                int value2 = (Integer) operand2;

                switch (nodeType) {
                    case ADD: return value1 + value2;
                    case SUBTRACT: return value1 - value2;
                    case MULTIPLY: return value1 * value2;

                    case FLOAT_DIVIDE: {
                        if(value2 != 0) {
                            return ((float) value1 / (float) value2);
                        }else{
                            errorHandler.flag(node, DIVISION_BY_ZERO, this);
                            return 0;
                        }
                    }
                    case INTEGER_DIVIDE: {
                        if(value2 != 0) {
                            return value1 / value2;
                        }else{
                            errorHandler.flag(node, DIVISION_BY_ZERO, this);
                            return 0;
                        }
                    }
                    case MOD: {
                        if(value2 != 0) {
                            return value1 % value2;
                        }else {
                            errorHandler.flag(node, DIVISION_BY_ZERO, this);
                            return 0;
                        }
                    }
                }
            }
            else{
                float value1 = operand1 instanceof Integer ? (Integer) operand1 : (Float) operand1;
                float value2 = operand2 instanceof Integer ? (Integer) operand2 : (Float) operand2;

                switch (nodeType) {
                    case ADD: return value1 + value2;
                    case SUBTRACT: return value1 - value2;
                    case MULTIPLY: return value1 * value2;
                    case FLOAT_DIVIDE:  {
                        if(value2 != 0.0f) {
                            return value1 / value2;
                        } else{
                            errorHandler.flag(node, DIVISION_BY_ZERO, this);
                            return 0;
                        }
                    }
                }
            }
        }
        //AND and OR Operators
        else if((nodeType == AND) || (nodeType == OR)) {
            boolean value1 = (Boolean) operand1;
            boolean value2 = (Boolean) operand2;

            switch (nodeType) {
                case AND: return value1 && value2;
                case OR: return value1 || value2;
            }
        }
        // Relational Operators
        else if(integerMode) {
            int value1 = (Integer) operand1;
            int value2 = (Integer) operand2;

            switch (nodeType)
            {
                case EQ: return value1 == value2;
                case NE: return value1 != value2;
                case LT: return value1 < value2;
                case LE: return value1 <= value2;
                case GT: return value1 > value2;
                case GE: return value1 >= value2;
            }
        }
        else {
            float value1 = (operand1 instanceof Integer) ? (Integer) operand1 : (Float) operand1;
            float value2 = (operand2 instanceof Integer) ? (Integer) operand2 : (Float) operand2;

            switch (nodeType)
            {
                case EQ: return value1 == value2;
                case NE: return value1 != value2;
                case LT: return value1 < value2;
                case LE: return value1 <= value2;
                case GT: return value1 > value2;
                case GE: return value1 >= value2;
            }
        }
        return null;
    }
}
