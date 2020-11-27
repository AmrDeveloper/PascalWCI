package frontend.pascal.parsers;

import frontend.Token;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.Definition;
import intermediate.SymbolTableEntry;
import intermediate.TypeSpec;
import intermediate.symtabimpl.DefinitionImpl;

import java.util.EnumSet;

import static frontend.pascal.PascalErrorCode.*;
import static frontend.pascal.PascalTokenType.*;
import static intermediate.symtabimpl.DefinitionImpl.CONSTANT;
import static intermediate.symtabimpl.DefinitionImpl.ENUMERATION_CONSTANT;

public class SimpleTypeParser extends PascalParserTD {

    // Synchronization set for starting a simple type specification.
    static final EnumSet<PascalTokenType> SIMPLE_TYPE_START_SET =
            ConstantDefinitionsParser.CONSTANT_START_SET.clone();
    static {
        SIMPLE_TYPE_START_SET.add(LEFT_PAREN);
        SIMPLE_TYPE_START_SET.add(COMMA);
        SIMPLE_TYPE_START_SET.add(SEMICOLON);
    }

    public SimpleTypeParser(PascalParserTD parent) {
        super(parent);
    }

    public TypeSpec parse(Token token) throws Exception {
        // Synchronize at the start of a simple type specification.
        switch ((PascalTokenType) token.getType()) {
            case IDENTIFIER: {
                String name = token.getText().toLowerCase();
                SymbolTableEntry id = symbolTableStack.lookup(name);

                if(id != null) {
                    Definition definition = id.getDefinition();

                    //It's either a type identifier or the start of subrange type
                    if(definition == DefinitionImpl.TYPE) {
                        id.appendLineNumber(token.getLineNumber());
                        token = nextToken(); // consume the identifier
                        // Return the type of the referent type.
                        return id.getTypeSpec();
                    }
                    else if((definition != CONSTANT) &&
                            (definition != ENUMERATION_CONSTANT)) {
                        errorHandler.flag(token, NOT_TYPE_IDENTIFIER, this);
                        // consume the identifier
                        token = nextToken();
                        return null;
                    }
                    else {
                        SubrangeTypeParser subrangeTypeParser =
                                new SubrangeTypeParser(this);
                        return subrangeTypeParser.parse(token);
                    }
                }
                else {
                    errorHandler.flag(token, IDENTIFIER_UNDEFINED, this);
                    // consume the identifier
                    token = nextToken();
                    return null;
                }
            }
            case LEFT_PAREN: {
                EnumerationTypeParser enumerationTypeParser =
                        new EnumerationTypeParser(this);
                return enumerationTypeParser.parse(token);
            }
            case COMMA:
            case SEMICOLON: {
                errorHandler.flag(token, INVALID_TYPE, this);
                return null;
            }
            default: {
                SubrangeTypeParser subrangeTypeParser =
                        new SubrangeTypeParser(this);
                return subrangeTypeParser.parse(token);
            }
        }
    }
}
