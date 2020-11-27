package frontend.pascal.parsers;

import frontend.Token;
import frontend.TokenType;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.SymbolTableEntry;
import intermediate.TypeFactory;
import intermediate.TypeForm;
import intermediate.TypeSpec;
import intermediate.typeimpl.TypeFormImpl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static frontend.pascal.PascalErrorCode.*;
import static frontend.pascal.PascalTokenType.*;
import static intermediate.typeimpl.TypeFormImpl.ENUMERATION;
import static intermediate.typeimpl.TypeFormImpl.SUBRANGE;
import static intermediate.typeimpl.TypeKeyImpl.*;

public class ArrayTypeParser extends PascalParserTD {

    // Synchronization set for the [ token.
    private static final EnumSet<PascalTokenType> LEFT_BRACKET_SET = SimpleTypeParser.SIMPLE_TYPE_START_SET.clone();

    static {
        LEFT_BRACKET_SET.add(LEFT_BRACKET);
        LEFT_BRACKET_SET.add(RIGHT_BRACKET);
    }

    // Synchronization set for the ] token.
    private static final EnumSet<PascalTokenType> RIGHT_BRACKET_SET = EnumSet.of(RIGHT_BRACKET, OF, SEMICOLON);

    // Synchronization set for OF.
    private static final EnumSet<PascalTokenType> OF_SET = TypeSpecificationParser.TYPE_START_SET.clone();

    static {
        OF_SET.add(OF);
        OF_SET.add(SEMICOLON);
    }

    // Synchronization set to start an index type.
    private static final EnumSet<PascalTokenType> INDEX_START_SET = SimpleTypeParser.SIMPLE_TYPE_START_SET.clone();

    static {
        INDEX_START_SET.add(COMMA);
    }

    // Synchronization set to end an index type.
    private static final EnumSet<PascalTokenType> INDEX_END_SET = EnumSet.of(RIGHT_BRACKET, OF, SEMICOLON);

    // Synchronization set to follow an index type.
    private static final EnumSet<PascalTokenType> INDEX_FOLLOW_SET = INDEX_START_SET.clone();

    static {
        INDEX_FOLLOW_SET.addAll(INDEX_END_SET);
    }

    public ArrayTypeParser(PascalParserTD parent) {
        super(parent);
    }

    public TypeSpec parse(Token token) throws Exception {
        TypeSpec arrayType = TypeFactory.createType(TypeFormImpl.ARRAY);

        // consume Array
        token = nextToken();

        // Synchronize at the [ token.
        token = synchronize(LEFT_BRACKET_SET);
        if (token.getType() != LEFT_BRACKET) {
            errorHandler.flag(token, MISSING_LEFT_BRACKET, this);
        }

        // Parse the list of index types.
        TypeSpec elementType = parseIndexTypeList(token, arrayType);

        // Synchronize at the ] token
        token = synchronize(RIGHT_BRACKET_SET);
        if (token.getType() == RIGHT_BRACKET) {
            //consume [
            token = nextToken();
        } else {
            errorHandler.flag(token, MISSING_RIGHT_BRACKET, this);
        }

        // Synchronize at OF
        token = synchronize(OF_SET);
        if (token.getType() == OF) {
            // consume OF
            token = nextToken();
        } else {
            errorHandler.flag(token, MISSING_OF, this);
        }

        // Parse the element type
        elementType.setAttribute(ARRAY_ELEMENT_TYPE, parseElementType(token));

        return arrayType;
    }

    private TypeSpec parseIndexTypeList(Token token, TypeSpec arrayType) throws Exception {
        TypeSpec elementType = arrayType;
        boolean anotherIndex = false;

        // consume the [ token
        token = nextToken();

        // Parse the list of index type specifications
        do {
            anotherIndex = false;

            // Parse the index type
            token = synchronize(INDEX_START_SET);
            parseIndexType(token, elementType);

            // Synchronize at the , token
            token = synchronize(INDEX_FOLLOW_SET);
            TokenType tokenType = token.getType();
            if ((tokenType != COMMA) && (tokenType != RIGHT_BRACKET)) {
                if (INDEX_START_SET.contains(tokenType)) {
                    errorHandler.flag(token, MISSING_COMMA, this);
                    anotherIndex = true;
                }
            }

            // Create an ARRAY element type Object
            // for each subsequent index type
            else if (tokenType == COMMA) {
                TypeSpec newElementType = TypeFactory.createType(TypeFormImpl.ARRAY);
                elementType.setAttribute(ARRAY_ELEMENT_TYPE, newElementType);
                elementType = newElementType;

                // consume the , token
                token = nextToken();
                anotherIndex = true;
            }
        } while (anotherIndex);

        return elementType;
    }

    private void parseIndexType(Token token, TypeSpec arrayType) throws Exception {
        SimpleTypeParser simpleTypeParser = new SimpleTypeParser(this);
        TypeSpec indexType = simpleTypeParser.parse(token);
        arrayType.setAttribute(ARRAY_INDEX_TYPE, indexType);

        if(indexType == null) return;

        TypeForm form = indexType.getForm();
        int count = 0;

        // Check the index type and set the element count
        if(form == SUBRANGE) {
            Integer minValue = (Integer) indexType.getAttribute(SUBRANGE_MIN_VALUE);
            Integer maxValue = (Integer) indexType.getAttribute(SUBRANGE_MAX_VALUE);

            if ((minValue != null) && (maxValue != null)) {
                count = maxValue - minValue + 1;
            }
        }
        else if(form == ENUMERATION) {
            List<SymbolTableEntry> constants = (List<SymbolTableEntry>)
                    indexType.getAttribute(ENUMERATION_CONSTANTS);
            count = constants.size();
        }
        else {
            errorHandler.flag(token, INVALID_INDEX_TYPE, this);
        }
        arrayType.setAttribute(ARRAY_ELEMENT_COUNT, count);
    }

    private TypeSpec parseElementType(Token token) throws Exception {
        TypeSpecificationParser typeSpecificationParser =
                new TypeSpecificationParser(this);
        return typeSpecificationParser.parse(token);
    }
}
