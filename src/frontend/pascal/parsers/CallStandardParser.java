package frontend.pascal.parsers;

import frontend.Token;
import frontend.pascal.PascalParserTD;
import intermediate.*;
import intermediate.symtabimpl.Predefined;
import intermediate.symtabimpl.RoutineCodeImpl;

import static frontend.pascal.PascalErrorCode.INVALID_TYPE;
import static frontend.pascal.PascalErrorCode.WRONG_NUMBER_OF_PARMS;
import static intermediate.icodeimpl.ICodeKeyImpl.ID;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.CALL;
import static intermediate.symtabimpl.SymbolTableKeyImp.ROUTINE_CODE;
import static intermediate.typeimpl.TypeFormImpl.ENUMERATION;

public class CallStandardParser extends CallParser {

    public CallStandardParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception {
        ICodeNode callNode = ICodeFactory.createICodeNode(CALL);
        SymbolTableEntry routineId = symbolTableStack.lookup(token.getText().toLowerCase());
        RoutineCode routineCode = (RoutineCode) routineId.getAttribute(ROUTINE_CODE);
        callNode.setAttribute(ID, routineId);

        // consume routine identifier
        token = nextToken();

        switch ((RoutineCodeImpl) routineCode) {
            case READ:
            case READLN:
                return parseReadReadln(token, callNode, routineId);
            case WRITE:
            case WRITELN:
                return parseWriteWriteln(token, callNode, routineId);
            case EOF:
            case EOLN:
                return parseEofEoln(token, callNode, routineId);
            case ABS:
            case SQR:
                return parseAbsSqr(token, callNode, routineId);
            case ARCTAN:
            case COS:
            case EXP:
            case LN:
            case SIN:
            case SQRT:
                return parseArctanCosExpLnSinSqrt(token, callNode, routineId);
            case PRED:
            case SUCC:
                return parsePredSucc(token, callNode, routineId);
            case CHR:
                return parseChr(token, callNode, routineId);
            case ODD:
                return parseOdd(token, callNode, routineId);
            case ORD:
                return parseOrd(token, callNode, routineId);
            case TRUNC:
                return parseRoundTrunc(token, callNode, routineId);
            default:
                return null;
        }
    }

    private ICodeNode parseReadReadln(Token token,
                                      ICodeNode callNode,
                                      SymbolTableEntry routineId) throws Exception {
        // Parse any actual parameters
        ICodeNode parmsNode = parseActualParameters(token, routineId, false, true, false);
        callNode.addChild(parmsNode);

        // Read must have parameters
        if ((routineId == Predefined.readId) && (callNode.getChildren().size() == 0)) {
            errorHandler.flag(token, WRONG_NUMBER_OF_PARMS, this);
        }

        return callNode;
    }

    private ICodeNode parseWriteWriteln(Token token,
                                        ICodeNode callNode,
                                        SymbolTableEntry routineId) throws Exception {
        // Parse any actual parameter
        ICodeNode parmsNode = parseActualParameters(token, routineId, false, false, true);
        callNode.addChild(parmsNode);

        // Write must have parameters
        if ((routineId == Predefined.writeId) && (callNode.getChildren().size() == 0)) {
            errorHandler.flag(token, WRONG_NUMBER_OF_PARMS, this);
        }

        return callNode;
    }

    private ICodeNode parseEofEoln(Token token,
                                   ICodeNode callNode,
                                   SymbolTableEntry routineId) throws Exception {
        // Parse any actual parameter
        ICodeNode parmsNode = parseActualParameters(token, routineId, false, false, true);
        callNode.addChild(parmsNode);

        // There should be no actual parameters
        if (checkParmCount(token, parmsNode, 0)) {
            callNode.setTypeSpec(Predefined.booleanType);
        }

        return callNode;
    }

    private ICodeNode parseAbsSqr(Token token,
                                  ICodeNode callNode,
                                  SymbolTableEntry routineId) throws Exception {
        // Parse any actual parameter
        ICodeNode parmsNode = parseActualParameters(token, routineId, false, false, false);
        callNode.addChild(parmsNode);

        // There should be one integer or real parameter.
        // The function return type is the parameter type.
        if (checkParmCount(token, parmsNode, 1)) {
            TypeSpec argType = parmsNode.getChildren().get(0).getTypeSpec().baseType();
            if ((argType == Predefined.integerType)
                    || (argType == Predefined.realType)) {
                callNode.setTypeSpec(argType);
            } else {
                errorHandler.flag(token, INVALID_TYPE, this);
            }
        }

        return callNode;
    }

