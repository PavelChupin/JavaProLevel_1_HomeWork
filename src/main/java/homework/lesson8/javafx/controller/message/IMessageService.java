package homework.lesson8.javafx.controller.message;

import homework.lesson8.messageconvert.Message;

import java.io.Closeable;
import java.io.IOException;

public interface IMessageService extends Closeable {

    void sendMessage(Message message);

    //void processRetrievedMessage(String message);
    void processRetrievedMessage(Message message);

    @Override
    default void close() throws IOException {
        //Do nothing
    }
}
