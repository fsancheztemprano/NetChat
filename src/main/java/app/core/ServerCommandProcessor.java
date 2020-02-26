package app.core;

import app.core.packetmodel.AppPacket;
import app.core.packetmodel.AuthRemovePacket;
import app.core.packetmodel.AuthRequestPacket;
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
                AuthRequestPacket authRequestPacket = (AuthRequestPacket) appPacket;
                serverSocketManager.getSocketEventBus().post((authRequestPacket));
                break;
            case AUTH_REMOVE:
                AuthRemovePacket authRemovePacket = (AuthRemovePacket) appPacket;
                serverSocketManager.getSocketEventBus().post((authRemovePacket));
                break;
        }
    }

}