package frontend.pascal.parsers;

import frontend.Token;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.ICodeFactory;
import intermediate.ICodeNode;

import java.util.EnumSet;

import static frontend.pascal.PascalErrorCode.MISSING_DO;
import static frontend.pascal.PascalTokenType.DO;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.*;

public class WhileStatementParser extends StatementParser {

    // Synchronization set for DO.
    private static final EnumSet<PascalTokenType> DO_SET = StatementParser.STMT_START_SET.clone();

    static {
        DO_SET.add(DO);
        DO_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    public WhileStatementParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception {
        // consume the WHILE
        token = nextToken();

        // Create LOOP, TEST and NOT Nodes
        ICodeNode loopNode = ICodeFactory.createICodeNode(LOOP);
        ICodeNode breakNode = ICodeFactory.createICodeNode(TEST);
        ICodeNode notNode = ICodeFactory.createICodeNode(NOT);

        // The LOOP node adopts the TEST node as its first child.
        // The TEST node adopts the NOT node as its only child.
        loopNode.addChild(breakNode);
        breakNode.addChild(notNode);

        // Parse the expression.
        // The NOT node adopts the expression subtree as its only child.
        ExpressionParser expressionParser = new ExpressionParser(this);
        notNode.addChild(expressionParser.parse(token));

        // Synchronize at the DO.
        token = synchronize(DO_SET);
        if(token.getType() == DO) {
            // consume the DO
            token = nextToken();
        }else{
            errorHandler.flag(token, MISSING_DO, this);
        }

        // Parse the statement.
        // The LOOP node adopts the statement subtree as its second child.
        StatementParser statementParser = new StatementParser(this);
        loopNode.addChild(statementParser.parse(token));

        return loopNode;
    }
}
