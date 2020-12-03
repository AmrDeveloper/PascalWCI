package frontend.pascal.parsers;

import frontend.Token;
import frontend.pascal.PascalErrorCode;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.SymbolTableEntry;

import java.util.EnumSet;

import static frontend.pascal.PascalTokenType.*;

public class ProgramParser extends PascalParserTD {

    // Synchronization set to start a program.
    static final EnumSet<PascalTokenType> PROGRAM_START_SET = EnumSet.of(PROGRAM, SEMICOLON);

    static {
        PROGRAM_START_SET.addAll(DeclarationsParser.DECLARATION_START_SET);
    }

    public ProgramParser(PascalParserTD parent) {
        super(parent);
    }

    public SymbolTableEntry parse(Token token, SymbolTableEntry parentId) throws Exception {
        token = synchronize(PROGRAM_START_SET);

        // Parse the program
        DeclaredRoutineParser routineParser = new DeclaredRoutineParser(this);
        routineParser.parse(token, parentId);

        // Look for the final period
        token = currentToken();
        if(token.getType() != DOT) {
            errorHandler.flag(token, PascalErrorCode.MISSING_PERIOD, this);
        }
        return null;
    }
}
