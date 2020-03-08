package app.core.events;

import app.core.WorkerNodeManager;

public class WorkerLoginEvent extends WorkerEvent {

    private final String username;
    private final String hashedPassword;

    public WorkerLoginEvent(WorkerNodeManager worker, String username, String hashedPassword) {
        super(worker);
        this.username       = username;
        this.hashedPassword = hashedPassword;
    }

    public WorkerLoginEvent(WorkerNodeManager worker) {
        super(worker);
        this.username       = null;
        this.hashedPassword = null;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

}

