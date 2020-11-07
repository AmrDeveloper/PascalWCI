package frontend.pascal.tokens;

import frontend.Source;

import static frontend.pascal.PascalErrorCode.INVALID_CHARACTER;
import static frontend.pascal.PascalTokenType.ERROR;
import static frontend.pascal.PascalTokenType.SPECIAL_SYMBOLS;

public class PascalSpecialSymbolToken extends PascalToken{

    public PascalSpecialSymbolToken(Source source) throws Exception {
        super(source);
    }

    @Override
    protected void extract() throws Exception {
        char currentChar = currentChar();
        text = Character.toString(currentChar);
        type = null;

        switch (currentChar) {
            case '+':
            case '-':
            case '*':
            case '/':
            case ',':
            case ';':
            case '\'':
            case '=':
            case '(':
            case ')':

            case '[':
            case ']':
            case '{':
            case '}':
            case '^': {
                nextChar();
                break;
            }
            case ':' :{
                currentChar = nextChar();

                if(currentChar == '=') {
                    text += currentChar;
                    nextChar();
                }
                break;
            }

            case '<' : {
                currentChar = nextChar();

                if(currentChar == '=') {
                    text += currentChar;
                    nextChar();
                }
                else if(currentChar == '>') {
                    text += currentChar;
                    nextChar();
                }

                break;
            }

            case '>': {
                currentChar =nextChar();

                if(currentChar == '.') {
                    text += currentChar;
                    nextChar();
                }
                break;
            }

            default: {
                nextChar();
                type = ERROR;
                value = INVALID_CHARACTER;
            }
        }

        if(type == null) {
            type = SPECIAL_SYMBOLS.get(text);
        }
    }

}
