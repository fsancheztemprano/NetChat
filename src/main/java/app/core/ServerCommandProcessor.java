package app.core;

import app.core.packetmodel.AppPacket;
import app.core.packetmodel.AuthRemoveEvent;
import app.core.packetmodel.AuthRequestEvent;
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
                AuthRequestEvent authRequestEvent = (AuthRequestEvent) appPacket;
                serverSocketManager.getSocketEventBus().post((authRequestEvent));
                break;
            case AUTH_REMOVE:
                AuthRemoveEvent authRemoveEvent = (AuthRemoveEvent) appPacket;
                serverSocketManager.getSocketEventBus().post((authRemoveEvent));
                break;
        }
    }

}