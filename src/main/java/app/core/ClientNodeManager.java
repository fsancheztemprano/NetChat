package app.core;

import app.Globals;
import app.core.AppPacket.ProtocolSignal;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.annotation.Nonnull;
import tools.HashTools;
import tools.log.Flogger;

public class ClientNodeManager extends AbstractNodeManager {

    private String hostname = Globals.DEFAULT_SERVER_HOSTNAME;
    private int port = Globals.DEFAULT_SERVER_PORT;


    public ClientNodeManager() {
        super(new Socket());
        setSessionID(-1);
        commandProcessor = new ClientCommandProcessor(this);
    }

    @Override
    public synchronized void startSocketManager() {
        InetSocketAddress addr = new InetSocketAddress(hostname, port);
        log("Conectando: " + addr.getAddress());
        try {
            managedSocket.connect(addr, Globals.CLIENT_CONNECT_TIMEOUT);
            setActive(true);
            setStreams();
            poolUpChildProcesses();
            log("Conectado: " + addr.getAddress());
//        } catch (ConnectException ce) {
//            Flogger.atWarning().withCause(ce).log("ER-CSM-0001");
//        } catch (IOException ce) {
//            Flogger.atWarning().withCause(ce).log("ER-CSM-0001");
        } catch (Exception ce) {
            Flogger.atWarning().withCause(ce).log("ER-CSM-0000");
            stopSocketManager();
        }
    }

    @Override
    public void stopSocketManager() {
        if (isActive() || isSocketOpen()) {
            try {
                disableChildProcesses();
                closeSocket();
                closePool();
            } catch (Exception e) {
                Flogger.atWarning().withCause(e).log("ER-CSM-0003");
            } finally {
                log("Conexion Finalizada");
                setActive(false);
                Thread.currentThread().interrupt();
            }
        }
    }


    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }


    void sendLoginRequest(@Nonnull final String username, @Nonnull final String password) {
        if (username.length() < 4 || password.length() < 4)
            return;
        String hashedPass = HashTools.getSha256(password);
        queueTransmission(AppPacket.ofType(ProtocolSignal.CLIENT_REQUEST_LOGIN).setUsername(username).setPassword(hashedPass));
    }

    @Override
    public synchronized void queueTransmission(@Nonnull AppPacket appPacket) {
        super.queueTransmission(appPacket.setAuth(getSessionID()));
    }

    public void sendLogOutAction() {
        queueTransmission(AppPacket.ofType(ProtocolSignal.CLIENT_REQUEST_LOGOUT));
        setSessionID(-1);
    }

    public void sendUserListRequest() {
        queueTransmission(AppPacket.ofType(ProtocolSignal.CLIENT_REQUEST_USER_LIST));
    }

    public void sendGroupListRequest() {
        queueTransmission(AppPacket.ofType(ProtocolSignal.CLIENT_REQUEST_GROUP_LIST));
    }

    public void sendPrivateMessage(String username, String message) {
        queueTransmission(AppPacket.ofType(ProtocolSignal.CLIENT_SEND_PRIVATE_MESSAGE).setDestiny(username).setMessage(message));
    }

    public void requestNewGroup(String newGroupName) {
        queueTransmission(AppPacket.ofType(ProtocolSignal.CLIENT_REQUEST_NEW_GROUP).setMessage(newGroupName));
    }

    public void requestJoinGroup(String selectedGroup) {
        queueTransmission(AppPacket.ofType(ProtocolSignal.CLIENT_REQUEST_GROUP_JOIN).setDestiny(selectedGroup));
    }

    public void requestQuitGroup(String selectedGroup) {
        queueTransmission(AppPacket.ofType(ProtocolSignal.CLIENT_REQUEST_GROUP_QUIT).setDestiny(selectedGroup));
    }

    public void sendGroupMessage(String groupName, String message) {
        queueTransmission(AppPacket.ofType(ProtocolSignal.CLIENT_SEND_GROUP_MESSAGE).setDestiny(groupName).setMessage(message));
    }
}
