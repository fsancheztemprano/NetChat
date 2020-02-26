package app.core.events;

import app.core.ActivableSocketManager;

public class SocketEvent {

    private final ActivableSocketManager socket;

    public SocketEvent(ActivableSocketManager socket) {
        this.socket = socket;
    }

    public ActivableSocketManager getSocket() {
        return socket;
    }
}
