package frontend.pascal.parsers;

import frontend.Token;
import frontend.pascal.PascalParserTD;
import intermediate.ICodeFactory;
import intermediate.ICodeNode;

import static frontend.pascal.PascalErrorCode.MISSING_END;
import static frontend.pascal.PascalTokenType.END;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.COMPOUND;

public class CompoundStatementParser  extends PascalParserTD {

    public CompoundStatementParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception {
        token = nextToken();

        //Create the compound node
        ICodeNode compoundNode = ICodeFactory.createICodeNode(COMPOUND);

        //Parse the statement list terminated by the END token
        StatementParser statementParser = new StatementParser(this);
        statementParser.parseList(token, compoundNode, END, MISSING_END);

        return compoundNode;
    }
}
