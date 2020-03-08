package app.core.events;

import app.core.WorkerNodeManager;

public class WorkerQuitGroupEvent extends WorkerEvent {

    private final String groupName;

    public WorkerQuitGroupEvent(WorkerNodeManager worker, String groupName) {
        super(worker);
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }
}
