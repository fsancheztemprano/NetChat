package app.core.events;

import app.core.WorkerNodeManager;

public abstract class WorkerEvent {

    protected final WorkerNodeManager worker;
    protected final long sessionID;

    public WorkerEvent(WorkerNodeManager worker) {
        this.worker    = worker;
        this.sessionID = worker.getSessionID();
    }

    public WorkerNodeManager getWorker() {
        return worker;
    }

    public long getSessionID() {
        return sessionID;
    }
}
