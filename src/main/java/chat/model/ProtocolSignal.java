package chat.model;

import java.io.Serializable;

public enum ProtocolSignal implements Serializable {
    HEARTBEAT,
    CLIENT_JOIN,
    NEW_MESSAGE,
    CLIENT_QUIT,
    SERVER_SHUTDOWN
}