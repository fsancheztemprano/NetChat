package chat.core;

public interface Globals {

    int MAX_ACTIVE_CLIENTS = 10;

    int DEFAULT_SERVER_PORT = 5555;
    String DEFAULT_SERVER_HOSTNAME = "localhost";


    long HEARTBEAT_TIMEOUT = 60;

    int CLIENT_CONNECT_TIMEOUT = 5000;//ms

    long HEARTBEAT_INTERVAL = 3000;//MS
    int HEARTBEAT_DELAY = 30;//in seconds

    long TRANSMITTER_THREAD_TIMEOUT = 1;//seconds
    long PROCESSORS_THREAD_TIMEOUT = 1;//seconds
    int HEARTBEAT_FIRST = 2;//seconds
}
