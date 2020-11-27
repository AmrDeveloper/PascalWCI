package frontend.pascal.parsers;

import frontend.Token;
import frontend.TokenType;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.SymbolTableEntry;
import intermediate.TypeFactory;
import intermediate.TypeSpec;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static frontend.pascal.PascalErrorCode.*;
import static frontend.pascal.PascalTokenType.*;
import static intermediate.symtabimpl.DefinitionImpl.ENUMERATION_CONSTANT;
import static intermediate.symtabimpl.SymbolTableKeyImp.CONSTANT_VALUE;
import static intermediate.typeimpl.TypeFormImpl.ENUMERATION;
import static intermediate.typeimpl.TypeKeyImpl.ENUMERATION_CONSTANTS;

public class EnumerationTypeParser extends PascalParserTD {

    // Synchronization set to start an enumeration constant.
    private static final EnumSet<PascalTokenType> ENUM_CONSTANT_START_SET = EnumSet.of(IDENTIFIER, COMMA);

    // Synchronization set to follow an enumeration definition.
    private static final EnumSet<PascalTokenType> ENUM_DEFINITION_FOLLOW_SET = EnumSet.of(RIGHT_PAREN, SEMICOLON);

    static {
        ENUM_DEFINITION_FOLLOW_SET.addAll(DeclarationsParser.VAR_START_SET);
    }

    public EnumerationTypeParser(PascalParserTD parent) {
        super(parent);
    }

    public TypeSpec parse(Token token) throws Exception {
        TypeSpec enumerationType = TypeFactory.createType(ENUMERATION);
        int value = -1;
        List<SymbolTableEntry> constants = new ArrayList<>();

        // consume the opening (
        token = nextToken();
        do {
            token = synchronize(ENUM_CONSTANT_START_SET);
            parseEnumerationIdentifier(token, ++value, enumerationType, constants);

            token = currentToken();
            TokenType tokenType = token.getType();

            // Look for the comma
            if (tokenType == COMMA) {
                // consume the comma
                token = nextToken();

                if (ENUM_DEFINITION_FOLLOW_SET.contains(token.getType())) {
                    errorHandler.flag(token, MISSING_IDENTIFIER, this);
                }
            } else if (ENUM_CONSTANT_START_SET.contains(tokenType)) {
                errorHandler.flag(token, MISSING_COMMA, this);
            }
        } while (!ENUM_DEFINITION_FOLLOW_SET.contains(token.getType()));

        // look for the closing )
        if (token.getType() == RIGHT_PAREN) {
            // consume the )
            token = nextToken();
        } else {
            errorHandler.flag(token, MISSING_RIGHT_PAREN, this);
        }

        enumerationType.setAttribute(ENUMERATION_CONSTANTS, constants);
        return enumerationType;
    }

    private void parseEnumerationIdentifier(Token token, int value,
                                            TypeSpec enumerationType,
                                            List<SymbolTableEntry> constants) throws Exception {
        TokenType tokenType = token.getType();

        if (tokenType == IDENTIFIER) {
            String name = token.getText().toLowerCase();
            SymbolTableEntry constantId = symbolTableStack.lookupLocal(name);

            if (constantId != null) {
                errorHandler.flag(token, IDENTIFIER_REDEFINED, this);
            } else {
                constantId = symbolTableStack.enterLocal(token.getText());
                constantId.setDefinition(ENUMERATION_CONSTANT);
                constantId.setTypeSpec(enumerationType);
                constantId.setAttribute(CONSTANT_VALUE, value);
                constantId.appendLineNumber(token.getLineNumber());
                constants.add(constantId);
            }

            // consume the identifier
            token = nextToken();
        }
        else {
            errorHandler.flag(token, MISSING_IDENTIFIER, this);
        }
    }

}
