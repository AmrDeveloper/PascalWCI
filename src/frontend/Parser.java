package frontend;

import intermediate.ICode;
import intermediate.SymbolTable;
import message.Message;
import message.MessageHandler;
import message.MessageListener;
import message.MessageProducer;

public abstract class Parser implements MessageProducer {

    protected static SymbolTable symTab;
    protected static MessageHandler messageHandler;

    protected Scanner scanner;
    protected ICode iCode;

    static {
        symTab = null;
        messageHandler = new MessageHandler();
    }

    protected Parser(Scanner scanner)    {
        this.scanner = scanner;
    }

    public abstract void parse()  throws Exception;

    public abstract int getErrorCount();

    public Token currentToken() {
        return scanner.currentToken();
    }

    public Token nextToken() throws Exception {
        return scanner.nextToken();
    }

    public void addMessageListener(MessageListener listener) {
        messageHandler.addListener(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        messageHandler.removeListener(listener);
    }

    public void sendMessage(Message message) {
        messageHandler.sendMessage(message);
    }

    public ICode getICode() {
        return iCode;
    }

    public SymbolTable getSymbolTable() {
        return symTab;
    }
}
