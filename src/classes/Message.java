package classes;

import jade.core.AID;

public class Message {
    public String message;
    public AID sender;

    public Message(String message, AID sender) {
        this.message = message;
        this.sender = sender;
    }
}
