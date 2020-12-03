package frontend.pascal.parsers;

import frontend.Token;
import frontend.TokenType;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.*;
import intermediate.icodeimpl.ICodeNodeTypeImpl;
import intermediate.symtabimpl.DefinitionImpl;
import intermediate.symtabimpl.Predefined;
import intermediate.typeimpl.TypeChecker;

import java.util.EnumSet;
import java.util.HashMap;

import static frontend.pascal.PascalErrorCode.*;
import static frontend.pascal.PascalTokenType.*;
import static intermediate.icodeimpl.ICodeKeyImpl.VALUE;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.NOT;
import static intermediate.symtabimpl.DefinitionImpl.UNDEFINED;
import static intermediate.symtabimpl.SymbolTableKeyImp.CONSTANT_VALUE;

public class ExpressionParser extends PascalParserTD {

    // Set of relational operators.
    private static final EnumSet<PascalTokenType> REL_OPS =
            EnumSet.of(EQUALS, NOT_EQUALS, LESS_THAN, LESS_EQUALS,
                    GREATER_THAN, GREATER_EQUALS);

    // Set of additive operators.
    private static final EnumSet<PascalTokenType> ADD_OPS =
            EnumSet.of(PLUS, MINUS, PascalTokenType.OR);

    // Set of multiplicative operators.
    private static final EnumSet<PascalTokenType> MULT_OPS =
            EnumSet.of(STAR, SLASH, DIV, PascalTokenType.MOD, PascalTokenType.AND);

    // Synchronization set for starting an expression.
    static final EnumSet<PascalTokenType> EXPR_START_SET = EnumSet.of(PLUS, MINUS, IDENTIFIER, INTEGER, REAL, STRING, PascalTokenType.NOT, LEFT_PAREN);

    // Map relational operator tokens to node types.
    private static final HashMap<PascalTokenType, ICodeNodeType> REL_OPS_MAP = new HashMap<>();

    static {
        REL_OPS_MAP.put(EQUALS, EQ);
        REL_OPS_MAP.put(NOT_EQUALS, NE);
        REL_OPS_MAP.put(LESS_THAN, LT);
        REL_OPS_MAP.put(LESS_EQUALS, LE);
        REL_OPS_MAP.put(GREATER_THAN, GT);
        REL_OPS_MAP.put(GREATER_EQUALS, GE);
    }

    // Map additive operator tokens to node types.
    private static final HashMap<PascalTokenType, ICodeNodeTypeImpl> ADD_OPS_OPS_MAP = new HashMap<>();

    static {
        ADD_OPS_OPS_MAP.put(PLUS, ADD);
        ADD_OPS_OPS_MAP.put(MINUS, SUBTRACT);
        ADD_OPS_OPS_MAP.put(PascalTokenType.OR, ICodeNodeTypeImpl.OR);
    }

    // Map multiplicative operator tokens to node types.
    private static final HashMap<PascalTokenType, ICodeNodeType> MULT_OPS_OPS_MAP = new HashMap<>();

    static {
        MULT_OPS_OPS_MAP.put(STAR, MULTIPLY);
        MULT_OPS_OPS_MAP.put(SLASH, FLOAT_DIVIDE);
        MULT_OPS_OPS_MAP.put(DIV, INTEGER_DIVIDE);
        MULT_OPS_OPS_MAP.put(PascalTokenType.MOD, ICodeNodeTypeImpl.MOD);
        MULT_OPS_OPS_MAP.put(PascalTokenType.AND, ICodeNodeTypeImpl.AND);
    }

    public ExpressionParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception {
        return parseExpression(token);
    }

    private ICodeNode parseExpression(Token token) throws Exception {
        ICodeNode rootNode = parseSimpleExpression(token);
        TypeSpec resultType = rootNode != null
                ? rootNode.getTypeSpec()
                : Predefined.undefinedType;

        token = currentToken();
        TokenType tokenType = token.getType();

        if (REL_OPS.contains(tokenType)) {
            ICodeNodeType nodeType = REL_OPS_MAP.get(tokenType);
            ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
            opNode.addChild(rootNode);

            // Consume the operator
            token = nextToken();

            ICodeNode simpleExpressionNode = parseSimpleExpression(token);
            opNode.addChild(simpleExpressionNode);

            // The operator node becomes the new root node
            rootNode = opNode;

            // Type check: The operands must be comparison compatible.
            TypeSpec simpleExpressionNType = simpleExpressionNode != null
                    ? simpleExpressionNode.getTypeSpec()
                    : Predefined.undefinedType;

            if (TypeChecker.areComparisonCompatible(resultType, simpleExpressionNType)) {
                resultType = Predefined.booleanType;
            } else {
                errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                resultType = Predefined.undefinedType;
            }
        }

        if (rootNode != null) rootNode.setTypeSpec(resultType);

        return rootNode;
    }

