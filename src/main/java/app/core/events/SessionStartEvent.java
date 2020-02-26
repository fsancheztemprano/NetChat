package app.core.events;

import app.core.WorkerNodeManager;

public class SessionStartEvent extends WorkerEvent {

    public SessionStartEvent(WorkerNodeManager emitter) {
        super(emitter);
    }
}
