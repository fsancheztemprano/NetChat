package app.core.events;

public class ClientGroupMessageEvent extends ClientEvent {

    private final String author;
    private final String destinyGroup;
    private final String message;

    public ClientGroupMessageEvent(String author, String destinyGroup, String message) {
        this.author       = author;
        this.destinyGroup = destinyGroup;
        this.message      = message;
    }

    public String getAuthor() {
        return author;
    }

    public String getDestinyGroup() {
        return destinyGroup;
    }

    public String getMessage() {
        return message;
    }
}
