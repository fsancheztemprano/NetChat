package app.core.events;

import app.core.WorkerNodeManager;

public class WorkerUserListEvent extends WorkerEvent {

    public WorkerUserListEvent(WorkerNodeManager worker) {
        super(worker);
    }
}
