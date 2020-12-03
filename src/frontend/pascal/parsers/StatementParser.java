package frontend.pascal.parsers;

import frontend.EofToken;
import frontend.Token;
import frontend.TokenType;
import frontend.pascal.PascalErrorCode;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.Definition;
import intermediate.ICodeFactory;
import intermediate.ICodeNode;
import intermediate.SymbolTableEntry;
import intermediate.symtabimpl.DefinitionImpl;

import java.util.EnumSet;

import static frontend.pascal.PascalErrorCode.MISSING_SEMICOLON;
import static frontend.pascal.PascalErrorCode.UNEXPECTED_TOKEN;
import static frontend.pascal.PascalTokenType.*;
import static intermediate.icodeimpl.ICodeKeyImpl.LINE;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.NO_OP;
import static intermediate.symtabimpl.DefinitionImpl.UNDEFINED;

public class StatementParser extends PascalParserTD {

    // Synchronization set for starting a statement.
    protected static final EnumSet<PascalTokenType> STMT_START_SET =
            EnumSet.of(BEGIN, CASE, FOR, PascalTokenType.IF, REPEAT, WHILE,
            IDENTIFIER, SEMICOLON);

    // Synchronization set for following a statement.
    protected static final EnumSet<PascalTokenType> STMT_FOLLOW_SET = EnumSet.of(SEMICOLON, END, ELSE, UNTIL, DOT);

    public StatementParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception {
        ICodeNode statementNode = null;

        switch ((PascalTokenType) token.getType()) {
            case BEGIN: {
                CompoundStatementParser compoundParser = new CompoundStatementParser(this);
                statementNode = compoundParser.parse(token);
                break;
            }
            case IDENTIFIER: {
                String name = token.getText().toLowerCase();
                SymbolTableEntry id = symbolTableStack.lookup(name);
                Definition idDefinition = id != null
                        ? id.getDefinition()
                        : UNDEFINED;

                // Assignment statement or procedure call
                switch ((DefinitionImpl) idDefinition) {
                    case VARIABLE:
                    case VALUE_PARM:
                    case VAR_PARM:
                    case UNDEFINED: {
                        AssignmentStatementParser assignmentParser = new AssignmentStatementParser(this);
                        statementNode = assignmentParser.parse(token);
                        break;
                    }
                    case FUNCTION: {
                        AssignmentStatementParser assignmentParser = new AssignmentStatementParser(this);
                        statementNode = assignmentParser.parseFunctionNameAssignment(token);
                        break;
                    }
                    case PROCEDURE: {
                        CallParser callParser = new CallParser(this);
                        statementNode = callParser.parse(token);
                        break;
                    }
                    default: {
                        errorHandler.flag(token, UNEXPECTED_TOKEN, this);
                        // consume identifier
                        token = nextToken();
                    }
                }
                break;
            }
            case REPEAT: {
                RepeatStatementParser repeatParser = new RepeatStatementParser(this);
                statementNode = repeatParser.parse(token);
                break;
            }
            case WHILE: {
                WhileStatementParser whileParser = new  WhileStatementParser(this);
                statementNode = whileParser.parse(token);
                break;
            }
            case FOR: {
                ForStatementParser forParser = new  ForStatementParser(this);
                statementNode = forParser.parse(token);
                break;
            }
            case IF: {
                IfStatementParser ifParser = new  IfStatementParser(this);
                statementNode = ifParser.parse(token);
                break;
            }
            case CASE: {
                CaseStatementParser caseParser = new  CaseStatementParser(this);
                statementNode = caseParser.parse(token);
                break;
            }
            default: {
                statementNode = ICodeFactory.createICodeNode(NO_OP);
                break;
            }
        }

        setLineNumber(statementNode, token);
        return statementNode;
    }

    protected void parseList(Token token, ICodeNode parentNode,
                             PascalTokenType terminator, PascalErrorCode errorCode)
            throws Exception {

        EnumSet<PascalTokenType> terminatorSet = STMT_START_SET.clone();
        terminatorSet.add(terminator);

         while (!(token instanceof EofToken)
                 && (token.getType() != terminator)) {

             ICodeNode statementNode = parse(token);
             parentNode.addChild(statementNode);

             token = currentToken();
             TokenType tokenType = token.getType();

             if(tokenType == SEMICOLON) {
                 token = nextToken();
             }
             else if(STMT_START_SET.contains(tokenType)) {
                 errorHandler.flag(token, MISSING_SEMICOLON, this);
             }

             token = synchronize(terminatorSet);
         }

         if(token.getType() == terminator) {
             token = nextToken();
         }else {
             errorHandler.flag(token, errorCode, this);
         }
    }

    protected void setLineNumber(ICodeNode node, Token token) {
        if (node != null) {
            node.setAttribute(LINE, token.getLineNumber());
        }
    }
}
