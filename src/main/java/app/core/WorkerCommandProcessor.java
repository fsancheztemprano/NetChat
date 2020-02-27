package app.core;

import app.core.AppPacket.ProtocolSignal;
import app.core.events.WorkerLoginEvent;
import app.core.events.WorkerPrivateMessageEvent;
import app.core.events.WorkerRequestEvent;
import app.core.events.WorkerRequestEvent.RequestType;
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
                    serverEventBus.post(new WorkerRequestEvent((WorkerNodeManager) appPacket.getHandler(), RequestType.USER_LIST_REQUEST));
                    break;
                case CLIENT_REQUEST_GROUP_LIST:
                    serverEventBus.post(new WorkerRequestEvent((WorkerNodeManager) appPacket.getHandler(), RequestType.GROUP_LIST_REQUEST));
                    break;
                case CLIENT_PM:
                    serverEventBus.post(new WorkerPrivateMessageEvent((WorkerNodeManager) appPacket.getHandler(), appPacket.getDestiny(), appPacket.getMessage()));
                    break;
                default:
                    socketManager.queueTransmission(new AppPacket(ProtocolSignal.UNAUTHORIZED_REQUEST));
                    break;
            }
        }
    }
}
