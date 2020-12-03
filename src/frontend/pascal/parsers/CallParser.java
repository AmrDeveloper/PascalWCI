package frontend.pascal.parsers;

import frontend.Token;
import frontend.TokenType;
import frontend.pascal.PascalErrorCode;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.*;
import intermediate.icodeimpl.ICodeNodeTypeImpl;
import intermediate.symtabimpl.Predefined;
import intermediate.typeimpl.TypeChecker;

import java.util.EnumSet;
import java.util.List;

import static frontend.pascal.PascalErrorCode.*;
import static frontend.pascal.PascalTokenType.*;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static intermediate.symtabimpl.DefinitionImpl.VAR_PARM;
import static intermediate.symtabimpl.RoutineCodeImpl.DECLARED;
import static intermediate.symtabimpl.RoutineCodeImpl.FORWARD;
import static intermediate.symtabimpl.SymbolTableKeyImp.ROUTINE_CODE;
import static intermediate.symtabimpl.SymbolTableKeyImp.ROUTINE_PARMS;
import static intermediate.typeimpl.TypeFormImpl.SCALAR;
import static intermediate.typeimpl.TypeFormImpl.SUBRANGE;

public class CallParser extends StatementParser {

    // Synchronization set for the , token.
    private static final EnumSet<PascalTokenType> COMMA_SET =
            ExpressionParser.EXPR_START_SET.clone();

    static {
        COMMA_SET.add(COMMA);
        COMMA_SET.add(RIGHT_PAREN);
    }

    ;

    public CallParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception {
        SymbolTableEntry routineId = symbolTableStack.lookupLocal(token.getText().toLowerCase());
        RoutineCode routineCode = (RoutineCode) routineId.getAttribute(ROUTINE_CODE);
        StatementParser callParser = (routineCode == DECLARED) || (routineCode == FORWARD)
                ? new CallDeclaredParser(this)
                : new CallStandardParser(this);

        return callParser.parse(token);
    }

    protected ICodeNode parseActualParameters(Token token,
                                              SymbolTableEntry routineId,
                                              boolean isDeclared,
                                              boolean isReadReadln,
                                              boolean isWriteWriteln) throws Exception {
        ExpressionParser expressionParser = new ExpressionParser(this);
        ICodeNode paramsNode = ICodeFactory.createICodeNode(PARAMETERS);
        List<SymbolTableEntry> formalParams = null;

        int parmCount = 0;
        int parmIndex = -1;

        if (isDeclared) {
            formalParams = (List<SymbolTableEntry>) routineId.getAttribute(ROUTINE_PARMS);
            parmCount = formalParams != null ? formalParams.size() : 0;
        }

        if (token.getType() != LEFT_PAREN) {
            if (parmCount != 0) {
                errorHandler.flag(token, WRONG_NUMBER_OF_PARMS, this);
            }

            return null;
        }

        // consume opening (
        token = nextToken();

        // Loop to parse each actual parameter
        while (token.getType() != RIGHT_PAREN) {
            ICodeNode actualNode = expressionParser.parse(token);

            // Declared procedure or function: Check the number of actual
            // parameters, and check each actual parameter against the
            // corresponding formal parameter
            if (isDeclared) {
                if (++parmIndex < parmCount) {
                    SymbolTableEntry formalId = formalParams.get(parmIndex);
                    checkActualParameter(token, formalId, actualNode);
                } else if (parmIndex == parmCount) {
                    errorHandler.flag(token, WRONG_NUMBER_OF_PARMS, this);
                }
            }

            // read or readln: Each actual parameter must be a variable that is
            //                 a scalar, boolean, or subrange of integer.
            else if (isReadReadln) {
                TypeSpec type = actualNode.getTypeSpec();
                TypeForm form = type.getForm();

                if (!((actualNode.getType() == ICodeNodeTypeImpl.VARIABLE)
                        && ((form == SCALAR)
                        || (type == Predefined.booleanType)
                        || ((form == SUBRANGE)
                        && (type.baseType() == Predefined.integerType))))) {
                    errorHandler.flag(token, INVALID_VAR_PARM, this);
                }
            }

            // write or writeln: The type of each actual parameter must be a
            // scalar, boolean, or a Pascal string. Parse any field width and
            // precision
            else if (isWriteWriteln) {
                // Create a WRITE_PARM node which adopts the expression node.
                ICodeNode exprNode = actualNode;
                actualNode = ICodeFactory.createICodeNode(WRITE_PARM);
                actualNode.addChild(exprNode);

                TypeSpec type = exprNode.getTypeSpec().baseType();
                TypeForm form = type.getForm();

                if (!((form == SCALAR)
                        || (type == Predefined.booleanType)
                        || (type.isPascalString()))) {
                    errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                }

                // Optional field with
                token = currentToken();
                actualNode.addChild(parseWriteSpec(token));

                // Optional precision
                token = currentToken();
                actualNode.addChild(parseWriteSpec(token));
            }

            paramsNode.addChild(actualNode);
            token = synchronize(COMMA_SET);
            TokenType tokenType = token.getType();

            // Look for the comma
            if (tokenType == COMMA) {
                // consume ,
                token = nextToken();
            } else if (ExpressionParser.EXPR_START_SET.contains(tokenType)) {
                errorHandler.flag(token, MISSING_COMMA, this);
            } else if (tokenType != RIGHT_PAREN) {
                token = synchronize(ExpressionParser.EXPR_START_SET);
            }
        }

        // consume closing )
        token = nextToken();

        if ((paramsNode.getChildren().size() == 0) ||
                (isDeclared && (parmIndex != parmCount - 1))) {
            errorHandler.flag(token, WRONG_NUMBER_OF_PARMS, this);
        }

        return paramsNode;
    }

    private void checkActualParameter(Token token,
                                      SymbolTableEntry formalId,
                                      ICodeNode actualNode) throws Exception {
        Definition formalDefinition = formalId.getDefinition();
        TypeSpec formalType = formalId.getTypeSpec();
        TypeSpec actualType = actualNode.getTypeSpec();

        // VAR parameter: The actual parameter must be a variable of the same
        //                type as the formal parameter.
        if (formalDefinition == VAR_PARM) {
            if ((actualNode.getType() != ICodeNodeTypeImpl.VARIABLE) ||
                    (actualType != formalType)) {
                errorHandler.flag(token, INVALID_VAR_PARM, this);
            }
        }

        // Value parameter: The actual parameter must be assignment-compatible
        //                  with the formal parameter.
        else if (!TypeChecker.areAssignmentCompatible(formalType, actualType)) {
            errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
        }
    }

    private ICodeNode parseWriteSpec(Token token) throws Exception {
        if (token.getType() == COLON) {
            // consume :
            token = nextToken();

            ExpressionParser expressionParser = new ExpressionParser(this);
            ICodeNode specNode = expressionParser.parse(token);

            if (specNode.getType() == INTEGER_CONSTANT) {
                return specNode;
            } else {
                errorHandler.flag(token, INVALID_NUMBER, this);
                return null;
            }
        } else {
            return null;
        }
    }
}
