package app.core;

import app.core.events.WorkerAuthEvent;
import javax.annotation.Nonnull;

public class ServerCommandProcessor extends AbstractCommandProcessor {

    private ServerSocketManager serverSocketManager;

    public ServerCommandProcessor(ServerSocketManager serverSocketManager) {
        this.serverSocketManager = serverSocketManager;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected void processCommand(@Nonnull AppPacket appPacket) {
        switch (appPacket.getSignal()) {
            case AUTH_REQUEST:
                serverSocketManager.getSocketEventBus().post(new WorkerAuthEvent((WorkerNodeManager) appPacket.getHandler(), appPacket.getUsername(), appPacket.getPassword()));
                break;
            case AUTH_REMOVE:
                serverSocketManager.getSocketEventBus().post(new WorkerAuthEvent((WorkerNodeManager) appPacket.getHandler()));
                break;
        }
    }

}