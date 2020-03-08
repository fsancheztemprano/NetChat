package app.core.events;

import app.core.ServerSocketManager;

public class ServerActiveClientsEvent extends ServerEvent {

    private final int activeClients;

    public ServerActiveClientsEvent(ServerSocketManager serverSocketManager, int activeClients) {
        super(serverSocketManager);
        this.activeClients = activeClients;
    }

    public int getActiveClients() {
        return activeClients;
    }
}
