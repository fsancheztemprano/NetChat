package app.core.packetmodel;

import app.core.WorkerSocketManager;
import java.io.Serializable;
import java.net.SocketAddress;
import java.util.Objects;

public class AppPacket implements Serializable {

    private ProtocolSignal signal;
    private SocketAddress originSocketAddress;
    private long auth;
    private String username;
    private String password;

    private String destiny;
    private String message;

    private transient WorkerSocketManager handler;

    public AppPacket(ProtocolSignal signal) {
        this.signal = signal;
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

    public long getAuth() {
        return auth;
    }

    public void setAuth(long auth) {
        this.auth = auth;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDestiny() {
        return destiny;
    }

    public void setDestiny(String destiny) {
        this.destiny = destiny;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public WorkerSocketManager getHandler() {
        return handler;
    }

    public void setHandler(WorkerSocketManager handler) {
        this.handler = handler;
    }

    @Override
    public String toString() {
        return "AppPacket{" +
               "signal=" + signal +
               ", originSocketAddress=" + originSocketAddress +
               ", auth='" + auth + '\'' +
               ", username='" + username + '\'' +
               ", password='" + password + '\'' +
               ", destiny='" + destiny + '\'' +
               ", message='" + message + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AppPacket appPacket = (AppPacket) o;
        return auth == appPacket.auth &&
               signal == appPacket.signal &&
               Objects.equals(originSocketAddress, appPacket.originSocketAddress) &&
               Objects.equals(username, appPacket.username) &&
               Objects.equals(password, appPacket.password) &&
               Objects.equals(destiny, appPacket.destiny) &&
               Objects.equals(message, appPacket.message) &&
               Objects.equals(handler, appPacket.handler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signal, originSocketAddress, auth, username, password, destiny, message, handler);
    }

    public enum ProtocolSignal implements Serializable {
        HEARTBEAT,
        CLIENT_JOIN,
        CLIENT_QUIT,
        NEW_MESSAGE,

        SERVER_SHUTDOWN,

        AUTH_REQUEST,
        AUTH_RESPONSE,
        NEW_PRIVATE_MSG,
        NEW_GROUP_MSG,
        SERVER_BROADCAST
    }
}
