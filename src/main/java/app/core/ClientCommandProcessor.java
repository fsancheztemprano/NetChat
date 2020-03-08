package app.core;

import app.core.events.ClientAlertEvent;
import app.core.events.ClientGroupListEvent;
import app.core.events.ClientGroupMessageEvent;
import app.core.events.ClientGroupUserListEvent;
import app.core.events.ClientLoginSuccessEvent;
import app.core.events.ClientPrivateMessageEvent;
import app.core.events.ClientUserListEvent;
import javax.annotation.Nonnull;

public class ClientCommandProcessor extends AbstractCommandProcessor {

    public ClientCommandProcessor(AbstractNodeManager socketManager) {
        super(socketManager, socketManager.getSocketEventBus());
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected void processCommand(@Nonnull AppPacket appPacket) {
        switch (appPacket.getSignal()) {
            case SERVER_RESPONSE_UNRECOGNIZED_REQUEST:
                socketManager.log("Unauthorized Request");
                break;
            case SERVER_RESPONSE_LOGIN_SUCCESS:
                if (appPacket.getAuth() != -1) {
                    socketManager.setSessionID(appPacket.getAuth());
                    eventBus.post(new ClientLoginSuccessEvent());
                } else
                    eventBus.post(new ClientAlertEvent("Login Failed"));
                break;
            case SERVER_RESPONSE_ALERT_MESSAGE:
                eventBus.post(new ClientAlertEvent(appPacket.getMessage()));
                break;
            case SERVER_SEND_USER_LIST:
                eventBus.post(new ClientUserListEvent(appPacket.getList()));
                break;
            case SERVER_PIPE_PRIVATE_MESSAGE:
                eventBus.post(new ClientPrivateMessageEvent(appPacket.getUsername(), appPacket.getDestiny(), appPacket.getMessage()));
                break;
            case SERVER_SEND_GROUP_LIST:
                eventBus.post(new ClientGroupListEvent(appPacket.getList()));
                break;
            case SERVER_SEND_GROUP_USER_LIST:
                eventBus.post(new ClientGroupUserListEvent(appPacket.getDestiny(), appPacket.getList()));
                break;
            case SERVER_PIPE_GROUP_MESSAGE:
                eventBus.post(new ClientGroupMessageEvent(appPacket.getUsername(), appPacket.getDestiny(), appPacket.getMessage()));
                break;
            default:
                eventBus.post(appPacket.getSignal().toString());
                break;
        }
    }
}