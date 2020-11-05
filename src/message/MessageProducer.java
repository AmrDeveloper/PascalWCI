package message;

public interface MessageProducer {

    public void addMessageListener(MessageListener listener);

    public void removeMessageListener(MessageListener listener);

    public void sendMessage(Message message);
}
