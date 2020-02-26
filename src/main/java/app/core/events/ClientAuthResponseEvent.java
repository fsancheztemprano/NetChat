package app.core.events;

public class ClientAuthResponseEvent extends ClientEvent {

    private final long auth;

    public ClientAuthResponseEvent(long auth) {
        this.auth = auth;
    }

    public long getAuth() {
        return auth;
    }
}
