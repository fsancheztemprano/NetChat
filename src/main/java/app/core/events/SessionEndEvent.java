package app.core.events;

import app.core.WorkerNodeManager;

public class SessionEndEvent extends WorkerEvent {

    public SessionEndEvent(WorkerNodeManager emitter) {
        super(emitter);
    }
}
