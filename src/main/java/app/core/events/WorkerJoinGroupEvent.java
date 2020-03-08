package app.core.events;

import app.core.WorkerNodeManager;

public class WorkerJoinGroupEvent extends WorkerEvent {

    private final String groupName;

    public WorkerJoinGroupEvent(WorkerNodeManager worker, String groupName) {
        super(worker);
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }
}
