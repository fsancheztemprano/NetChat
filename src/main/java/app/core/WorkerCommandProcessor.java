package app.core;

import app.core.packetmodel.AppPacket;
import app.core.packetmodel.AppPacket.ProtocolSignal;

public class WorkerCommandProcessor extends AbstractCommandProcessor {

    private final WorkerNodeManager workerNodeManager;
    private final long managerID;

    public WorkerCommandProcessor(WorkerNodeManager manager) {
        super(manager);
        this.workerNodeManager = manager;
        this.managerID         = manager.getSessionID();
    }

    @Override
    protected void processCommand(AppPacket appPacket) {
        if (appPacket.getSignal() == ProtocolSignal.AUTH_REQUEST || appPacket.getSignal() == ProtocolSignal.HEARTBEAT || appPacket.getAuth() == managerID)
            workerNodeManager.getServerSocketManager().getCommandProcessor().processCommand(appPacket);
    }
}
