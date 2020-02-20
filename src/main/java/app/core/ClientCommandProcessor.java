package app.core;

import app.core.packetmodel.AppPacket;
import app.core.packetmodel.AuthResponsePacket;
import javax.annotation.Nonnull;

public class ClientCommandProcessor extends AbstractCommandProcessor {

    ClientSocketManager clientManager;

    public ClientCommandProcessor(ClientSocketManager clientManager) {
        super(clientManager, clientManager.getInboundCommandQueue());
        this.clientManager = clientManager;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected void processCommand(@Nonnull AppPacket appPacket) {
        switch (appPacket.getSignal()) {
            case AUTH_RESPONSE:
                clientManager.setSessionID(appPacket.getAuth());
                AuthResponsePacket authResponsePacket = (AuthResponsePacket) appPacket;
                clientManager.getSocketEventBus().post(authResponsePacket);
                break;
        }
    }
}