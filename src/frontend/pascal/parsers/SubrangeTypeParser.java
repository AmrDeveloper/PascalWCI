package frontend.pascal.parsers;

import frontend.Token;
import frontend.TokenType;
import frontend.pascal.PascalParserTD;
import intermediate.TypeFactory;
import intermediate.TypeSpec;
import intermediate.symtabimpl.Predefined;

import static frontend.pascal.PascalErrorCode.*;
import static frontend.pascal.PascalTokenType.DOT_DOT;
import static frontend.pascal.PascalTokenType.IDENTIFIER;
import static intermediate.typeimpl.TypeFormImpl.ENUMERATION;
import static intermediate.typeimpl.TypeFormImpl.SUBRANGE;
import static intermediate.typeimpl.TypeKeyImpl.*;

public class SubrangeTypeParser extends PascalParserTD {

    public SubrangeTypeParser(PascalParserTD parent) {
        super(parent);
    }

    public TypeSpec parse(Token token) throws Exception {
        TypeSpec subrangeType = TypeFactory.createType(SUBRANGE);
        Object minValue = null;
        Object maxValue = null;

        // Parse the minimum constant
        Token constantToken = token;
        ConstantDefinitionsParser constantParser = new ConstantDefinitionsParser(this);
        minValue = constantParser.parseConstant(token);

        // Set the minimum constant's type
        TypeSpec minType = constantToken.getType() == IDENTIFIER
                ? constantParser.getConstantType(constantToken)
                : constantParser.getConstantType(minValue);

        minValue = checkValueType(constantToken, minValue, minType);

        token = currentToken();
        boolean sawDoubleDots = false;

        // Look for the '..' token
        if (token.getType() == DOT_DOT) {
            // consume the .. token
            token = nextToken();
            sawDoubleDots = true;
        }

        TokenType tokenType = token.getType();

        // At the start of the maximum constant?
        if (ConstantDefinitionsParser.CONSTANT_START_SET.contains(tokenType)) {
            if (!sawDoubleDots) {
                errorHandler.flag(token, MISSING_DOT_DOT, this);
            }

            // Parse the maximum constant
            token = synchronize(ConstantDefinitionsParser.CONSTANT_START_SET);
            constantToken = token;

            maxValue = constantParser.parseConstant(token);

            // Set the maximum constant's type
            TypeSpec maxType = constantToken.getType() == IDENTIFIER
                    ? constantParser.getConstantType(constantToken)
                    : constantParser.getConstantType(maxValue);

            maxValue = checkValueType(constantToken, maxValue, maxType);

            // Are the min and max value types valid
            if ((minType == null) || (maxType == null)) {
                errorHandler.flag(constantToken, INCOMPATIBLE_TYPES, this);
            }

            // Are the min and max value types the same?
            else if (minType != maxType) {
                errorHandler.flag(constantToken, INVALID_SUBRANGE_TYPE, this);
            }

            // Min value > max value?
            else if ((minValue != null) && (maxValue != null) &&
                    ((Integer) minValue >= (Integer) maxValue)) {
                errorHandler.flag(constantToken, MIN_GT_MAX, this);
            }
        } else {
            errorHandler.flag(constantToken, INVALID_SUBRANGE_TYPE, this);
        }

        subrangeType.setAttribute(SUBRANGE_BASE_TYPE, minType);
        subrangeType.setAttribute(SUBRANGE_MIN_VALUE, minValue);
        subrangeType.setAttribute(SUBRANGE_MAX_VALUE, maxValue);
        return subrangeType;
    }

    private Object checkValueType(Token token, Object value, TypeSpec type) {
        if (type == null) {
            return value;
        }
        if (type == Predefined.integerType) {
            return value;
        } else if (type == Predefined.charType) {
            char ch = ((String) value).charAt(0);
            return Character.getNumericValue(ch);
        } else if (type.getForm() == ENUMERATION) return value;
        else {
            errorHandler.flag(token, INVALID_SUBRANGE_TYPE, this);
            return value;
        }
    }
}
