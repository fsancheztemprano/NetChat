package app.core.events;

import app.core.WorkerNodeManager;

public class WorkerNewGroupEvent extends WorkerEvent {

    private final String newGroupName;

    public WorkerNewGroupEvent(WorkerNodeManager worker, String newGroupName) {
        super(worker);
        this.newGroupName = newGroupName;
    }

    public String getNewGroupName() {
        return newGroupName;
    }
}
