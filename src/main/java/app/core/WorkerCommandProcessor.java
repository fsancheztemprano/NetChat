package app.core;

import app.core.AppPacket.ProtocolSignal;
import app.core.events.WorkerGroupListEvent;
import app.core.events.WorkerLoginEvent;
import app.core.events.WorkerPrivateMessageEvent;
import app.core.events.WorkerUserListEvent;
import com.google.common.eventbus.EventBus;

public class WorkerCommandProcessor extends AbstractCommandProcessor {

    private final long managerID;
    private final EventBus serverEventBus;

    public WorkerCommandProcessor(WorkerNodeManager manager) {
        super(manager);
        this.managerID      = manager.getSessionID();
        this.serverEventBus = manager.getServerEventBus();
    }

    @Override
    protected void processCommand(AppPacket appPacket) {
        if (appPacket.getSignal() == ProtocolSignal.CLIENT_LOGIN_REQUEST || appPacket.getSignal() == ProtocolSignal.HEARTBEAT || appPacket.getAuth() == managerID) {
            switch (appPacket.getSignal()) {
                case CLIENT_LOGIN_REQUEST:
                    serverEventBus.post(new WorkerLoginEvent((WorkerNodeManager) appPacket.getHandler(), appPacket.getUsername(), appPacket.getPassword()));
                    break;
                case CLIENT_LOGOUT_REQUEST:
                    serverEventBus.post(new WorkerLoginEvent((WorkerNodeManager) appPacket.getHandler()));
                    break;
                case CLIENT_REQUEST_USER_LIST:
                    serverEventBus.post(new WorkerUserListEvent((WorkerNodeManager) appPacket.getHandler()));
                    break;
                case CLIENT_REQUEST_GROUP_LIST:
                    serverEventBus.post(new WorkerGroupListEvent((WorkerNodeManager) appPacket.getHandler()));
                    break;
                case CLIENT_PM:
                    serverEventBus.post(new WorkerPrivateMessageEvent((WorkerNodeManager) appPacket.getHandler(), appPacket.getDestiny(), appPacket.getMessage()));
                    break;
                case CLIENT_REQUEST_NEW_GROUP:
//                    serverEventBus.post(new );
                    break;
                default:
                    socketManager.queueTransmission(AppPacket.ofType(ProtocolSignal.UNAUTHORIZED_REQUEST));
                    break;
            }
        }
    }
}
