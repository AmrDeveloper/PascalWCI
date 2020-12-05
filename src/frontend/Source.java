package frontend;

import message.*;

import java.io.BufferedReader;
import java.io.IOException;

public class Source implements MessageProducer {

    public static final char EOL = '\n';
    public static final char EOF = (char) 0;

    private BufferedReader reader;
    private String line;
    private int lineNum;
    private int currentPosition;
    protected static MessageHandler messageHandler;

    static {
        messageHandler = new MessageHandler();
    }

    public Source(BufferedReader reader) throws IOException {
        this.lineNum = 0;
        this.currentPosition = -2;
        this.reader = reader;
    }

    public char currentChar() throws Exception {
        if(currentPosition == -2) {
            readLine();
            return nextChar();
        }
        else if(line == null) {
            return EOF;
        }
        else if((currentPosition == -1) || currentPosition == line.length()) {
            return EOL;
        }
        else if(currentPosition > line.length()) {
            readLine();
            return nextChar();
        }
        else{
            return line.charAt(currentPosition);
        }
    }

    public char nextChar() throws Exception {
        ++currentPosition;
        return currentChar();
    }

    public char peekChar() throws Exception {
        currentChar();
        if(line == null) return EOF;
        int nextPosition = currentPosition + 1;
        return (nextPosition < line.length()) ? line.charAt(nextPosition) : EOL;
    }

    private void readLine() throws Exception {
        line = reader.readLine();
        currentPosition = -1;

        if(line != null) {
            ++lineNum;
            sendMessage(new Message(MessageType.SOURCE_LINE, new Object[] {lineNum, line}));
        }
    }

    public void close() throws Exception {
        if(reader != null) {
            try{
                reader.close();
            }catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    public int getLineNum() {
        return lineNum;
    }

    public int getCurrentPosition() {
        return currentPosition;
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
}