    private ICodeNode parseArctanCosExpLnSinSqrt(Token token,
                                                 ICodeNode callNode,
                                                 SymbolTableEntry routineId) throws Exception {
        // Parse any actual parameter
        ICodeNode parmsNode = parseActualParameters(token, routineId, false, false, false);
        callNode.addChild(parmsNode);

        // There should be one integer or real parameter.
        // The function return type is the parameter type.
        if (checkParmCount(token, parmsNode, 1)) {
            TypeSpec argType = parmsNode.getChildren().get(0).getTypeSpec().baseType();
            if ((argType == Predefined.integerType)
                    || (argType == Predefined.realType)) {
                callNode.setTypeSpec(Predefined.realType);
            } else {
                errorHandler.flag(token, INVALID_TYPE, this);
            }
        }

        return callNode;
    }

    private ICodeNode parsePredSucc(Token token,
                                    ICodeNode callNode,
                                    SymbolTableEntry routineId) throws Exception {
        // Parse any actual parameter
        ICodeNode parmsNode = parseActualParameters(token, routineId, false, false, false);
        callNode.addChild(parmsNode);

        // There should be one integer or enumeration parameter.
        // The function return type is the parameter type.
        if (checkParmCount(token, parmsNode, 1)) {
            TypeSpec argType = parmsNode.getChildren().get(0).getTypeSpec().baseType();
            if ((argType == Predefined.integerType) || (argType.getForm() == ENUMERATION)) {
                callNode.setTypeSpec(argType);
            } else {
                errorHandler.flag(token, INVALID_TYPE, this);
            }
        }

        return callNode;
    }

    private ICodeNode parseChr(Token token,
                               ICodeNode callNode,
                               SymbolTableEntry routineId) throws Exception {
        // Parse any actual parameter
        ICodeNode parmsNode = parseActualParameters(token, routineId, false, false, false);
        callNode.addChild(parmsNode);

        // There should be one integer
        // The function return type is character.
        if (checkParmCount(token, parmsNode, 1)) {
            TypeSpec argType = parmsNode.getChildren().get(0).getTypeSpec().baseType();
            if (argType == Predefined.integerType) {
                callNode.setTypeSpec(Predefined.charType);
            } else {
                errorHandler.flag(token, INVALID_TYPE, this);
            }
        }

        return callNode;
    }

    private ICodeNode parseOdd(Token token,
                               ICodeNode callNode,
                               SymbolTableEntry routineId) throws Exception {
        // Parse any actual parameter
        ICodeNode parmsNode = parseActualParameters(token, routineId, false, false, false);
        callNode.addChild(parmsNode);


        if (checkParmCount(token, parmsNode, 1)) {
            TypeSpec argType = parmsNode.getChildren().get(0).getTypeSpec().baseType();
            if ((argType == Predefined.integerType)) {
                callNode.setTypeSpec(Predefined.booleanType);
            } else {
                errorHandler.flag(token, INVALID_TYPE, this);
            }
        }

        return callNode;
    }

    private ICodeNode parseOrd(Token token,
                               ICodeNode callNode,
                               SymbolTableEntry routineId) throws Exception {
        // Parse any actual parameter
        ICodeNode parmsNode = parseActualParameters(token, routineId, false, false, false);
        callNode.addChild(parmsNode);

        // There should be one character or enumeration parameter.
        // The function return type is integer.
        if (checkParmCount(token, parmsNode, 1)) {
            TypeSpec argType = parmsNode.getChildren().get(0).getTypeSpec().baseType();
            if ((argType == Predefined.charType) || (argType.getForm() == ENUMERATION)) {
                callNode.setTypeSpec(Predefined.integerType);
            } else {
                errorHandler.flag(token, INVALID_TYPE, this);
            }
        }

        return callNode;
    }

    private ICodeNode parseRoundTrunc(Token token,
                                      ICodeNode callNode,
                                      SymbolTableEntry routineId) throws Exception {
        // Parse any actual parameter
        ICodeNode parmsNode = parseActualParameters(token, routineId, false, false, false);
        callNode.addChild(parmsNode);

        // There should be one real parameter.er.
        // The function return type is integer.
        if (checkParmCount(token, parmsNode, 1)) {
            TypeSpec argType = parmsNode.getChildren().get(0).getTypeSpec().baseType();
            if (argType == Predefined.realType) {
                callNode.setTypeSpec(Predefined.integerType);
            } else {
                errorHandler.flag(token, INVALID_TYPE, this);
            }
        }

        return callNode;
    }

    private boolean checkParmCount(Token token, ICodeNode parmsNode, int count) {
        if (((parmsNode == null) && (count == 0))
                || (parmsNode.getChildren().size() == count)) {
            return true;
        } else {
            errorHandler.flag(token, WRONG_NUMBER_OF_PARMS, this);
            return false;
        }
    }
}
