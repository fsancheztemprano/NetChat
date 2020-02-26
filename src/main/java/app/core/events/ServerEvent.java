package app.core.events;

import app.core.ServerSocketManager;

public class ServerEvent {

    private final ServerSocketManager serverSocketManager;

    public ServerEvent(ServerSocketManager serverSocketManager) {
        this.serverSocketManager = serverSocketManager;
    }

    public ServerSocketManager getServerSocketManager() {
        return serverSocketManager;
    }
}