    private ICodeNode parseSimpleExpression(Token token) throws Exception {
        Token signToken = null;

        // type of leading sign (if any)
        TokenType signType = null;

        //Look for a leading x or -
        TokenType tokenType = token.getType();
        if ((tokenType == PLUS) || (tokenType == MINUS)) {
            signType = tokenType;
            signToken = token;
            token = nextToken();
        }

        // Parse a term and make the root of its tree the root node.
        ICodeNode rootNode = parseTerm(token);
        TypeSpec resultType = (rootNode != null)
                ? rootNode.getTypeSpec()
                : Predefined.undefinedType;

        // Type check : leading sign
        if ((signType != null) && (!TypeChecker.isIntegerOrReal(resultType))) {
            errorHandler.flag(signToken, INCOMPATIBLE_TYPES, this);
        }

        if (signType == MINUS) {
            ICodeNode negateNode = ICodeFactory.createICodeNode(NEGATE);
            negateNode.addChild(rootNode);
            negateNode.setTypeSpec(rootNode.getTypeSpec());
            rootNode = negateNode;
        }

        token = currentToken();
        tokenType = token.getType();

        while (ADD_OPS.contains(tokenType)) {
            TokenType operator = tokenType;

            ICodeNodeType nodeType = ADD_OPS_OPS_MAP.get(tokenType);
            ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
            opNode.addChild(rootNode);

            // consume the operator
            token = nextToken();

            ICodeNode termNode = parseTerm(token);
            opNode.addChild(termNode);

            TypeSpec termType = (termNode != null)
                    ? termNode.getTypeSpec()
                    : Predefined.undefinedType;

            rootNode = opNode;

            switch ((PascalTokenType) operator) {
                case PLUS:
                case MINUS: {
                    // Both operands integer ==> integer result.
                    if (TypeChecker.areBothInteger(resultType, termType)) {
                        resultType = Predefined.integerType;
                    }

                    // Both real operands or one real and one integer operand result ==> real
                    else if (TypeChecker.isAtLeastOneReal(resultType, termType)) {
                        resultType = Predefined.realType;
                    }

                    break;
                }
                case OR: {
                    // Both operands boolean ==> boolean result
                    if (TypeChecker.areBothBoolean(resultType, termType)) {
                        resultType = Predefined.booleanType;
                    } else {
                        errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                    }
                }
            }

            rootNode.setTypeSpec(resultType);

            token = currentToken();
            tokenType = token.getType();
        }
        return rootNode;
    }

    private ICodeNode parseTerm(Token token) throws Exception {
        ICodeNode rootNode = parseFactor(token);

        token = currentToken();
        TokenType tokenType = token.getType();

        TypeSpec resultType = (rootNode != null)
                ? rootNode.getTypeSpec()
                : Predefined.undefinedType;

        while (MULT_OPS.contains(tokenType)) {
            TokenType operator = tokenType;

            ICodeNodeType nodeType = MULT_OPS_OPS_MAP.get(tokenType);
            ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
            opNode.addChild(rootNode);

            token = nextToken();

            ICodeNode factorNode = parseFactor(token);
            opNode.addChild(parseFactor(token));

            TypeSpec factorType = (factorNode != null)
                    ? factorNode.getTypeSpec()
                    : Predefined.undefinedType;

            rootNode = opNode;

            switch ((PascalTokenType) operator) {
                case STAR: {
                    // Both operands integer ==> integer result.
                    if (TypeChecker.areBothInteger(resultType, factorType)) {
                        resultType = Predefined.integerType;
                    }
                    // Both real operands or one real and one integer operand
                    // ==> real result.
                    else if (TypeChecker.isAtLeastOneReal(resultType, factorType)) {
                        resultType = Predefined.realType;
                    } else {
                        errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                    }
                    break;
                }
                case SLASH: {
                    // All integer and real operand combinations
                    // ==> real result.
                    if (TypeChecker.areBothInteger(resultType, factorType) || TypeChecker.isAtLeastOneReal(resultType, factorType)) {
                        resultType = Predefined.realType;
                    } else {
                        errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                    }
                    break;
                }
                case DIV:
                case MOD: {
                    // Both operands integer ==> integer result
                    if (TypeChecker.areBothInteger(resultType, factorType)) {
                        resultType = Predefined.integerType;
                    } else {
                        errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                    }
                    break;
                }
                case AND: {
                    // Both operands boolean ==> boolean result
                    if (TypeChecker.areBothBoolean(resultType, factorType)) {
                        resultType = Predefined.booleanType;
                    } else {
                        errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                    }
                    break;
                }
            }

            rootNode.setTypeSpec(resultType);
            token = currentToken();
            tokenType = token.getType();
        }

        return rootNode;
    }

