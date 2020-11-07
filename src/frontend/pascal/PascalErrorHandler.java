package frontend.pascal;

import frontend.Parser;
import frontend.Token;
import message.Message;

import static frontend.pascal.PascalErrorCode.TOO_MANY_ERRORS;
import static message.MessageType.SYNTAX_ERROR;

public class PascalErrorHandler {

    private static final int MAX_ERRORS = 25;
    private static int errorCount = 0;   // count of syntax errors

    public void flag(Token token, PascalErrorCode errorCode, Parser parser) {
        parser.sendMessage(new Message(SYNTAX_ERROR, new Object[] {
                token.getLineNumber(),
                token.getPosition(),
                token.getText(),
                errorCode.toString()
        }));

        if(++errorCount > MAX_ERRORS) {
            abortTranslation(TOO_MANY_ERRORS, parser);
        }
    }

    public void abortTranslation(PascalErrorCode errorCode, Parser parser) {
        String fatalText = "FATAL ERROR: " + errorCode.toString();
        parser.sendMessage(new Message(SYNTAX_ERROR, new Object[] {
                0,
                0,
                "",
                fatalText
        }));

        //terminates the program.
        System.exit(errorCode.getStatus());
    }

    public int getErrorCount() {
        return errorCount;
    }
}
