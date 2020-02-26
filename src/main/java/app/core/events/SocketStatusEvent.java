package app.core.events;

public class SocketStatusEvent {

    private final boolean active;

    public SocketStatusEvent(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
