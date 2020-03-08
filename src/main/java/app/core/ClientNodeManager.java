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
        AppPacket loginRequest = new AppPacket(ProtocolSignal.CLIENT_LOGIN_REQUEST);
        loginRequest.setUsername(username);
        loginRequest.setPassword(hashedPass);
        queueTransmission(loginRequest);
    }

    @Override
    public synchronized boolean queueTransmission(@Nonnull AppPacket appPacket) {
        appPacket.setAuth(getSessionID());
        return super.queueTransmission(appPacket);
    }

    public void sendLogOutAction() {
        queueTransmission(new AppPacket(ProtocolSignal.CLIENT_LOGOUT_REQUEST));
        setSessionID(-1);
    }

    public void sendUserListRequest() {
        queueTransmission(new AppPacket(ProtocolSignal.CLIENT_REQUEST_USER_LIST));
    }

    public void sendGroupListRequest() {
        queueTransmission(new AppPacket(ProtocolSignal.CLIENT_REQUEST_GROUP_LIST));
    }

    public void sendPM(String username, String message) {
        AppPacket pmPacket = new AppPacket(ProtocolSignal.CLIENT_PM);
        pmPacket.setDestiny(username);
        pmPacket.setMessage(message);
        queueTransmission(pmPacket);
    }

    public void requestNewGroup(String newGroupName) {
        AppPacket newGroupRequest = new AppPacket(ProtocolSignal.CLIENT_REQUEST_NEW_GROUP);
        newGroupRequest.setMessage(newGroupName);
        queueTransmission(newGroupRequest);
    }
}
