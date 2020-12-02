package frontend.pascal.parsers;

import frontend.Token;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.ICodeFactory;
import intermediate.ICodeNode;
import intermediate.TypeSpec;
import intermediate.symtabimpl.Predefined;
import intermediate.typeimpl.TypeChecker;

import java.util.EnumSet;

import static frontend.pascal.PascalErrorCode.INCOMPATIBLE_TYPES;
import static frontend.pascal.PascalErrorCode.MISSING_THEN;
import static frontend.pascal.PascalTokenType.ELSE;
import static frontend.pascal.PascalTokenType.THEN;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.IF;

public class IfStatementParser extends StatementParser {

    // Synchronization set for THEN
    private static final EnumSet<PascalTokenType> THEN_SET = StatementParser.STMT_START_SET.clone();

    static {
        THEN_SET.add(THEN);
        THEN_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    public IfStatementParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception {
        // consume the if
        token = nextToken();

        // Create an IF node
        ICodeNode ifNote = ICodeFactory.createICodeNode(IF);

        // Parse the expression
        // The IF node adopts the expression subtree as its first child.
        ExpressionParser expressionParser = new ExpressionParser(this);
        ICodeNode exprNode = expressionParser.parse(token);
        ifNote.addChild(exprNode);

        // Type check: the expression type must be boolean
        TypeSpec exprType = (exprNode != null)
                ? exprNode.getTypeSpec()
                : Predefined.undefinedType;

        if(!TypeChecker.isBoolean(exprType)) {
            errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
        }

        // Synchronize at the THEN
        token = synchronize(THEN_SET);
        if (token.getType() == THEN) {
            // consume the THEN
            token = nextToken();
        } else {
            errorHandler.flag(token, MISSING_THEN, this);
        }

        // Parse the THEN statement
        // The if node adopts the statement subtree as it's second child.
        StatementParser statementParser = new StatementParser(this);
        ifNote.addChild(statementParser.parse(token));
        token = currentToken();

        // Loop for an ELSE
        if (token.getType() == ELSE) {
            // consume the THEN
            token = nextToken();

            // Parse the ELSE statement
            // The If node adopts the statement subtree as it's third child
            ifNote.addChild(statementParser.parse(token));
        }

        return ifNote;
    }
}
