package app.core.events;

import app.core.WorkerNodeManager;

public class WorkerSessionStartEvent extends WorkerEvent {

    public WorkerSessionStartEvent(WorkerNodeManager emitter) {
        super(emitter);
    }
}
