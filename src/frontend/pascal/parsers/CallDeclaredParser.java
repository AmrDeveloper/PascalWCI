package frontend.pascal.parsers;

import frontend.Token;
import frontend.pascal.PascalParserTD;
import intermediate.ICodeFactory;
import intermediate.ICodeNode;
import intermediate.SymbolTableEntry;

import static intermediate.icodeimpl.ICodeKeyImpl.ID;
import static intermediate.icodeimpl.ICodeNodeTypeImpl.CALL;

public class CallDeclaredParser extends CallParser {

    public CallDeclaredParser(PascalParserTD parent) {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception {
        // Create the CALL node.
        ICodeNode callNode = ICodeFactory.createICodeNode(CALL);
        SymbolTableEntry routineId = symbolTableStack.lookup(token.getText().toLowerCase());
        callNode.setAttribute(ID, routineId);
        callNode.setTypeSpec(routineId.getTypeSpec());

        // consume procedure or function identifier
        token = nextToken();

        ICodeNode parmsNode = parseActualParameters(token, routineId, true, false, false);

        callNode.addChild(parmsNode);

        return callNode;
    }
}
