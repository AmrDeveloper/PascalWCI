package backend.interpreter.executors;

import backend.interpreter.Cell;
import backend.interpreter.Executor;
import backend.interpreter.RuntimeErrorCode;
import frontend.Source;
import frontend.Token;
import frontend.TokenType;
import intermediate.ICodeNode;
import intermediate.RoutineCode;
import intermediate.SymbolTableEntry;
import intermediate.TypeSpec;
import intermediate.symtabimpl.Predefined;
import intermediate.symtabimpl.RoutineCodeImpl;

import java.util.List;

import static frontend.pascal.PascalTokenType.*;
import static intermediate.icodeimpl.ICodeKeyImpl.ID;
import static intermediate.icodeimpl.ICodeKeyImpl.VALUE;
import static intermediate.symtabimpl.RoutineCodeImpl.*;
import static intermediate.symtabimpl.SymbolTableKeyImp.ROUTINE_CODE;

public class CallStandardExecutor extends CallExecutor {

    private ExpressionExecutor expressionExecutor;

    public CallStandardExecutor(Executor executor) {
        super(executor);
    }

    public Object execute(ICodeNode node) {
        SymbolTableEntry routineId = (SymbolTableEntry) node.getAttribute(ID);
        RoutineCode routineCode = (RoutineCode) routineId.getAttribute(ROUTINE_CODE);

        TypeSpec type = node.getTypeSpec();
        expressionExecutor = new ExpressionExecutor(this);
        ICodeNode actualNode = null;

        // Get the actual parameters of the call
        if (node.getChildren().size() > 0) {
            ICodeNode parmsNode = node.getChildren().get(0);
            actualNode = parmsNode.getChildren().get(0);
        }

        switch ((RoutineCodeImpl) routineCode) {
            case READ:
            case READLN:
                return executeReadReadln(node, routineCode);

            case WRITE:
            case WRITELN:
                return executeWriteWriteln(node, routineCode);

            case EOF:
            case EOLN:
                return executeEofEoln(node, routineCode);

            case ABS:
            case SQR:
                return executeAbsSqr(node, routineCode, actualNode);

            case ARCTAN:
            case COS:
            case EXP:
            case LN:
            case SIN:
            case SQRT:
                return executeArctanCosExpLnSinSqrt(node, routineCode, actualNode);

            case PRED:
            case SUCC:
                return executePredSucc(node, routineCode, actualNode, type);

            case CHR:
                return executeChr(node, routineCode, actualNode);
            case ODD:
                return executeOdd(node, routineCode, actualNode);
            case ORD:
                return executeOrd(node, routineCode, actualNode);

            case ROUND:
            case TRUNC:
                return executeRoundTrunc(node, routineCode, actualNode);

            default:
                return null;  // should never get here
        }
    }

