package frontend.pascal.parsers;

import frontend.EofToken;
import frontend.Token;
import frontend.TokenType;
import frontend.pascal.PascalErrorCode;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.ICodeFactory;
import intermediate.ICodeNode;

import static frontend.pascal.PascalTokenType.IDENTIFIER;
import static frontend.pascal.PascalTokenType.SEMICOLON;
import static intermediate.icodeimpl.ICodeKeyImpl.LINE;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.NO_OP;

public class StatementParser extends PascalParserTD {

    public StatementParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception {
        ICodeNode statementNode;

        switch ((PascalTokenType) token.getType()) {
            case BEGIN: {
                CompoundStatementParser compoundParser =
                        new CompoundStatementParser(this);
                statementNode = compoundParser.parse(token);
                break;
            }
            case IDENTIFIER: {
                AssignmentStatementParser assignmentParser =
                        new AssignmentStatementParser(this);
                statementNode = assignmentParser.parse(token);
                break;
            }
            default: {
                statementNode = ICodeFactory.createICodeNode(NO_OP);
                break;
            }
        }

        setLineNumber(statementNode, token);
        return statementNode;
    }

    protected void parseList(Token token, ICodeNode parentNode,
                             PascalTokenType terminator, PascalErrorCode errorCode)
            throws Exception {
         while (!(token instanceof EofToken)
                 && (token.getType() != terminator)) {

             ICodeNode statementNode = parse(token);

             parentNode.addChild(statementNode);

             token = currentToken();
             TokenType tokenType = token.getType();

             if(tokenType == SEMICOLON) {
                 token = nextToken();
             }
             else if(tokenType == IDENTIFIER) {
                 errorHandler.flag(token, PascalErrorCode.MISSING_SEMICOLON, this);
             }
             else if(tokenType != terminator) {
                 errorHandler.flag(token, PascalErrorCode.UNEXPECTED_TOKEN, this);
                 token = nextToken();
             }
         }

         if(token.getType() == terminator) {
             token = nextToken();
         }else {
             errorHandler.flag(token, errorCode, this);
         }
    }

    protected void setLineNumber(ICodeNode node, Token token) {
        if (node != null) {
            node.setAttribute(LINE, token.getLineNumber());
        }
    }
}
