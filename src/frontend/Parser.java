package frontend;

import intermediate.ICode;
import intermediate.SymbolTable;
import intermediate.SymbolTableFactory;
import intermediate.SymbolTableStack;
import message.Message;
import message.MessageHandler;
import message.MessageListener;
import message.MessageProducer;

public abstract class Parser implements MessageProducer {

    //protected static SymbolTable symbolTable;
    protected static SymbolTableStack symbolTableStack;
    protected static MessageHandler messageHandler;

    protected Scanner scanner;
    protected ICode iCode;

    static {
        symbolTableStack = SymbolTableFactory.createSymbolTableStack();
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

    public Scanner getScanner() {
        return scanner;
    }

    public SymbolTableStack getSymbolTableStack() {
        return symbolTableStack;
    }
}
