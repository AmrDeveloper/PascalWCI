package frontend.pascal;

import frontend.*;
import frontend.pascal.parsers.BlockParser;
import frontend.pascal.parsers.ProgramParser;
import frontend.pascal.parsers.StatementParser;
import intermediate.ICodeFactory;
import intermediate.ICodeNode;
import intermediate.SymbolTableEntry;
import intermediate.symtabimpl.DefinitionImpl;
import intermediate.symtabimpl.Predefined;
import message.Message;
import message.MessageType;

import java.io.IOException;
import java.util.EnumSet;

import static frontend.pascal.PascalErrorCode.*;
import static frontend.pascal.PascalTokenType.*;
import static intermediate.symtabimpl.SymbolTableKeyImp.ROUTINE_ICODE;
import static intermediate.symtabimpl.SymbolTableKeyImp.ROUTINE_SYMTAB;
import static message.MessageType.PARSER_SUMMARY;
import static message.MessageType.TOKEN;

// Top Down Pascal Parser
public class PascalParserTD extends Parser {

    protected static PascalErrorHandler errorHandler = new PascalErrorHandler();

    public PascalParserTD(Scanner scanner) {
        super(scanner);
    }

    public PascalParserTD(PascalParserTD parent) {
       super(parent.getScanner());
    }

    @Override
    public void parse() throws Exception {
        long startTime = System.currentTimeMillis();

        iCode = ICodeFactory.createICode();
        Predefined.initialize(symbolTableStack);

        try {
            Token token = nextToken();

            ProgramParser programParser = new ProgramParser(this);
            programParser.parse(token, null);
            token = currentToken();

            float elapsedTime = (System.currentTimeMillis() - startTime) / 1000f;
            sendMessage(new Message(PARSER_SUMMARY, new Number[] {
                    token.getLineNumber(),
                    getErrorCount(),
                    elapsedTime
            }));
        } catch (IOException ex) {
            errorHandler.abortTranslation(IO_ERROR, this);
        }

    }

    public Token synchronize(EnumSet syncSet) throws Exception{
        Token token = currentToken();

        if(!syncSet.contains(token.getType())) {
            errorHandler.flag(token, UNEXPECTED_TOKEN, this);

            do {
               token = nextToken();
            }while (!(token instanceof EofToken) && !syncSet.contains(token.getType()));
        }
        return token;
    }

    @Override
    public int getErrorCount() {
        return errorHandler.getErrorCount();
    }
}
