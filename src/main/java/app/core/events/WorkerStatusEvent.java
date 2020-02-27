package app.core.events;

import app.core.WorkerNodeManager;

public class WorkerStatusEvent extends WorkerEvent {

    private final boolean active;

    public WorkerStatusEvent(WorkerNodeManager emitter, boolean active) {
        super(emitter);
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
