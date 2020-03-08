package app.core;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.net.SocketAddress;
import java.util.Objects;

public class AppPacket implements Serializable, Cloneable {

    private final ProtocolSignal signal;
    private SocketAddress originSocketAddress;
    private long auth;

    private String username;
    private String password;

    private String destiny;
    private String message;

    private String[] list;

    private transient AbstractNodeManager handler;

    public static AppPacket ofType(ProtocolSignal protocolSignal) {
        return new AppPacket(protocolSignal);
    }

    private AppPacket(ProtocolSignal signal) {
        this.signal = signal;
    }

    public ProtocolSignal getSignal() {
        return signal;
    }

    public SocketAddress getOriginSocketAddress() {
        return originSocketAddress;
    }

    public long getAuth() {
        return auth;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDestiny() {
        return destiny;
    }

    public String getMessage() {
        return message;
    }

    public AbstractNodeManager getHandler() {
        return handler;
    }

    public String[] getList() {
        return list;
    }

    public AppPacket setOriginSocketAddress(SocketAddress originSocketAddress) {
        this.originSocketAddress = originSocketAddress;
        return this;
    }

    protected AppPacket setAuth(long auth) {
        this.auth = auth;
        return this;
    }

    public AppPacket setUsername(String username) {
        this.username = username;
        return this;
    }

    public AppPacket setPassword(String password) {
        this.password = password;
        return this;
    }

    public AppPacket setDestiny(String destiny) {
        this.destiny = destiny;
        return this;
    }

    public AppPacket setMessage(String message) {
        this.message = message;
        return this;
    }

    public AppPacket setHandler(AbstractNodeManager handler) {
        this.handler = handler;
        return this;
    }

    public AppPacket setList(String[] list) {
        this.list = list;
        return this;
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

        CLIENT_LOGIN_REQUEST,
        AUTH_RESPONSE,
        CLIENT_LOGOUT_REQUEST,

        NEW_MESSAGE,

        SERVER_SHUTDOWN,


        CLIENT_PM,
        CLIENT_PM_ACK,
        CLIENT_GROUP_MSG,
        CLIENT_REQUEST_USER_LIST,
        CLIENT_REQUEST_GROUP_LIST,

        CLIENT_REQUEST_NEW_GROUP,

        SERVER_SEND_USER_LIST,
        SERVER_SEND_GROUP_LIST,

        SERVER_RESPONSE_NEW_GROUP_DENIED
    }
}
