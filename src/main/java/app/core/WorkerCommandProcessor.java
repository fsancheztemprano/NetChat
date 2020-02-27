package app.core;

import app.core.AppPacket.ProtocolSignal;
import app.core.events.WorkerAuthEvent;
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
        if (appPacket.getSignal() == ProtocolSignal.AUTH_REQUEST || appPacket.getSignal() == ProtocolSignal.HEARTBEAT || appPacket.getAuth() == managerID) {
            switch (appPacket.getSignal()) {
                case AUTH_REQUEST:
                    WorkerAuthEvent event = new WorkerAuthEvent((WorkerNodeManager) appPacket.getHandler(), appPacket.getUsername(), appPacket.getPassword());
                    serverEventBus.post(event);
                    break;
                case AUTH_REMOVE:
                    serverEventBus.post(new WorkerAuthEvent((WorkerNodeManager) appPacket.getHandler()));
                    break;
                case CLIENT_REQUEST_USER_LIST:
                    serverEventBus.post(new WorkerRequestEvent((WorkerNodeManager) appPacket.getHandler(), RequestType.USER_LIST_REQUEST));
                    break;
                case CLIENT_REQUEST_GROUP_LIST:
                    serverEventBus.post(new WorkerRequestEvent((WorkerNodeManager) appPacket.getHandler(), RequestType.GROUP_LIST_REQUEST));
                    break;
                default:
                    socketManager.queueTransmission(new AppPacket(ProtocolSignal.UNAUTHORIZED_REQUEST));
                    break;
            }
        }
    }
}
