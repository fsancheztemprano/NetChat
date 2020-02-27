package app.core.packetmodel;

import app.core.AbstractNodeManager;
import com.google.common.base.MoreObjects;
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

    private String[] list;

    private transient AbstractNodeManager handler;

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

    public AbstractNodeManager getHandler() {
        return handler;
    }

    public void setHandler(AbstractNodeManager handler) {
        this.handler = handler;
    }

    public String[] getList() {
        return list;
    }

    public void setList(String[] list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("signal", signal)
                          .add("originSocketAddress", originSocketAddress)
                          .add("auth", auth)
                          .add("username", username)
                          .add("password", password)
                          .add("destiny", destiny)
                          .add("message", message)
                          .add("list", list)
                          .add("handler", handler)
                          .toString();
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
               Objects.equals(message, appPacket.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signal, originSocketAddress, auth, username, password, destiny, message);
    }

    public enum ProtocolSignal implements Serializable {
        HEARTBEAT,
        SERVER_BROADCAST,
        UNAUTHORIZED_REQUEST,

        AUTH_REQUEST,
        AUTH_RESPONSE,
        AUTH_REMOVE,

        CLIENT_JOIN,
        CLIENT_QUIT,
        NEW_MESSAGE,

        SERVER_SHUTDOWN,


        NEW_PRIVATE_MSG,
        NEW_GROUP_MSG,

        BROADCAST_USER_LIST
    }
}
