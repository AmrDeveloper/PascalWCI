package frontend.pascal.tokens;

import frontend.Source;
import frontend.pascal.PascalTokenType;

import static frontend.pascal.PascalTokenType.IDENTIFIER;
import static frontend.pascal.PascalTokenType.RESERVED_WORDS;

public class PascalWordToken extends PascalToken {

    public PascalWordToken(Source source) throws Exception {
        super(source);
    }

    @Override
    protected void extract() throws Exception {
        StringBuilder textBuffer = new StringBuilder();
        char currentChar = currentChar();

        while (Character.isLetterOrDigit(currentChar)) {
            textBuffer.append(currentChar);
            currentChar = currentChar();
        }

        text = textBuffer.toString();

        //Check if this words is reserved word or an identifier
        type = (RESERVED_WORDS.contains(text.toLowerCase()))
                ? PascalTokenType.valueOf(text.toUpperCase()) : IDENTIFIER;
    }
}
