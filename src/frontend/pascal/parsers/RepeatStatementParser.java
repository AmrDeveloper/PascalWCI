package frontend.pascal.parsers;

import frontend.Scanner;
import frontend.Token;
import frontend.pascal.PascalParserTD;
import intermediate.ICodeFactory;
import intermediate.ICodeNode;
import intermediate.TypeSpec;
import intermediate.symtabimpl.Predefined;
import intermediate.typeimpl.TypeChecker;

import static frontend.pascal.PascalErrorCode.INCOMPATIBLE_TYPES;
import static frontend.pascal.PascalErrorCode.MISSING_UNTIL;
import static frontend.pascal.PascalTokenType.UNTIL;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.LOOP;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.TEST;

public class RepeatStatementParser extends StatementParser {

    public RepeatStatementParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception{
        // Consume the REPEAT
        token = nextToken();

        // Create the LOOP and TEST nodes
        ICodeNode loopNode = ICodeFactory.createICodeNode(LOOP);
        ICodeNode testNode = ICodeFactory.createICodeNode(TEST);

        // Parse the statement list terminated by the UNTIL token.
        // The LOOP node is the parent of the statement subtrees.
        StatementParser statementParser = new StatementParser(this);
        statementParser.parseList(token, loopNode, UNTIL, MISSING_UNTIL);
        token = currentToken();

        // Parse the expression
        // The TEST node adopts the expression subtree as its only child.
        // The LOOP node adopts the TEST node.
        ExpressionParser expressionParser = new ExpressionParser(this);
        ICodeNode exprNode = expressionParser.parse(token);
        testNode.addChild(exprNode);
        loopNode.addChild(testNode);

        // Type Check: the test expression must be boolean
        TypeSpec exprType = (exprNode != null)
                ? exprNode.getTypeSpec()
                : Predefined.undefinedType;

        if(!TypeChecker.isBoolean(exprType)) {
            errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
        }

        return loopNode;
    }
}
