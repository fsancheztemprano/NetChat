package app.core;

import app.core.events.ClientLoginResponseEvent;
import app.core.events.ClientPmEvent;
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
                break;
            case AUTH_RESPONSE:
                socketManager.setSessionID(appPacket.getAuth());
                socketManager.getSocketEventBus().post(new ClientLoginResponseEvent(appPacket.getAuth()));
                break;
            case SERVER_SEND_USER_LIST:
                socketManager.getSocketEventBus().post(new ClientUserListEvent(appPacket.getList()));
                break;
            case CLIENT_PM:
                socketManager.getSocketEventBus().post(new ClientPmEvent(false, appPacket.getUsername(), appPacket.getDestiny(), appPacket.getMessage()));
                break;
            case CLIENT_PM_ACK:
                socketManager.getSocketEventBus().post(new ClientPmEvent(true, appPacket.getUsername(), appPacket.getDestiny(), appPacket.getMessage()));
                break;
            default:
                break;
        }
    }
}