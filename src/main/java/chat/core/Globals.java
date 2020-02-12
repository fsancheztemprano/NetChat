package chat.core;

public interface Globals {

    int HEARTBEAT_DELAY = 30;//in seconds
    int MAX_ACTIVE_CLIENTS = 3;

    int DEFAULT_SERVER_PORT = 5555;
    String DEFAULT_SERVER_HOSTNAME = "localhost";


    long WORKER_TIMEOUT = 120;
}
