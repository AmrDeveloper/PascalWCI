package frontend.pascal.parsers;

import frontend.Token;
import frontend.TokenType;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;
import intermediate.*;
import intermediate.icodeimpl.ICodeNodeTypeImpl;
import intermediate.symtabimpl.Predefined;
import intermediate.typeimpl.TypeChecker;
import intermediate.typeimpl.TypeFormImpl;

import java.util.EnumSet;

import static frontend.pascal.PascalErrorCode.*;
import static frontend.pascal.PascalTokenType.*;
import static intermediate.icodeimpl.ICodeKeyImpl.ID;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.SUBSCRIPTS;
import static intermediate.symtabimpl.DefinitionImpl.*;
import static intermediate.typeimpl.TypeKeyImpl.*;

public class VariableParser extends PascalParserTD {

    // Synchronization set to start a subscript or a field.
    private static final EnumSet<PascalTokenType> SUBSCRIPT_FIELD_START_SET = EnumSet.of(LEFT_BRACKET, DOT);

    // Synchronization set for the ] token.
    private static final EnumSet<PascalTokenType> RIGHT_BRACKET_SET = EnumSet.of(RIGHT_BRACKET, EQUALS, SEMICOLON);

    public VariableParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception {
        // Look up the identifier in the symbol table stack.
        String name = token.getText().toLowerCase();
        SymbolTableEntry variableId = symbolTableStack.lookup(name);

        // If not found, flag the error and enter the identifier
        // as an undefined identifier with an undefined type.
        if (variableId == null) {
            errorHandler.flag(token, IDENTIFIER_UNDEFINED, this);
            variableId = symbolTableStack.enterLocal(name);
            variableId.setDefinition(UNDEFINED);
            variableId.setTypeSpec(Predefined.undefinedType);
        }

        return parse(token, variableId);
    }

    public ICodeNode parse(Token token, SymbolTableEntry variableId) throws Exception {
        // Check how the variable is defined.
        Definition definitionCode = variableId.getDefinition();
        if ((definitionCode != VARIABLE)
                && (definitionCode != VALUE_PARM)
                && (definitionCode != VAR_PARM)) {
            errorHandler.flag(token, INVALID_IDENTIFIER_USAGE, this);
        }

        variableId.appendLineNumber(token.getLineNumber());

        ICodeNode variableNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.VARIABLE);
        variableNode.setAttribute(ID, variableId);

        // consume the identifier
        token = nextToken();

        // Parse array subscript or record fields
        TypeSpec variableType = variableId.getTypeSpec();
        while (SUBSCRIPT_FIELD_START_SET.contains(token.getType())) {
            ICodeNode subFldNode = token.getType() == LEFT_BRACKET
                    ? parseSubscripts(variableType)
                    : parseField(variableType);

            token = currentToken();

            // Update the variable&apos;s type.
            // The variable node adopts the SUBSCRIPTS or FIELD node
            variableType = subFldNode.getTypeSpec();
            variableNode.addChild(subFldNode);
        }

        variableNode.setTypeSpec(variableType);
        return variableNode;
    }

    private ICodeNode parseSubscripts(TypeSpec variableType) throws Exception {
        Token token;
        ExpressionParser expressionParser = new ExpressionParser(this);

        // Create a SUBSCRIPTS node.
        ICodeNode subscriptsNode = ICodeFactory.createICodeNode(SUBSCRIPTS);

        do {
            // consume the [ or , token
            token = nextToken();

            // The current variable is an array
            if (variableType.getForm() == TypeFormImpl.ARRAY) {
                // Parse the subscript expression
                ICodeNode exprNode = expressionParser.parse(token);
                TypeSpec exprToken = (exprNode != null)
                        ? exprNode.getTypeSpec()
                        : Predefined.undefinedType;

                // The subscript expression type must be assignment
                // compatible with the array index type
                TypeSpec indexType = (TypeSpec) variableType.getAttribute(ARRAY_INDEX_TYPE);
                if (!TypeChecker.areAssignmentCompatible(indexType, exprToken)) {
                    errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                }

                // The SUBSCRIPTS node adopts the subscript expression tree.
                subscriptsNode.addChild(exprNode);

                // Update the variable&apos;s type.
                variableType = (TypeSpec) variableType.getAttribute(ARRAY_ELEMENT_TYPE);
            }
            // Not an array type, so too many subscripts.
            else {
                errorHandler.flag(token, TOO_MANY_SUBSCRIPTS, this);
                expressionParser.parse(token);
            }
            token = currentToken();
        }
        while (token.getType() != COMMA);

        // Synchronize at the ] token.
        token = synchronize(RIGHT_BRACKET_SET);

        if(token.getType() == RIGHT_BRACKET) {
            // consume the ]
            token = nextToken();
        }
        else {
            errorHandler.flag(token, MISSING_RIGHT_BRACKET, this);
        }
        subscriptsNode.setTypeSpec(variableType);
        return subscriptsNode;
    }

    private ICodeNode parseField(TypeSpec variableType) throws Exception {
        // Create a FIELD node.
        ICodeNode fieldNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.FIELD);

        // consume the . token
        Token token = nextToken();
        TokenType tokenType = token.getType();
        TypeForm variableForm = variableType.getForm();

        if((tokenType == IDENTIFIER) && (variableForm == TypeFormImpl.RECORD)) {
            SymbolTable symbolTable = (SymbolTable) variableType.getAttribute(RECORD_SYMTAB);
            String fieldName = token.getText().toLowerCase();
            SymbolTableEntry fieldId = symbolTable.lookup(fieldName);

            if(fieldId != null) {
                variableType = fieldId.getTypeSpec();
                fieldId.appendLineNumber(token.getLineNumber());

                // set the field identifier's name
                fieldNode.setAttribute(ID, fieldId);
            }
            else{
                errorHandler.flag(token, INVALID_FIELD, this);
            }
        }
        else {
            errorHandler.flag(token, INVALID_FIELD, this);
        }

        // consume the field identifier
        token = nextToken();

        fieldNode.setTypeSpec(variableType);
        return fieldNode;
    }
}
