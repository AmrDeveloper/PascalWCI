package frontend.pascal.parsers;

import frontend.Token;
import frontend.TokenType;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.SymbolTableEntry;
import intermediate.TypeSpec;
import intermediate.symtabimpl.DefinitionImpl;

import java.util.EnumSet;

import static frontend.pascal.PascalErrorCode.*;
import static frontend.pascal.PascalTokenType.*;

public class TypeDefinitionsParser extends PascalParserTD {

    // Synchronization set for a type identifier
    private static final EnumSet<PascalTokenType> IDENTIFIER_SET = DeclarationsParser.VAR_START_SET.clone();

    static {
        IDENTIFIER_SET.add(IDENTIFIER);
    }

    // Synchronization set for the = token.
    private static final EnumSet<PascalTokenType> EQUALS_SET = ConstantDefinitionsParser.CONSTANT_START_SET.clone();

    static {
        EQUALS_SET.add(EQUALS);
        EQUALS_SET.add(SEMICOLON);
    }

    // Synchronization set for what follows a definition or declaration.
    private static final EnumSet<PascalTokenType> FOLLOW_SET = EnumSet.of(SEMICOLON);
    // Synchronization set for the start of the next definition or declaration.
    private static final EnumSet<PascalTokenType> NEXT_START_SET = DeclarationsParser.VAR_START_SET.clone();

    static {
        NEXT_START_SET.add(SEMICOLON);
        NEXT_START_SET.add(IDENTIFIER);
    }

    public TypeDefinitionsParser(PascalParserTD parent) {
        super(parent);
    }

    public void parse(Token token) throws Exception {
        token = synchronize(IDENTIFIER_SET);

        // Loop to parse a sequence of type definitions
        // separated by semicolons.
        while (token.getType() == IDENTIFIER) {
            String name = token.getText().toLowerCase();
            SymbolTableEntry typeId = symbolTableStack.lookupLocal(name);

            // Enter the new identifier into the symbol table
            // but don't set how it&apos;s defined yet.
            if (typeId == null) {
                typeId = symbolTableStack.enterLocal(name);
                typeId.appendLineNumber(token.getLineNumber());
            } else {
                errorHandler.flag(token, IDENTIFIER_REDEFINED, this);
                typeId = null;
            }

            // consume the identifier token
            token = nextToken();

            // Synchronize on the = token
            token = synchronize(EQUALS_SET);
            if (token.getType() == EQUALS) {
                // consume the =
                token = nextToken();
            } else {
                errorHandler.flag(token, MISSING_EQUALS, this);
            }

            // Parse the type specification
            TypeSpecificationParser typeSpecificationParser = new TypeSpecificationParser(this);
            TypeSpec type = typeSpecificationParser.parse(token);

            // Set identifier to be a type and set its type specification.
            if (typeId != null) {
                typeId.setDefinition(DefinitionImpl.TYPE);
            }

            // Cross-link the type identifier and the type specification.
            if ((type != null) && (typeId != null)) {
                if (type.getIdentifier() == null) {
                    type.setIdentifier(typeId);
                }
                typeId.setTypeSpec(type);
            } else {
                token = synchronize(FOLLOW_SET);
            }

            token = currentToken();
            TokenType tokenType = token.getType();

            // Look for one or more semicolons after a definition.
            if (tokenType == SEMICOLON) {
                while (token.getType() == SEMICOLON) {
                    token = nextToken(); // consume the ;
                }
            }

            // If at the start of the next definition or declaration,
            // then missing a semicolon.
            else if (NEXT_START_SET.contains(tokenType)) {
                errorHandler.flag(token, MISSING_SEMICOLON, this);
            }
            token = synchronize(IDENTIFIER_SET);
        }

    }
}
