package app.core.events;

public class ClientPrivateMessageEvent extends ClientEvent {

    private final String origin;
    private final String destiny;
    private final String message;

    public ClientPrivateMessageEvent(String origin, String destiny, String message) {
        this.origin  = origin;
        this.destiny = destiny;
        this.message = message;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestiny() {
        return destiny;
    }

    public String getMessage() {
        return message;
    }
}
