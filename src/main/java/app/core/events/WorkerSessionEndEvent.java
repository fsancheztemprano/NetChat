package app.core.events;

import app.core.WorkerNodeManager;

public class WorkerSessionEndEvent extends WorkerEvent {

    public WorkerSessionEndEvent(WorkerNodeManager emitter) {
        super(emitter);
    }
}
