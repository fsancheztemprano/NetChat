package app.core.events;

import app.core.WorkerNodeManager;

public class WorkerRequestEvent extends WorkerEvent {

    private final RequestType requestType;

    public WorkerRequestEvent(WorkerNodeManager worker, RequestType requestType) {
        super(worker);
        this.requestType = requestType;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public enum RequestType {
        USER_LIST_REQUEST,
        GROUP_LIST_REQUEST
    }
}
