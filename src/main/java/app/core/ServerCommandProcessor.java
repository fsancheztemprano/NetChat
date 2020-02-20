package app.core;

import app.core.packetmodel.AppPacket;
import app.core.packetmodel.AppPacket.ProtocolSignal;
import app.core.packetmodel.AuthRemovePacket;
import app.core.packetmodel.AuthRequestPacket;
import javax.annotation.Nonnull;

public class ServerCommandProcessor extends AbstractCommandProcessor {

    private ServerSocketManager serverSocketManager;

    public ServerCommandProcessor(ServerSocketManager serverSocketManager) {
        super(serverSocketManager, serverSocketManager.getServerCommandQueue());
        this.serverSocketManager = serverSocketManager;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected void processCommand(@Nonnull AppPacket appPacket) {
        if (appPacket.getSignal() != ProtocolSignal.AUTH_REQUEST && appPacket.getSignal() != ProtocolSignal.HEARTBEAT && (appPacket.getAuth() == -1)) { //TODO change -1 => if auth in list
            appPacket.getHandler().queueTransmission(new AppPacket(ProtocolSignal.UNAUTHORIZED_REQUEST));
            return;
        }
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