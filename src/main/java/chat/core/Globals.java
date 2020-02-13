package chat.core;

public interface Globals {

    int MAX_ACTIVE_CLIENTS = 3;

    int DEFAULT_SERVER_PORT = 5555;
    String DEFAULT_SERVER_HOSTNAME = "localhost";


    long HEARTBEAT_TIMEOUT = 30;

    int CLIENT_CONNECT_TIMEOUT = 5000;//ms

    long HEARTBEAT_INTERVAL = 500;//MS
    int HEARTBEAT_DELAY = 3;//in seconds
}
