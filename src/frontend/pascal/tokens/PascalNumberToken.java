package frontend.pascal.tokens;

import frontend.Source;

import static frontend.pascal.PascalErrorCode.*;
import static frontend.pascal.PascalTokenType.*;
import static java.lang.Double.MAX_EXPONENT;

public class PascalNumberToken extends PascalToken{

    public PascalNumberToken(Source source) throws Exception {
        super(source);
    }

    @Override
    protected void extract() throws Exception {
        StringBuilder textBuffer = new StringBuilder();
        extractNumber(textBuffer);
        text = textBuffer.toString();
    }

    protected void extractNumber(StringBuilder textBuffer) throws Exception{
        String wholeDigits = null;          //digits before the decimal point
        String fractionDigits = null;       //digits after the decimal point
        String exponentDigits = null;       //exponent digits
        char exponentSign = '+';            //sign + or -
        boolean sawDotDot = false;          //true if saw .. token
        char currentChar;

        type = INTEGER;

        wholeDigits = unsingedIntegerDigits(textBuffer);
        if(type == ERROR) {
            return;
        }

        currentChar = currentChar();
        if(currentChar == '.') {
            if(peekChar() == '.') {
                sawDotDot = true;
            }else{
                type = REAL;
                textBuffer.append(currentChar);

                fractionDigits = unsingedIntegerDigits(textBuffer);
                if(type == ERROR) {
                    return;
                }
            }
        }

        currentChar = currentChar();
        if(!sawDotDot && ((currentChar == 'E') || currentChar == 'e')){
            type = REAL;
            textBuffer.append(currentChar);
            currentChar = nextChar();

            if((currentChar == '+') || currentChar == '-') {
                textBuffer.append(currentChar);
                exponentSign = currentChar;
                currentChar = nextChar();
            }

            exponentDigits =  unsingedIntegerDigits(textBuffer);
        }

        if(type == INTEGER) {
            int integerValue = computeIntegerValue(wholeDigits);

            if(type != ERROR) {
                value = new Integer(integerValue);
            }
        }
        else if(type == REAL) {
            float floatValue = computeFloatValue(wholeDigits, fractionDigits, exponentDigits, exponentSign);
            if(type != ERROR) {
                value = new Float(floatValue);
            }
        }
    }

    private String unsingedIntegerDigits(StringBuilder textBuffer) throws Exception {
        char currentChar = currentChar();

        if(!Character.isDigit(currentChar)) {
            type = ERROR;
            value = INVALID_NUMBER;
            return null;
        }

        StringBuilder digits = new StringBuilder();
        while (Character.isDigit(currentChar)) {
            textBuffer.append(currentChar);
            digits.append(currentChar);
            currentChar = nextChar();
        }

        return digits.toString();
    }

    private int computeIntegerValue(String digits) {
        if(digits == null) {
            return 0;
        }

        int integerValue = 0;
        int prevValue = -1;
        int index = 0;

        while((index < digits.length()) && (integerValue >= prevValue)) {
            prevValue = integerValue;
            integerValue = 10 * integerValue + Character.getNumericValue(digits.charAt(index++));
        }

        //No over flow
        if(integerValue >= prevValue) {
            return integerValue;
        }
        //Overflow: set the integer out or the range error
        else {
            type = ERROR;
            value = RANGE_INTEGER;
            return 0;
        }
    }

    private float computeFloatValue(String wholeDigits, String fractionDigits,
                                    String exponentDigits, char exponentSign) {
        double floatValue = 0.0;
        int exponentValue = computeIntegerValue(exponentDigits);
        String digits = wholeDigits;

        if(exponentSign == '-') {
            exponentValue = -exponentValue;
        }

        if(fractionDigits != null) {
            exponentValue -= fractionDigits.length();
            digits  += fractionDigits;
        }

        // Check for a real number out of range error
        if(Math.abs(exponentSign + wholeDigits.length()) > MAX_EXPONENT) {
            type = ERROR;
            value = RANGE_REAL;
            return 0.0f;
        }

        int index = 0;
        while (index < digits.length()) {
            floatValue = 10 * floatValue + Character.getNumericValue(digits.charAt(index++));
        }

        if(exponentValue != 0) {
            floatValue *= Math.pow(10, exponentValue);
        }

        return (float) floatValue;
    }
}
