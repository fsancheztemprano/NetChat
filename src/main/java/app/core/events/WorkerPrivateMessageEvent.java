package app.core.events;

import app.core.WorkerNodeManager;

public class WorkerPrivateMessageEvent extends WorkerEvent {

    private final String destiny;
    private final String message;

    public WorkerPrivateMessageEvent(WorkerNodeManager worker, String destiny, String message) {
        super(worker);
        this.destiny = destiny;
        this.message = message;
    }

    public String getDestiny() {
        return destiny;
    }

    public String getMessage() {
        return message;
    }

}
