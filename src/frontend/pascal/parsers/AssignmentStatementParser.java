package frontend.pascal.parsers;

import frontend.Token;
import frontend.pascal.PascalErrorCode;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.ICodeFactory;
import intermediate.ICodeNode;
import intermediate.SymbolTableEntry;
import intermediate.TypeSpec;
import intermediate.symtabimpl.Predefined;
import intermediate.typeimpl.TypeChecker;

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
        // Create the ASSIGN node.
        ICodeNode assignNode = ICodeFactory.createICodeNode(ASSIGN);

        // Parse the target variable
        VariableParser variableParser = new VariableParser(this);
        ICodeNode targetNode = variableParser.parse(token);
        TypeSpec targetType = (targetNode != null)
                ?  targetNode.getTypeSpec()
                : Predefined.undefinedType;

        // The ASSIGN node adopts the variable node as its first child
        assignNode.addChild(targetNode);

        // Synchronize on the := token
        token = synchronize(COLON_EQUALS_SET);
        if(token.getType() == COLON_EQUALS) {
            // consume the :=
            token = nextToken();
        }
        else {
            errorHandler.flag(token, PascalErrorCode.MISSING_COLON_EQUALS, this);
        }

        // Parse the expression.  The ASSIGN node adopts the expression's
        // node as its second child.
        ExpressionParser expressionParser = new ExpressionParser(this);
        ICodeNode exprNode = expressionParser.parse(token);
        assignNode.addChild(exprNode);

        // Type check: assignment Compatible
        TypeSpec exprType = (exprNode != null)
                ? exprNode.getTypeSpec()
                : Predefined.undefinedType;

        if(!TypeChecker.areAssignmentCompatible(targetType, exprType)) {
            errorHandler.flag(token, PascalErrorCode.INCOMPATIBLE_TYPES, this);
        }

        assignNode.setTypeSpec(targetType);
        return assignNode;
    }
}
