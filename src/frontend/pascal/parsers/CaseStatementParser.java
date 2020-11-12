package frontend.pascal.parsers;

import frontend.Token;
import frontend.pascal.PascalParserTD;
import intermediate.ICodeNode;

public class CaseStatementParser extends StatementParser {

    public CaseStatementParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception{
        return null;
    }
}
