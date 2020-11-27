package frontend.pascal.parsers;

import frontend.Token;
import frontend.TokenType;
import frontend.pascal.PascalParserTD;
import intermediate.ICodeFactory;
import intermediate.ICodeNode;
import intermediate.SymbolTableEntry;

import static frontend.pascal.PascalErrorCode.MISSING_BEGIN;
import static frontend.pascal.PascalErrorCode.MISSING_END;
import static frontend.pascal.PascalTokenType.BEGIN;
import static frontend.pascal.PascalTokenType.END;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.COMPOUND;

public class BlockParser extends PascalParserTD {

    public BlockParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token, SymbolTableEntry entry) throws Exception {
        DeclarationsParser declarationParser = new DeclarationsParser(this);
        StatementParser statementParser = new StatementParser(this);

        // Parse any declarations.
        declarationParser.parse(token);

        token = synchronize(StatementParser.STMT_START_SET);
        TokenType tokenType = token.getType();
        ICodeNode rootNode = null;

        // Look for the BEGIN token to parse a compound statement.
        if(tokenType == BEGIN) {
            rootNode = statementParser.parse(token);
        }
        // Missing BEGIN: Attempt to parse anyway if possible
        else{
            errorHandler.flag(token, MISSING_BEGIN, this);

            if(StatementParser.STMT_START_SET.contains(tokenType)) {
                rootNode = ICodeFactory.createICodeNode(COMPOUND);
                statementParser.parseList(token, rootNode, END, MISSING_END);
            }
        }

        return rootNode;
    }
}
