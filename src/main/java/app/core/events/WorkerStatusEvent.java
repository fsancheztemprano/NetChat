package app.core.events;


import app.core.WorkerNodeManager;

public class WorkerStatusEvent extends WorkerEvent {

    private final boolean active;

    public WorkerStatusEvent(WorkerNodeManager worker, boolean active) {
        super(worker);
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
