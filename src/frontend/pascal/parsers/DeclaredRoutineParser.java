package frontend.pascal.parsers;

import frontend.Token;
import frontend.TokenType;
import frontend.pascal.PascalErrorCode;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.*;
import intermediate.symtabimpl.DefinitionImpl;
import intermediate.symtabimpl.Predefined;
import intermediate.typeimpl.TypeFormImpl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static frontend.pascal.PascalErrorCode.IDENTIFIER_REDEFINED;
import static frontend.pascal.PascalErrorCode.MISSING_IDENTIFIER;
import static frontend.pascal.PascalTokenType.*;
import static intermediate.symtabimpl.DefinitionImpl.*;
import static intermediate.symtabimpl.RoutineCodeImpl.DECLARED;
import static intermediate.symtabimpl.RoutineCodeImpl.FORWARD;
import static intermediate.symtabimpl.SymbolTableKeyImp.*;

public class DeclaredRoutineParser extends PascalParserTD {

    // counter for dummy routine names
    private static int dummyCounter = 0;

    // entry of parent routine's name
    private SymbolTableEntry parentId;

    // Synchronization set for a formal parameter sublist.
    private static final EnumSet<PascalTokenType> PARAMETER_SET = DeclarationsParser.DECLARATION_START_SET.clone();

    static {
        PARAMETER_SET.add(VAR);
        PARAMETER_SET.add(IDENTIFIER);
        PARAMETER_SET.add(RIGHT_PAREN);
    }

    // Synchronization set for the opening left parenthesis.
    private static final EnumSet<PascalTokenType> LEFT_PAREN_SET = DeclarationsParser.DECLARATION_START_SET.clone();

    static {
        LEFT_PAREN_SET.add(LEFT_PAREN);
        LEFT_PAREN_SET.add(SEMICOLON);
        LEFT_PAREN_SET.add(COLON);
    }

    // Synchronization set for the closing right parenthesis.
    private static final EnumSet<PascalTokenType> RIGHT_PAREN_SET = LEFT_PAREN_SET.clone();

    static {
        RIGHT_PAREN_SET.remove(LEFT_PAREN);
        RIGHT_PAREN_SET.add(RIGHT_PAREN);
    }

    // Synchronization set to follow a formal parameter identifier.
    private static final EnumSet<PascalTokenType> PARAMETER_FOLLOW_SET = EnumSet.of(COLON, RIGHT_PAREN, SEMICOLON);

    static {
        PARAMETER_FOLLOW_SET.addAll(DeclarationsParser.DECLARATION_START_SET);
    }

    // Synchronization set for the , token.
    private static final EnumSet<PascalTokenType> COMMA_SET = EnumSet.of(COMMA, COLON, IDENTIFIER, RIGHT_PAREN, SEMICOLON);

    static {
        COMMA_SET.addAll(DeclarationsParser.DECLARATION_START_SET);
    }

    public DeclaredRoutineParser(PascalParserTD parent) {
        super(parent);
    }

