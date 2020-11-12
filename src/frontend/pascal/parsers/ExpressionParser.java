package frontend.pascal.parsers;

import frontend.Token;
import frontend.TokenType;
import frontend.pascal.PascalErrorCode;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.ICodeFactory;
import intermediate.ICodeNode;
import intermediate.ICodeNodeType;
import intermediate.SymbolTableEntry;
import intermediate.icodeimpl.ICodeNodeTypeImpl;

import java.util.EnumSet;
import java.util.HashMap;

import static frontend.pascal.PascalErrorCode.*;
import static frontend.pascal.PascalTokenType.*;
import static intermediate.icodeimpl.ICodeKeyImpl.ID;
import static intermediate.icodeimpl.ICodeKeyImpl.VALUE;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.NOT;

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
    static final EnumSet<PascalTokenType> EXPR_START_SET =  EnumSet.of(PLUS, MINUS, IDENTIFIER, INTEGER, REAL, STRING,                   PascalTokenType.NOT, LEFT_PAREN);

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

        token = currentToken();
        TokenType tokenType = token.getType();
        if (REL_OPS.contains(tokenType)) {
            ICodeNodeType nodeType = REL_OPS_MAP.get(tokenType);
            ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
            opNode.addChild(rootNode);

            token = nextToken();

            opNode.addChild(parseSimpleExpression(token));

            rootNode = opNode;
        }
        return rootNode;
    }

    private ICodeNode parseSimpleExpression(Token token) throws Exception {
        TokenType signType = null;

        //Look for a leading x or -
        TokenType tokenType = token.getType();
        if ((tokenType == PLUS) || (tokenType == MINUS)) {
            signType = tokenType;
            token = nextToken();
        }

        ICodeNode rootNode = parseTerm(token);

        if (signType == MINUS) {
            ICodeNode negateNode = ICodeFactory.createICodeNode(NEGATE);
            negateNode.addChild(rootNode);
            rootNode = negateNode;
        }

        token = currentToken();
        tokenType = token.getType();

        while (ADD_OPS.contains(tokenType)) {
            ICodeNodeType nodeType = ADD_OPS_OPS_MAP.get(tokenType);
            ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
            opNode.addChild(rootNode);

            token = nextToken();

            opNode.addChild(parseTerm(token));

            rootNode = opNode;

            token = currentToken();
            tokenType = token.getType();
        }

        return rootNode;
    }

    private ICodeNode parseTerm(Token token) throws Exception {
        ICodeNode rootNode = parseFactor(token);

        token = currentToken();
        TokenType tokenType = token.getType();

        while(MULT_OPS.contains(tokenType)){
            ICodeNodeType nodeType = MULT_OPS_OPS_MAP.get(tokenType);
            ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
            opNode.addChild(rootNode);

            token = nextToken();

            opNode.addChild(parseFactor(token));

            rootNode = opNode;

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
               String name = token.getText().toLowerCase();
               SymbolTableEntry id = symbolTableStack.lookup(name);
               if(id == null) {
                   errorHandler.flag(token, IDENTIFIER_UNDEFINED, this);
                   id = symbolTableStack.enterLocal(name);
               }

               rootNode = ICodeFactory.createICodeNode(VARIABLE);
               rootNode.setAttribute(ID, id);
               id.appendLineNumber(token.getLineNumber());

               token = nextToken();
               break;
           }
           case INTEGER: {
               rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
               rootNode.setAttribute(VALUE, token.getValue());

               token = nextToken();
               break;
           }
           case REAL: {
               rootNode = ICodeFactory.createICodeNode(REAL_CONSTANT);
               rootNode.setAttribute(VALUE, token.getValue());

               token = nextToken();
               break;
           }
           case STRING: {
               String value = (String) token.getValue();
               rootNode = ICodeFactory.createICodeNode(STRING_CONSTANT);
               rootNode.setAttribute(VALUE,value);

               token = nextToken();
               break;
           }
           case NOT: {
               token = nextToken();

               rootNode = ICodeFactory.createICodeNode(NOT);
               rootNode.addChild(parseFactor(token));
               break;
           }
           case LEFT_PAREN: {
               token = nextToken();

               rootNode = parseExpression(token);

               token = currentToken();
               if(token.getType() == RIGHT_PAREN) {
                   token = nextToken();
               }else{
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
}
