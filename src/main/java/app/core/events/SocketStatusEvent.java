package app.core.events;

import app.core.ActivableSocketManager;

public class SocketStatusEvent extends SocketEvent {

    private final boolean active;

    public SocketStatusEvent(ActivableSocketManager socket, boolean active) {
        super(socket);
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