    public SymbolTableEntry parse(Token token, SymbolTableEntry parentId) throws Exception {
        Definition routineDefinition = null;
        String dummyName = null;
        SymbolTableEntry routineId = null;
        TokenType routineType = token.getType();

        // Initialize
        switch ((PascalTokenType) routineType) {
            case PROGRAM: {
                // consume PROGRAM
                token = nextToken();
                routineDefinition = DefinitionImpl.PROGRAM;
                dummyName = "DummyProgramName".toLowerCase();
                break;
            }
            case PROCEDURE: {
                // consume PROCEDURE
                token = nextToken();
                routineDefinition = DefinitionImpl.PROCEDURE;
                dummyName = "DummyProcedureName_".toLowerCase() + String.format("%03d", ++dummyCounter);
                break;
            }
            case FUNCTION: {
                // consume FUNCTION
                token = nextToken();
                routineDefinition = DefinitionImpl.FUNCTION;
                dummyName = "DummyFunctionName_".toLowerCase() + String.format("%03d", ++dummyCounter);
                break;
            }
            default: {
                routineDefinition = DefinitionImpl.PROGRAM;
                dummyName = "DummyProgramName".toLowerCase();
                break;
            }
        }


        // Parse the routine name
        routineId = parseRoutineName(token, dummyName);
        routineId.setDefinition(routineDefinition);

        token = currentToken();

        // Create new intermediate code for the routine
        ICode iCode = ICodeFactory.createICode();
        routineId.setAttribute(ROUTINE_ICODE, iCode);
        routineId.setAttribute(ROUTINE_ROUTINES, new ArrayList<SymbolTableEntry>());

        // Push the routine's new symbol table onto the stack
        // If it was forwarded, push it's existing symbol table
        if (routineId.getAttribute(ROUTINE_ICODE) == FORWARD) {
            SymbolTable symbolTable = (SymbolTable) routineId.getAttribute(ROUTINE_SYMTAB);
            symbolTableStack.push(symbolTable);
        } else {
            routineId.setAttribute(ROUTINE_SYMTAB, symbolTableStack.push());
        }

        // Program: Set the program identifier in the symbol table stack
        if (routineDefinition == DefinitionImpl.PROGRAM) {
            symbolTableStack.setProgramId(routineId);
        } else if (routineId.getAttribute(ROUTINE_CODE) != FORWARD) {
            List<SymbolTableEntry> subroutines =
                    (List<SymbolTableEntry>) parentId.getAttribute(ROUTINE_ROUTINES);
            subroutines.add(routineId);
        }

        // If the routine was forwarded, there should not be
        // any formal parameters or a function return type.
        // But parse them anyway if they&apos;re there.
        if (routineId.getAttribute(ROUTINE_CODE) == FORWARD) {
            if (token.getType() != SEMICOLON) {
                errorHandler.flag(token, PascalErrorCode.ALREADY_FORWARDED, this);
                parseHeader(token, routineId);
            }
        }
        // Parse the routine's formal parameters and function return type.
        else {
            parseHeader(token, routineId);
        }

        // Look for the semicolon
        token = currentToken();
        if (token.getType() == SEMICOLON) {
            do {
                // consume ;
                token = nextToken();
            } while (token.getType() == SEMICOLON);
        } else {
            errorHandler.flag(token, PascalErrorCode.MISSING_SEMICOLON, this);
        }

        // Parse the routine&apos;s block or forward declaration
        if ((token.getType() == IDENTIFIER)
                && (token.getText().equalsIgnoreCase("forward"))) {
            // consume forward
            token = nextToken();
            routineId.setAttribute(ROUTINE_CODE, FORWARD);
        } else {
            routineId.setAttribute(ROUTINE_CODE, DECLARED);

            BlockParser blockParser = new BlockParser(this);
            ICodeNode rootNode = blockParser.parse(token, routineId);
            iCode.setRoot(rootNode);
        }

        // Pop the routine's symbol table off the stack
        symbolTableStack.pop();

        return routineId;
    }

    private SymbolTableEntry parseRoutineName(Token token, String dummyName) throws Exception {
        SymbolTableEntry routineId = null;

        // Parse the routine name identifier
        if (token.getType() == IDENTIFIER) {
            String routineName = token.getText().toLowerCase();
            routineId = symbolTableStack.lookupLocal(routineName);

            // Not already defined locally: Enter into the local symbol table.
            if (routineId == null) {
                routineId = symbolTableStack.enterLocal(routineName);
            }
            // If already defined, it should be a forward definition.
            else if (routineId.getAttribute(ROUTINE_CODE) != FORWARD) {
                routineId = null;
                errorHandler.flag(token, IDENTIFIER_REDEFINED, this);
            }

            // consume the routine name identifier
            token = nextToken();
        } else {
            errorHandler.flag(token, MISSING_IDENTIFIER, this);
        }

        // If necessary, create a dummy routine name symbol table entry
        if (routineId == null) {
            routineId = symbolTableStack.enterLocal(dummyName);
        }

        return routineId;
    }

