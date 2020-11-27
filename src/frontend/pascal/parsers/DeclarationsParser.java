package frontend.pascal.parsers;

import frontend.Token;
import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalTokenType;

import java.util.EnumSet;

import static frontend.pascal.PascalTokenType.*;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.VARIABLE;

public class DeclarationsParser extends PascalParserTD {

    static final EnumSet<PascalTokenType> DECLARATION_START_SET = EnumSet.of(CONST, TYPE, VAR, PROCEDURE, FUNCTION, BEGIN);

    static final EnumSet<PascalTokenType> TYPE_START_SET = DECLARATION_START_SET.clone();

    static {
        TYPE_START_SET.remove(CONST);
    }

    static final EnumSet<PascalTokenType> VAR_START_SET = TYPE_START_SET.clone();

    static {
        VAR_START_SET.remove(TYPE);
    }

    static final EnumSet<PascalTokenType> ROUTINE_START_SET = VAR_START_SET.clone();

    static {
        ROUTINE_START_SET.remove(VAR);
    }

    public DeclarationsParser(PascalParserTD parent) {
        super(parent);
    }

    public void parse(Token token) throws Exception {
        token = synchronize(DECLARATION_START_SET);

        if(token.getType() == CONST) {
            // consume CONST keyword
            token = nextToken();
            ConstantDefinitionsParser constantDefinitionsParser = new ConstantDefinitionsParser(this);
            constantDefinitionsParser.parse(token);
        }

        token = synchronize(TYPE_START_SET);

        if(token.getType() == TYPE) {
            // consume TYPE
            token = nextToken();
            TypeDefinitionsParser typeDefinitionsParser = new TypeDefinitionsParser(this);
            typeDefinitionsParser.parse(token);
        }

        token = synchronize(VAR_START_SET);

        if(token.getType() == VAR) {
            // consume VAR
            token = nextToken();

            VariableDeclarationsParser variableDeclarationsParser = new VariableDeclarationsParser(this);
            variableDeclarationsParser.setDefinition(VARIABLE);
            variableDeclarationsParser.parse(token);
        }

        token = synchronize(ROUTINE_START_SET);
    }

}