    //a factor can be a variable, a number,a string, NOT followed by another factor, or a parenthesized expression
    private ICodeNode parseFactor(Token token) throws Exception {
        TokenType tokenType = token.getType();
        ICodeNode rootNode = null;

        switch ((PascalTokenType) tokenType) {
            case IDENTIFIER: {
                rootNode = parseIdentifier(token);
                break;
            }
            case INTEGER: {
                rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
                rootNode.setAttribute(VALUE, token.getValue());

                // consume the number
                token = nextToken();

                rootNode.setTypeSpec(Predefined.integerType);
                break;
            }
            case REAL: {
                rootNode = ICodeFactory.createICodeNode(REAL_CONSTANT);
                rootNode.setAttribute(VALUE, token.getValue());

                // consume the number
                token = nextToken();

                //TODO: Test this
                rootNode.setTypeSpec(Predefined.realType);
                break;
            }
            case STRING: {
                String value = (String) token.getValue();

                // Create a STRING_CONSTANT node as the root node.
                rootNode = ICodeFactory.createICodeNode(STRING_CONSTANT);
                rootNode.setAttribute(VALUE, value);

                TypeSpec resultType = value.length() == 1
                        ? Predefined.charType
                        : TypeFactory.createStringType(value);

                // consume the string
                token = nextToken();

                rootNode.setTypeSpec(resultType);
                break;
            }
            case NOT: {
                // consume the NOT
                token = nextToken();

                // Create a NOT node as the root node.
                rootNode = ICodeFactory.createICodeNode(NOT);

                // Parse the factor.  The NOT node adopts the
                // factor node as its child
                ICodeNode factorNode = parseFactor(token);
                rootNode.addChild(factorNode);

                // Type check: The factor must be boolean.
                TypeSpec factorType = factorNode != null
                        ? factorNode.getTypeSpec()
                        : Predefined.undefinedType;

                if (!TypeChecker.isBoolean(factorType)) {
                    errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                }

                rootNode.setTypeSpec(Predefined.booleanType);
                break;
            }
            case LEFT_PAREN: {
                token = nextToken();

                rootNode = parseExpression(token);

                token = currentToken();
                if (token.getType() == RIGHT_PAREN) {
                    token = nextToken();
                } else {
                    errorHandler.flag(token, MISSING_RIGHT_PAREN, this);
                }
                break;
            }
            default: {
                errorHandler.flag(token, UNEXPECTED_TOKEN, this);
                break;
            }
        }

        return rootNode;
    }

    private ICodeNode parseIdentifier(Token token) throws Exception {
        ICodeNode rootNode = null;

        // Look up the identifier in the symbol table stack
        String name = token.getText().toLowerCase();
        SymbolTableEntry id = symbolTableStack.lookup(name);

        // Undefined
        if(id == null) {
            errorHandler.flag(token, IDENTIFIER_UNDEFINED, this);
            id = symbolTableStack.enterLocal(name);
            id.setDefinition(UNDEFINED);
            id.setTypeSpec(Predefined.undefinedType);
        }

        Definition definitionCode = id.getDefinition();

        switch ((DefinitionImpl) definitionCode) {
            case CONSTANT: {
                Object value = id.getAttribute(CONSTANT_VALUE);
                TypeSpec type = id.getTypeSpec();

                if(value instanceof Integer) {
                    rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
                    rootNode.setAttribute(VALUE, value);
                }
                else if(value instanceof Float) {
                    rootNode = ICodeFactory.createICodeNode(REAL_CONSTANT);
                    rootNode.setAttribute(VALUE, value);
                }
                else if(value instanceof String) {
                    rootNode = ICodeFactory.createICodeNode(STRING_CONSTANT);
                    rootNode.setAttribute(VALUE, value);
                }

                id.appendLineNumber(token.getLineNumber());

                // consume the constant identifier
                token = nextToken();

                if(rootNode != null) rootNode.setTypeSpec(type);

                break;
            }
            case ENUMERATION_CONSTANT: {
                Object value = id.getAttribute(CONSTANT_VALUE);
                TypeSpec type = id.getTypeSpec();

                rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
                rootNode.setAttribute(VALUE, value);

                id.appendLineNumber(token.getLineNumber());

                // consume the enum constant identifier
                token = nextToken();

                rootNode.setTypeSpec(type);
                break;
            }
            case FUNCTION: {
                CallParser callParser = new CallParser(this);
                rootNode = callParser.parse(token);
                break;
            }
            default: {
                VariableParser variableParser = new VariableParser(this);
                rootNode = variableParser.parse(token, id);
                break;
            }
        }
        return rootNode;
    }
}