    private void parseHeader(Token token, SymbolTableEntry routineId) throws Exception {
        // Parse the routine's formal parameters
        parseFormalParameters(token, routineId);
        token = currentToken();

        // If this is a function, parse and set its return type
        if (routineId.getDefinition() == DefinitionImpl.FUNCTION) {
            VariableDeclarationsParser variableDeclarationsParser
                    = new VariableDeclarationsParser(this);
            variableDeclarationsParser.setDefinition(DefinitionImpl.FUNCTION);
            TypeSpec type = variableDeclarationsParser.parseTypeSpec(token);

            token = currentToken();

            // The return type cannot be an array of record
            if (type != null) {
                TypeForm form = type.getForm();
                if ((form == TypeFormImpl.ARRAY)
                        || (form == TypeFormImpl.RECORD)) {
                    errorHandler.flag(token, PascalErrorCode.INVALID_TYPE, this);
                }
            }
            // Missing return type
            else {
                type = Predefined.undefinedType;
            }

            routineId.setTypeSpec(type);
            token = currentToken();
        }

    }

    protected void parseFormalParameters(Token token, SymbolTableEntry routineId) throws Exception {
        // Parse the formal parameters if there is an opening left parenthesis
        token = synchronize(LEFT_PAREN_SET);
        if (token.getType() == LEFT_PAREN) {
            // consume (
            token = nextToken();

            List<SymbolTableEntry> parameters = new ArrayList<>();
            TokenType tokenType = token.getType();

            // Look to parse sub lists of formal parameter declarations
            while ((tokenType == IDENTIFIER) || (tokenType == VAR)) {
                parameters.addAll(parseParmSublist(token, routineId));
                token = currentToken();
                tokenType = token.getType();
            }

            // Closing right parenthesis
            if (token.getType() == RIGHT_PAREN) {
                // consume )
                token = nextToken();
            } else {
                errorHandler.flag(token, PascalErrorCode.MISSING_RIGHT_PAREN, this);
            }

            routineId.setAttribute(ROUTINE_PARMS, parameters);
        }
    }

    private List<SymbolTableEntry> parseParmSublist(Token token, SymbolTableEntry routineId) throws Exception {
        boolean isProgram = routineId.getDefinition() == DefinitionImpl.PROGRAM;
        Definition parameterDefinition = isProgram ? PROGRAM_PARM : null;
        TokenType tokenType = token.getType();

        // VAR of Value Parameter
        if (tokenType == VAR) {
            if (!isProgram) parameterDefinition = VAR_PARM;
            else errorHandler.flag(token, PascalErrorCode.INVALID_VAR_PARM, this);

            // consume var
            token = nextToken();
        } else if (!isProgram) {
            parameterDefinition = VALUE_PARM;
        }

        // Parse the parameter sublist and its type specification
        VariableDeclarationsParser variableDeclarationsParser =
                new VariableDeclarationsParser(this);
        variableDeclarationsParser.setDefinition(parameterDefinition);
        List<SymbolTableEntry> sublist
                = variableDeclarationsParser.parseIdentifierSublist(token, PARAMETER_FOLLOW_SET, COMMA_SET);

        token = currentToken();
        tokenType = token.getType();

        if (!isProgram) {
            // Look for one or more semicolons after a sublist
            if (tokenType == SEMICOLON) {
                while (token.getType() == SEMICOLON) {
                    // consume the ;
                    token = nextToken();
                }
            }

            // If at the start of the next sublist, then missing a semicolon
            else if (VariableDeclarationsParser.NEXT_START_SET.contains(tokenType)) {
                errorHandler.flag(token, PascalErrorCode.MISSING_SEMICOLON, this);
            }

            token = synchronize(PARAMETER_SET);
        }

        return sublist;
    }
}
