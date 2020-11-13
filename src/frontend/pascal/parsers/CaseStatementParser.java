package frontend.pascal.parsers;

import frontend.EofToken;
import frontend.Token;
import frontend.TokenType;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.ICodeFactory;
import intermediate.ICodeNode;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static frontend.pascal.PascalErrorCode.*;
import static frontend.pascal.PascalTokenType.*;
import static intermediate.icodeimpl.ICodeKeyImpl.VALUE;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.*;

public class CaseStatementParser extends StatementParser {

    // Synchronization set for starting a CASE option constant.
    private static final EnumSet<PascalTokenType> CONSTANT_START_SET
            = EnumSet.of(IDENTIFIER, INTEGER, PLUS, MINUS, STRING);

    // Synchronization set for OF.
    private static final EnumSet<PascalTokenType> OF_SET = CONSTANT_START_SET.clone();
    static {
        OF_SET.add(OF);
        OF_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    // Synchronization set for COMMA.
    private static final EnumSet<PascalTokenType> COMMA_SET =  CONSTANT_START_SET.clone();
    static {
        COMMA_SET.add(COMMA);
        COMMA_SET.add(COLON);
        COMMA_SET.addAll(StatementParser.STMT_START_SET);
        COMMA_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    public CaseStatementParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception {
        // consume the case
        token = nextToken();

        // create a select node
        ICodeNode selectNode = ICodeFactory.createICodeNode(SELECT);

        // Parse the CASE expression
        // The SELECT node adopts the expression subtree as its first child.
        ExpressionParser expressionParser = new ExpressionParser(this);
        selectNode.addChild(expressionParser.parse(token));

        // Synchronize at the OF
        token = synchronize(OF_SET);
        if(token.getType() == OF) {
            // consume the OF
            token = nextToken();
        }else {
            errorHandler.flag(token, MISSING_OF, this);
        }

        // Set of CASE branch constants
        Set<Object> constantSet = new HashSet<>();

        // Loop to parse each CASE branch until the END token
        // or the end of the source file.
        while (!(token instanceof EofToken) && (token.getType() != END)) {
            // The SELECT node adopts the CASE branch subtree.
            selectNode.addChild(parseBranch(token, constantSet));

            token = currentToken();
            TokenType tokenType = token.getType();

            // Look for the semicolon between CASE branches
            if(tokenType == SEMICOLON) {
                // consume ;
                token = nextToken();
            }
            // If at the start of the next constant, then missing a semicolon.
            else if(CONSTANT_START_SET.contains(tokenType)) {
                errorHandler.flag(token, MISSING_SEMICOLON, this);
            }
        }

        // Look for the END token
        if(token.getType() == END) {
            // consume END
            token = nextToken();
        }else{
            errorHandler.flag(token, MISSING_END, this);
        }

        return selectNode;
    }

    private ICodeNode parseBranch(Token token, Set<Object> constantSet)  throws Exception {
        // Create an SELECT_BRANCH node and a SELECT_CONSTANTS node.
        // The SELECT_BRANCH node adopts the SELECT_CONSTANTS node as its
        // first child.
        ICodeNode branchNode = ICodeFactory.createICodeNode(SELECT_BRANCH);
        ICodeNode constantsNode = ICodeFactory.createICodeNode(SELECT_CONSTANTS);
        branchNode.addChild(constantsNode);

        // Parse the list of CASE branch constants
        // The SELECT_CONSTANTS node adopts each constant.
        parseConstantList(token, constantsNode, constantSet);

        // Look for the : token.
        token = currentToken();
        if(token.getType() == COLON) {
            // consume :
            token = nextToken();
        }else{
            errorHandler.flag(token, MISSING_COLON, this);
        }

        // Parse the CASE branch statement. The SELECT_BRANCH node adopts
        // the statement subtree as its second child.
        StatementParser statementParser = new StatementParser(this);
        branchNode.addChild(statementParser.parse(token));

        return branchNode;
    }

    private void parseConstantList(Token token, ICodeNode constantsNode, Set<Object> constantSet) throws Exception {
        //Loop to parse each constant
        while (CONSTANT_START_SET.contains(token.getType())) {

            // The constant list node adopts the constant node
            constantsNode.addChild(parseConstant(token, constantSet));

            // Synchronize at the comma between constants
            token = synchronize(COMMA_SET);

            // Look for the comma
            if(token.getType() == COMMA) {
                // consume the ,
                token = nextToken();
            }
            // If at the start of the next constant, then missing a comma
            else if(CONSTANT_START_SET.contains(token.getType())) {
                errorHandler.flag(token, MISSING_COMMA, this);
            }
        }
    }

    private ICodeNode parseConstant(Token token, Set<Object> constantSet) throws Exception {
        TokenType sign = null;
        ICodeNode constantNode = null;

        // Synchronize at the start of a constant.
        token = synchronize(CONSTANT_START_SET);
        TokenType tokenType = token.getType();

        // Plus or minus sign ?
        if((tokenType == PLUS) || (tokenType == MINUS)) {
            sign = tokenType;
            // consume sign
            token = nextToken();
        }

        // Parse the constant
        switch ((PascalTokenType) token.getType()) {
            case IDENTIFIER:  {
                constantNode = parseIdentifierConstant(token, sign);
                break;
            }
            case INTEGER: {
                constantNode = parseIntegerConstant(token.getText(), sign);
                break;
            }
            case STRING:  {
                constantNode = parseCharacterConstant(token, (String) token.getValue(), sign);
                break;
            }
            default: {
                errorHandler.flag(token, INVALID_CONSTANT, this);
                break;
            }
        }

        // Check for reused constants
        if(constantNode != null) {
            Object value = constantNode.getAttribute(VALUE);

            if(constantSet.contains(value)) {
                errorHandler.flag(token, CASE_CONSTANT_REUSED, this);
            }else {
                constantSet.add(value);
            }
        }

        // consume the constant
        nextToken();
        return constantNode;
    }

    private ICodeNode parseIdentifierConstant(Token token, TokenType sign) throws Exception {
        // Placeholder: not supported for now
        errorHandler.flag(token, INVALID_CONSTANT, this);
        return null;
    }

    private ICodeNode parseIntegerConstant(String value, TokenType sign) throws Exception {
        ICodeNode constantNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
        int intValue = Integer.parseInt(value);

        if(sign == MINUS) {
            intValue = -intValue;
        }

        constantNode.setAttribute(VALUE, intValue);
        return constantNode;
    }

    private ICodeNode parseCharacterConstant(Token token, String value, TokenType sign) throws  Exception {
        ICodeNode constantNode = null;

        if(sign != null || value.length() != 1) {
            errorHandler.flag(token, INVALID_CONSTANT, this);
        } else {
            constantNode = ICodeFactory.createICodeNode(STRING_CONSTANT);
            constantNode.setAttribute(VALUE, value);
        }
        return constantNode;
    }
}
