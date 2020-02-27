package app.core.events;

import app.core.WorkerNodeManager;

public class WorkerAuthEvent extends WorkerEvent {

    private final AuthType authType;
    private final String username;
    private final String hashedPassword;

    public WorkerAuthEvent(WorkerNodeManager worker, String username, String hashedPassword) {
        super(worker);
        this.authType       = AuthType.REQUEST;
        this.username       = username;
        this.hashedPassword = hashedPassword;
    }

    public WorkerAuthEvent(WorkerNodeManager worker) {
        super(worker);
        this.authType       = AuthType.REMOVE;
        this.username       = null;
        this.hashedPassword = null;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public enum AuthType {
        REQUEST,
        REMOVE
    }
}

