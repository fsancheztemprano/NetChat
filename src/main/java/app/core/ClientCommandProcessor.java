package app.core;

import app.core.events.ClientAuthResponseEvent;
import app.core.events.ClientUserListEvent;
import javax.annotation.Nonnull;

public class ClientCommandProcessor extends AbstractCommandProcessor {

    public ClientCommandProcessor(AbstractNodeManager socketManager) {
        super(socketManager);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected void processCommand(@Nonnull AppPacket appPacket) {
        switch (appPacket.getSignal()) {
            case UNAUTHORIZED_REQUEST:
                socketManager.log("Unauthorized Request");
            case AUTH_RESPONSE:
                socketManager.setSessionID(appPacket.getAuth());
                socketManager.getSocketEventBus().post(new ClientAuthResponseEvent(appPacket.getAuth()));
                break;
            case BROADCAST_USER_LIST:
                socketManager.getSocketEventBus().post(new ClientUserListEvent(appPacket.getList()));
        }
    }
}