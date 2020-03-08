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
            case SERVER_RESPONSE_UNAUTHORIZED_REQUEST:
                socketManager.log("Unauthorized Request");
                break;
            case SERVER_RESPONSE_AUTH:
                socketManager.setSessionID(appPacket.getAuth());
                socketManager.getSocketEventBus().post(new ClientLoginResponseEvent(appPacket.getAuth()));
                break;
            case SERVER_SEND_USER_LIST:
                socketManager.getSocketEventBus().post(new ClientUserListEvent(appPacket.getList()));
                break;
            case CLIENT_SEND_PM:
                socketManager.getSocketEventBus().post(new ClientPmEvent(false, appPacket.getUsername(), appPacket.getDestiny(), appPacket.getMessage()));
                break;
            case CLIENT_SENT_PM_ACK:
                socketManager.getSocketEventBus().post(new ClientPmEvent(true, appPacket.getUsername(), appPacket.getDestiny(), appPacket.getMessage()));
                break;
            default:
                break;
        }
    }
}