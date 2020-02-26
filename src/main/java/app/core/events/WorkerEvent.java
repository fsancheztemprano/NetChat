package app.core.events;

import app.core.WorkerNodeManager;

public abstract class WorkerEvent {

    private WorkerNodeManager emitter;

    public WorkerEvent(WorkerNodeManager emitter) {
        this.emitter = emitter;
    }

    public WorkerNodeManager getEmitter() {
        return emitter;
    }
}