    private Object executeReadReadln(ICodeNode callNode, RoutineCode routineCode) {
        ICodeNode parmsNode = callNode.getChildren().size() > 0
                ? callNode.getChildren().get(0)
                : null;

        if (parmsNode != null) {
            List<ICodeNode> actuals = parmsNode.getChildren();

            // Loop to process each actual parameter
            for (ICodeNode actualNode : actuals) {
                TypeSpec type = actualNode.getTypeSpec();
                TypeSpec baseType = type.baseType();
                Cell variableCell = (Cell) expressionExecutor.executeVariable(actualNode);
                Object value;

                // Read a value of the appropriate type from the standard input
                try {
                    if (baseType == Predefined.integerType) {
                        Token token = standardIn.nextToken();
                        value = (Integer) parseNumber(token, baseType);
                    } else if (baseType == Predefined.realType) {
                        Token token = standardIn.nextToken();
                        value = (Float) parseNumber(token, baseType);
                    } else if (baseType == Predefined.booleanType) {
                        Token token = standardIn.nextToken();
                        value = parseBoolean(token);
                    } else if (baseType == Predefined.charType) {
                        char ch = standardIn.nextChar();
                        if ((ch == Source.EOL) || (ch == Source.EOF)) {
                            ch = ' ';
                        }
                        value = ch;
                    } else {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    errorHandler.flag(callNode, RuntimeErrorCode.INVALID_INPUT, this);

                    if (type == Predefined.realType) {
                        value = 0.0f;
                    } else if (type == Predefined.charType) {
                        value = ' ';
                    } else if (type == Predefined.booleanType) {
                        value = false;
                    } else {
                        value = 0;
                    }
                }

                // Range check and set the value
                value = checkRange(callNode, type, value);
                variableCell.setValue(value);

                SymbolTableEntry actualId = (SymbolTableEntry) actualNode.getAttribute(ID);
                sendAssignMessage(callNode, actualId.getName(), value);
            }
        }

        // Skip the rest of the input line for readln
        if (routineCode == READLN) {
            try {
                standardIn.skipToNextLine();
            } catch (Exception ex) {
                errorHandler.flag(callNode, RuntimeErrorCode.INVALID_INPUT, this);
            }
        }

        return null;
    }

    private Number parseNumber(Token token, TypeSpec typeSpec) throws Exception {
        TokenType tokenType = token.getType();
        TokenType sign = null;

        // Leading sign ?
        if ((tokenType == PLUS) || (tokenType == MINUS)) {
            sign = tokenType;
            token = standardIn.nextToken();
            tokenType = token.getType();
        }

        // Integer value
        if (tokenType == INTEGER) {
            Number value = (sign == MINUS)
                    ? -((Integer) token.getValue())
                    : (Integer) token.getValue();

            return typeSpec == Predefined.integerType
                    ? value
                    : new Float(((Integer) value).intValue());
        }

        // Real value
        else if (tokenType == REAL) {
            Number value = (sign == MINUS)
                    ? -((Float) token.getValue())
                    : (Float) token.getValue();

            return typeSpec == Predefined.realType
                    ? value
                    : new Integer(((Float) value).intValue());
        }

        // Bad input
        else {
            throw new Exception();
        }
    }

    private Boolean parseBoolean(Token token) throws Exception {
        if (token.getType() == IDENTIFIER) {
            String text = token.getText();

            if ("true".equalsIgnoreCase(text)) {
                return new Boolean(true);
            } else if ("false".equalsIgnoreCase(text)) {
                return new Boolean(false);
            } else {
                throw new Exception();
            }
        } else {
            throw new Exception();
        }
    }

    private Object executeWriteWriteln(ICodeNode callNode, RoutineCode routineCode) {
        ICodeNode parmsNode = callNode.getChildren().size() > 0
                ? callNode.getChildren().get(0)
                : null;

        if (parmsNode != null) {
            List<ICodeNode> actuals = parmsNode.getChildren();

            // Loop to process each WRITE_PARM actual parameter node
            for (ICodeNode writeParmNode : actuals) {
                List<ICodeNode> children = writeParmNode.getChildren();
                ICodeNode exprNode = children.get(0);
                TypeSpec dataType = exprNode.getTypeSpec().baseType();
                String typeNode = dataType.isPascalString() ? "s"
                        : dataType == Predefined.integerType ? "d"
                        : dataType == Predefined.realType ? "f"
                        : dataType == Predefined.booleanType ? "s"
                        : dataType == Predefined.charType ? "c"
                        : "s";

                Object value = expressionExecutor.execute(exprNode);

                if ((dataType == Predefined.charType) && (value instanceof String)) {
                    value = ((String) value).charAt(0);
                }

                // Java format String
                StringBuilder format = new StringBuilder("%");

                // Process any field with and precision value
                if (children.size() > 1) {
                    int w = (Integer) children.get(1).getAttribute(VALUE);
                    format.append(w == 0 ? 1 : w);
                }

                if (children.size() > 2) {
                    int p = (Integer) children.get(2).getAttribute(VALUE);
                    format.append(".");
                    format.append(p == 0 ? 1 : p);
                }

                format.append(typeNode);

                // Write the formatted value to the standard output
                standardOut.printf(format.toString(), value);
                standardOut.flush();
            }
        }

        // Line feed for writeln
        if (routineCode == WRITELN) {
            standardOut.println();
            standardOut.flush();
        }

        return null;
    }

    private Boolean executeEofEoln(ICodeNode callNode, RoutineCode routineCode) {
        try {
            if (routineCode == EOF) {
                return standardIn.atEof();
            } else {
                return standardIn.atEol();
            }
        } catch (Exception ex) {
            errorHandler.flag(callNode, RuntimeErrorCode.INVALID_INPUT, this);
            return true;
        }
    }

    private Number executeAbsSqr(ICodeNode callNode, RoutineCode routineCode, ICodeNode actualNode) {
        Object argValue = expressionExecutor.execute(actualNode);

        if (argValue instanceof Integer) {
            int value = (Integer) argValue;
            return routineCode == ABS ? Math.abs(value) : value * value;
        } else {
            float value = (Float) argValue;
            return routineCode == ABS ? Math.abs(value) : value * value;
        }
    }

    private Float executeArctanCosExpLnSinSqrt(ICodeNode callNode,
                                               RoutineCode routineCode,
                                               ICodeNode actualNode) {
        Object argValue = expressionExecutor.execute(actualNode);
        Float value = argValue instanceof Integer
                ? (Integer) argValue
                : (Float) argValue;

        switch ((RoutineCodeImpl) routineCode) {
            case ARCTAN:
                return (float) Math.atan(value);
            case COS:
                return (float) Math.cos(value);
            case EXP:
                return (float) Math.exp(value);
            case SIN:
                return (float) Math.signum(value);

            case LN: {
                if (value > 0.0f) {
                    return (float) Math.log(value);
                } else {
                    errorHandler.flag(callNode,
                            RuntimeErrorCode.INVALID_STANDARD_FUNCTION_ARGUMENT,
                            this);
                    return 0.0f;
                }
            }
            case SQRT: {
                if (value >= 0.0f) {
                    return (float) Math.sqrt(value);
                } else {
                    errorHandler.flag(callNode,
                            RuntimeErrorCode.INVALID_STANDARD_FUNCTION_ARGUMENT,
                            this);
                    return 0.0f;
                }
            }

            default:
                // should never get here
                return 0.0f;
        }
    }

    private Integer executePredSucc(ICodeNode callNode,
                                    RoutineCode routineCode,
                                    ICodeNode actualNode, TypeSpec type) {
        int value = (Integer) expressionExecutor.execute(actualNode);
        int newValue = (routineCode == PRED) ? --value : ++value;
        newValue = (Integer) checkRange(callNode, type, newValue);
        return newValue;
    }

    private Character executeChr(ICodeNode callNode,
                                 RoutineCode routineCode,
                                 ICodeNode actualNode) {
        int value = (Integer) expressionExecutor.execute(actualNode);
        char ch = (char) value;
        return ch;
    }

    private Boolean executeOdd(ICodeNode callNode,
                               RoutineCode routineCode,
                               ICodeNode actualNode) {
        int value = (Integer) expressionExecutor.execute(actualNode);
        return (value & 1) == 1;
    }

    private Integer executeOrd(ICodeNode callNode,
                               RoutineCode routineCode,
                               ICodeNode actualNode) {
        Object value = expressionExecutor.execute(actualNode);

        if (value instanceof Character) {
            char ch = ((Character) value).charValue();
            return (int) ch;
        } else if (value instanceof String) {
            char ch = ((String) value).charAt(0);
            return (int) ch;
        } else {
            return (Integer) value;
        }
    }

    private Integer executeRoundTrunc(ICodeNode callNode,
                                      RoutineCode routineCode,
                                      ICodeNode actualNode) {
        float value = (Float) expressionExecutor.execute(actualNode);

        if (routineCode == ROUND) {
            return value >= 0.0f
                    ? (int) (value + 0.5f)
                    : (int) (value - 0.5f);
        } else {
            return (int) value;
        }
    }
}
