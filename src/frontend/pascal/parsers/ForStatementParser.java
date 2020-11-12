package frontend.pascal.parsers;

import frontend.Token;
import frontend.TokenType;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.ICodeFactory;
import intermediate.ICodeNode;

import java.util.EnumSet;

import static frontend.pascal.PascalErrorCode.MISSING_DO;
import static frontend.pascal.PascalErrorCode.MISSING_TO_DOWNTO;
import static frontend.pascal.PascalTokenType.*;
import static intermediate.icodeimpl.ICodeKeyImpl.VALUE;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.*;

public class ForStatementParser extends StatementParser {

    // Synchronization set for TO or DOWNTO.
    static final EnumSet<PascalTokenType> TO_DOWNTO_SET = ExpressionParser.EXPR_START_SET.clone();

    static {
        TO_DOWNTO_SET.add(TO);
        TO_DOWNTO_SET.add(DOWNTO);
        TO_DOWNTO_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    // Synchronization set for DO.
    private static final EnumSet<PascalTokenType> DO_SET = StatementParser.STMT_START_SET.clone();

    static {
        DO_SET.add(DO);
        DO_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    public ForStatementParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception {
        // consume the FOR
        token = nextToken();
        Token targetToken = token;

        // Create the loop COMPOUND, LOOP and TEST nodes.
        ICodeNode compoundNode = ICodeFactory.createICodeNode(COMPOUND);
        ICodeNode loopNode = ICodeFactory.createICodeNode(LOOP);
        ICodeNode testNode = ICodeFactory.createICodeNode(TEST);

        // Parse the embedded initial assignment
        AssignmentStatementParser assignmentParser = new AssignmentStatementParser(this);
        ICodeNode initAssignNode = assignmentParser.parse(token);

        // Set the current line number attribute
        setLineNumber(initAssignNode, targetToken);

        // The COMPOUND node adopts the initial ASSIGN and the LOOP nodes
        // as its first and second children
        compoundNode.addChild(initAssignNode);
        compoundNode.addChild(loopNode);

        // Synchronize at the TO or DOWNTO
        token = synchronize(TO_DOWNTO_SET);
        TokenType direction = token.getType();

        // Look for the TO or DOWNTO
        if((direction == TO) || (direction == DOWNTO)) {
            // Consume the TO or DOWNTO
            token = nextToken();
        }else {
            direction = TO;
            errorHandler.flag(token, MISSING_TO_DOWNTO, this);
        }

        // Create a relational operator node: GT for TO, or LT for DOWNTO.
        ICodeNode relOpNode = ICodeFactory.createICodeNode(direction == TO ? GT : LT);

        // Copy the control VARIABLE node. The relational operator
        // node adopts the copied VARIABLE node as it's first child.
        ICodeNode controlVarNode = initAssignNode.getChildren().get(0);
        relOpNode.addChild(controlVarNode.copy());

        // Parse the termination expression. The relational operator node
        // adopts the expression as its second child.
        ExpressionParser expressionParser = new ExpressionParser(this);
        relOpNode.addChild(expressionParser.parse(token));

        // The TEST node adopts the relational operator node as its only child.
        // The LOOP node adopts the TEST node as its first child.
        testNode.addChild(relOpNode);
        loopNode.addChild(testNode);

        // Synchronize at the DO.
        token = synchronize(DO_SET);
        if(token.getType() == DO) {
            // consume the DO
            token = nextToken();
        }else{
            errorHandler.flag(token, MISSING_DO, this);
        }

        // Parse the nested statement. The LOOP node adopts the statement
        // node as its second child
        StatementParser statementParser = new StatementParser(this);
        loopNode.addChild(statementParser.parse(token));

        // Create an assignment with a copy of the control variable
        // to advance the value of the variable.
        ICodeNode nextAssignNode = ICodeFactory.createICodeNode(ASSIGN);
        nextAssignNode.addChild(controlVarNode.copy());

        // Create the arithmetic operator node:
        // ADD for TO, or SUBTRACT for DOWNTO.
        ICodeNode arithOpNode = ICodeFactory.createICodeNode(direction == TO ? ADD : SUBTRACT);

        // The operator node adopts a copy of the loop variable as its
        // first child and the value 1 as its second child.
        arithOpNode.addChild(controlVarNode.copy());
        ICodeNode oneNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
        oneNode.setAttribute(VALUE, 1);
        arithOpNode.addChild(oneNode);

        // The next ASSIGN node adopts the arithmetic operator node as its
        // second child. The loop node adopts the next ASSIGN node as its
        // third child.
        nextAssignNode.addChild(arithOpNode);
        loopNode.addChild(nextAssignNode);

        // Set the current line number attribute
        setLineNumber(nextAssignNode, targetToken);

        return compoundNode;
    }
}
