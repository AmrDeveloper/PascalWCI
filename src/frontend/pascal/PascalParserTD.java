package frontend.pascal;

import frontend.*;
import message.Message;
import message.MessageType;

import java.io.IOException;

import static frontend.pascal.PascalErrorCode.IO_ERROR;
import static frontend.pascal.PascalTokenType.ERROR;
import static message.MessageType.TOKEN;

//Top Down Pascal Parser
public class PascalParserTD extends Parser {

    protected static PascalErrorHandler errorHandler = new PascalErrorHandler();

    public PascalParserTD(Scanner scanner) {
        super(scanner);
    }

    @Override
    public void parse() throws Exception {
        Token token;
        long startTime = System.currentTimeMillis();

        try {
            while (!((token = nextToken()) instanceof EofToken)) {
                TokenType tokenType = token.getType();
                if(tokenType != ERROR) {
                    sendMessage(new Message(TOKEN,
                                            new Object[] {token.getLineNumber(),
                                                token.getPosition(),
                                                tokenType,
                                                token.getText(),
                                                token.getValue()}));
                }
                else{
                    errorHandler.flag(token, (PascalErrorCode) token.getValue(), this) ;
                }
            }

            float elapsedTime = (System.currentTimeMillis() - startTime) / 1000f;

            sendMessage(new Message(MessageType.PARSER_SUMMARY, new Number[]{
                    token.getLineNumber(),
                    getErrorCount(),
                    elapsedTime}));

        }catch (IOException ex) {
            errorHandler.abortTranslation(IO_ERROR, this);
        }

    }

    @Override
    public int getErrorCount() {
        return errorHandler.getErrorCount();
    }
}
