package app.core.events;

public class ClientLoginResponseEvent extends ClientEvent {

    private final long auth;

    public ClientLoginResponseEvent(long auth) {
        this.auth = auth;
    }

    public long getAuth() {
        return auth;
    }
}
