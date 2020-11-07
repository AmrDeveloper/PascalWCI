package frontend.pascal.tokens;

import frontend.Source;

import static frontend.Source.EOF;
import static frontend.pascal.PascalErrorCode.UNEXPECTED_EOF;
import static frontend.pascal.PascalTokenType.ERROR;
import static frontend.pascal.PascalTokenType.STRING;

public class PascalStringToken extends PascalToken {

    public PascalStringToken(Source source) throws Exception {
        super(source);
    }

    @Override
    protected void extract() throws Exception {
        StringBuilder textBuffer = new StringBuilder();
        StringBuilder valueBuffer = new StringBuilder();

        char currentChar = nextChar();
        textBuffer.append('\'');

        do {
            if(Character.isWhitespace(currentChar)) {
                currentChar = ' ';
            }

            if((currentChar != '\'') && (currentChar != EOF)) {
                textBuffer.append(currentChar);
                valueBuffer.append(currentChar);
                currentChar = nextChar();
            }

            if(currentChar == '\'') {
                while ((currentChar == '\'') && (peekChar() == '\'')) {
                    textBuffer.append("''");
                    valueBuffer.append(currentChar);
                    currentChar = nextChar();
                    currentChar = nextChar();
                }
            }
        }while ((currentChar != '\'') && (currentChar != EOF));

        if(currentChar == '\'') {
            nextChar();
            textBuffer.append('\'');

            type = STRING;
            value = valueBuffer.toString();
        }else {
            type = ERROR;
            value = UNEXPECTED_EOF;
        }

        text = textBuffer.toString();
    }
}
