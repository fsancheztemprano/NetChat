package app.core;

import java.io.Serializable;
import java.net.SocketAddress;
import java.util.Objects;

public class AppPacket implements Serializable {

    private ProtocolSignal signal;
    private SocketAddress originSocketAddress;
    private String username;
    private String message;

    public AppPacket(ProtocolSignal signal, SocketAddress originSocketAddress, String username, String message) {
        this.signal              = signal;
        this.originSocketAddress = originSocketAddress;
        this.username            = username;
        this.message             = message;
    }

    public ProtocolSignal getSignal() {
        return signal;
    }

    public void setSignal(ProtocolSignal signal) {
        this.signal = signal;
    }

    public SocketAddress getOriginSocketAddress() {
        return originSocketAddress;
    }

    public void setOriginSocketAddress(SocketAddress originSocketAddress) {
        this.originSocketAddress = originSocketAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AppPacket that = (AppPacket) o;
        return signal == that.signal &&
               Objects.equals(originSocketAddress, that.originSocketAddress) &&
               Objects.equals(username, that.username) &&
               Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signal, originSocketAddress, username, message);
    }

    @Override
    public String toString() {
        return "ClientMessage{" +
               "signal=" + signal +
               ", origin=" + originSocketAddress +
               ", username='" + username + '\'' +
               ", message='" + message + '\'' +
               '}';
    }

    public enum ProtocolSignal implements Serializable {
        HEARTBEAT,
        CLIENT_JOIN,
        NEW_MESSAGE,
        CLIENT_QUIT,
        SERVER_SHUTDOWN,
        AUTH_REQUEST,
        AUTH_RESPONSE
    }
}
