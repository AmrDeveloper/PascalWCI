package frontend.pascal;

import frontend.EofToken;
import frontend.Scanner;
import frontend.Source;
import frontend.Token;
import frontend.pascal.tokens.*;

import static frontend.Source.EOF;
import static frontend.pascal.PascalErrorCode.INVALID_CHARACTER;

public class PascalScanner extends Scanner {

    public PascalScanner(Source source) {
        super(source);
    }

    @Override
    protected Token extractToken() throws Exception {
        skipWhiteSpace();

        Token token;
        char currentChar = currentChar();

        if (currentChar == EOF) {
            //token = new EofToken(source, END_OF_FILE);
            token = new EofToken(source);
        }
        else if(Character.isLetter(currentChar)) {
            token = new PascalWordToken(source);
        }
        else if(Character.isDigit(currentChar)) {
            token = new PascalNumberToken(source);
        }
        else if(PascalTokenType.SPECIAL_SYMBOLS
                .containsKey(Character.toString(currentChar))) {
            token = new PascalSpecialSymbolToken(source);
        }
        else if(currentChar == '\'') {
            token = new PascalStringToken(source);
        }
        else {
            token = new PascalErrorToken(source, INVALID_CHARACTER,
                    Character.toString(currentChar));
            nextChar();
        }
        return token;
    }

    private void skipWhiteSpace() throws Exception {
        char currentChar = currentChar();
        while (Character.isWhitespace(currentChar) || currentChar == '{') {
            if (currentChar == '{') {
                do {
                    currentChar = nextChar();
                } while ((currentChar != '}' && (currentChar != EOF)));

                if (currentChar == '}') {
                    currentChar = nextChar();
                }
            } else {
                currentChar = nextChar();
            }
        }
    }
}
