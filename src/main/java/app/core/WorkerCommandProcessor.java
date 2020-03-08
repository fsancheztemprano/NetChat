package app.core;

import app.core.AppPacket.ProtocolSignal;
import app.core.events.WorkerGroupListEvent;
import app.core.events.WorkerJoinGroupEvent;
import app.core.events.WorkerLoginEvent;
import app.core.events.WorkerNewGroupEvent;
import app.core.events.WorkerPrivateMessageEvent;
import app.core.events.WorkerQuitGroupEvent;
import app.core.events.WorkerUserListEvent;

public class WorkerCommandProcessor extends AbstractCommandProcessor {

    private final long managerID;

    public WorkerCommandProcessor(WorkerNodeManager manager) {
        super(manager, manager.getServerEventBus());
        this.managerID = manager.getSessionID();
    }

    @Override
    protected void processCommand(AppPacket appPacket) {
        if (appPacket.getSignal() == ProtocolSignal.CLIENT_REQUEST_LOGIN || appPacket.getSignal() == ProtocolSignal.HEARTBEAT || appPacket.getAuth() == managerID) {
            switch (appPacket.getSignal()) {
                case CLIENT_REQUEST_LOGIN:
                    eventBus.post(new WorkerLoginEvent((WorkerNodeManager) appPacket.getHandler(), appPacket.getUsername(), appPacket.getPassword()));
                    break;
                case CLIENT_REQUEST_LOGOUT:
                    eventBus.post(new WorkerLoginEvent((WorkerNodeManager) appPacket.getHandler()));
                    break;
                case CLIENT_REQUEST_USER_LIST:
                    eventBus.post(new WorkerUserListEvent((WorkerNodeManager) appPacket.getHandler()));
                    break;
                case CLIENT_REQUEST_GROUP_LIST:
                    eventBus.post(new WorkerGroupListEvent((WorkerNodeManager) appPacket.getHandler()));
                    break;
                case CLIENT_SEND_PM:
                    eventBus.post(new WorkerPrivateMessageEvent((WorkerNodeManager) appPacket.getHandler(), appPacket.getDestiny(), appPacket.getMessage()));
                    break;
                case CLIENT_REQUEST_NEW_GROUP:
                    eventBus.post(new WorkerNewGroupEvent((WorkerNodeManager) appPacket.getHandler(), appPacket.getMessage()));
                    break;
                case CLIENT_REQUEST_GROUP_JOIN:
                    eventBus.post(new WorkerJoinGroupEvent((WorkerNodeManager) appPacket.getHandler(), appPacket.getDestiny()));
                    break;
                case CLIENT_REQUEST_GROUP_QUIT:
                    eventBus.post(new WorkerQuitGroupEvent((WorkerNodeManager) appPacket.getHandler(), appPacket.getDestiny()));
                    break;
                default:
                    socketManager.queueTransmission(AppPacket.ofType(ProtocolSignal.SERVER_RESPONSE_UNRECOGNIZED_REQUEST));
                    break;
            }
        } else
            socketManager.queueTransmission(AppPacket.ofType(ProtocolSignal.SERVER_RESPONSE_AUTHORIZED_REQUEST));
    }
}
