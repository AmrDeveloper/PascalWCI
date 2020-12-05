package frontend.pascal.parsers;

import frontend.Token;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.TypeFactory;
import intermediate.TypeSpec;
import intermediate.symtabimpl.DefinitionImpl;
import intermediate.typeimpl.TypeFormImpl;

import java.util.EnumSet;

import static frontend.pascal.PascalErrorCode.MISSING_END;
import static frontend.pascal.PascalTokenType.*;
import static intermediate.typeimpl.TypeKeyImpl.RECORD_SYMTAB;

public class RecordTypeParser extends PascalParserTD {

    // Synchronization set for the END.
    private static final EnumSet<PascalTokenType> END_SET = DeclarationsParser.VAR_START_SET.clone();

    static {
        END_SET.add(END);
        END_SET.add(SEMICOLON);
    }

    public RecordTypeParser(PascalParserTD parent) {
        super(parent);
    }

    public TypeSpec parse(Token token) throws Exception {
        TypeSpec recordType = TypeFactory.createType(TypeFormImpl.RECORD);

        // consume RECORD
        token = nextToken();

        // Push a symbol table for the RECORD type Specification.
        recordType.setAttribute(RECORD_SYMTAB, symbolTableStack.push());

        // Parse the field declarations.
        VariableDeclarationsParser variableDeclarationsParser = new VariableDeclarationsParser(this);
        variableDeclarationsParser.setDefinition(DefinitionImpl.FIELD);
        variableDeclarationsParser.parse(token, null);

        // Pop off the record's symbol table
        symbolTableStack.pop();

        // Synchronize at the END.
        token = synchronize(END_SET);

        // Look for the END
        if(token.getType() == END) {
            // consume END
            token = nextToken();
        }else {
            errorHandler.flag(token, MISSING_END, this);
        }

        return recordType;
    }
}
