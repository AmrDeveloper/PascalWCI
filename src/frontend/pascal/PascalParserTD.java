package frontend.pascal;

import frontend.*;
import frontend.pascal.parsers.StatementParser;
import intermediate.ICodeFactory;
import intermediate.ICodeNode;
import intermediate.SymbolTableEntry;
import message.Message;
import message.MessageType;

import java.io.IOException;

import static frontend.pascal.PascalErrorCode.*;
import static frontend.pascal.PascalTokenType.*;
import static message.MessageType.PARSER_SUMMARY;
import static message.MessageType.TOKEN;

//Top Down Pascal Parser
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

        try {
            Token token = nextToken();
            ICodeNode rootNode = null;;

            if(token.getType() == BEGIN) {
                StatementParser statementParser = new StatementParser(this);
                rootNode = statementParser.parse(token);
                token = currentToken();
            }
            else{
                errorHandler.flag(token, UNEXPECTED_TOKEN, this);
            }

            if(token.getType() != DOT) {
                errorHandler.flag(token, MISSING_PERIOD, this);
            }

            if(rootNode != null) {
                iCode.setRoot(rootNode);
            }

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

    @Override
    public int getErrorCount() {
        return errorHandler.getErrorCount();
    }
}
