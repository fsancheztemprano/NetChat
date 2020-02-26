package app.core;

import app.core.packetmodel.AppPacket;
import app.core.packetmodel.AuthResponsePacket;
import javax.annotation.Nonnull;

public class ClientCommandProcessor extends AbstractCommandProcessor {

    private final ClientNodeManager clientManager;

    public ClientCommandProcessor(ClientNodeManager clientManager) {
        this.clientManager = clientManager;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected void processCommand(@Nonnull AppPacket appPacket) {
        switch (appPacket.getSignal()) {
            case UNAUTHORIZED_REQUEST:
                clientManager.log("Unauthorized Request Sent");
            case AUTH_RESPONSE:
                clientManager.setSessionID(appPacket.getAuth());
                AuthResponsePacket authResponsePacket = (AuthResponsePacket) appPacket;
                clientManager.getSocketEventBus().post(authResponsePacket);
                break;
        }
    }
}