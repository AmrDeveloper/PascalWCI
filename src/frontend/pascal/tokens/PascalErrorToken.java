package frontend.pascal.tokens;

import frontend.Source;
import frontend.pascal.PascalErrorCode;

import static frontend.pascal.PascalTokenType.ERROR;

public class PascalErrorToken extends PascalToken{

    public PascalErrorToken(Source source, PascalErrorCode errorCode, String tokenText)
            throws Exception
    {
        super(source);
        this.text = tokenText;
        this.type = ERROR;
        this.value = errorCode;
    }

    @Override
    protected void extract() throws Exception {

    }
}
