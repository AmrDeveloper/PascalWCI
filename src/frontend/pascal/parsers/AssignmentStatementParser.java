package frontend.pascal.parsers;

import frontend.Token;
import frontend.pascal.PascalErrorCode;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.ICodeFactory;
import intermediate.ICodeNode;
import intermediate.SymbolTableEntry;

import java.util.EnumSet;

import static frontend.pascal.PascalTokenType.COLON_EQUALS;
import static intermediate.icodeimpl.ICodeKeyImpl.ID;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.ASSIGN;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.VARIABLE;

public class AssignmentStatementParser extends PascalParserTD {

    // Synchronization set for the := token.
    private static final EnumSet<PascalTokenType> COLON_EQUALS_SET =
            ExpressionParser.EXPR_START_SET.clone();

    static {
        COLON_EQUALS_SET.add(COLON_EQUALS);
        COLON_EQUALS_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    public AssignmentStatementParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception {
        ICodeNode assignNode = ICodeFactory.createICodeNode(ASSIGN);

        // Look up the target identifier in the symbol table stack
        String targetName = token.getText().toLowerCase();
        SymbolTableEntry targetId = symbolTableStack.lookup(targetName);
        if(targetId == null) {
            targetId = symbolTableStack.enterLocal(targetName);
        }
        targetId.appendLineNumber(token.getLineNumber());

        token = nextToken();

        ICodeNode variableNode = ICodeFactory.createICodeNode(VARIABLE);
        variableNode.setAttribute(ID, targetId);

        assignNode.addChild(variableNode);

        // Synchronize on the := token.
        token = synchronize(COLON_EQUALS_SET);
        if(token.getType() == COLON_EQUALS) {
            token = nextToken();
        }
        else {
            errorHandler.flag(token, PascalErrorCode.MISSING_COLON_EQUALS, this);
        }

        ExpressionParser expressionParser = new ExpressionParser(this);
        assignNode.addChild(expressionParser.parse(token));

        return assignNode;
    }
}
