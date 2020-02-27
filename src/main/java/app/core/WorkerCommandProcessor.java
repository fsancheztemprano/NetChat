package app.core;

import app.core.AppPacket.ProtocolSignal;

public class WorkerCommandProcessor extends AbstractCommandProcessor {

    private final WorkerNodeManager workerNodeManager;
    private final long managerID;

    public WorkerCommandProcessor(WorkerNodeManager manager) {
        this.workerNodeManager = manager;
        this.managerID         = manager.getSessionID();
    }

    @Override
    protected void processCommand(AppPacket appPacket) {
        if (appPacket.getSignal() == ProtocolSignal.AUTH_REQUEST || appPacket.getSignal() == ProtocolSignal.HEARTBEAT || appPacket.getAuth() == managerID)
            workerNodeManager.getServerCommandProcessor().queueCommandProcess(appPacket);
        else
            workerNodeManager.queueTransmission(new AppPacket(ProtocolSignal.UNAUTHORIZED_REQUEST));
    }
}
