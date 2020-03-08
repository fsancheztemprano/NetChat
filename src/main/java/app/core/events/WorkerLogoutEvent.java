package app.core.events;

import app.core.WorkerNodeManager;

public class WorkerLogoutEvent extends WorkerEvent {

    public WorkerLogoutEvent(WorkerNodeManager worker) {
        super(worker);
    }
}
