package app.core.events;

import app.core.WorkerNodeManager;

public class WorkerGroupListEvent extends WorkerEvent {

    public WorkerGroupListEvent(WorkerNodeManager worker) {
        super(worker);
    }
}
