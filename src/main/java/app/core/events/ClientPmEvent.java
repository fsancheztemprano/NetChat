package app.core.events;

public class ClientPmEvent extends ClientEvent {

    private final boolean ack;
    private final String origin;
    private final String destiny;
    private final String message;

    public ClientPmEvent(boolean ack, String origin, String destiny, String message) {
        this.ack     = ack;
        this.origin  = origin;
        this.destiny = destiny;
        this.message = message;
    }

    public boolean isAck() {
        return ack;
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
