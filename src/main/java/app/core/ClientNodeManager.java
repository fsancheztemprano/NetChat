package app.core;

import app.core.packetmodel.AppPacket;
import app.core.packetmodel.AuthRemovePacket;
import app.core.packetmodel.AuthRequestPacket;
import com.google.common.eventbus.EventBus;
import com.google.common.flogger.StackSize;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import tools.log.Flogger;

public class ClientNodeManager extends AbstractNodeManager {

    private String hostname = Globals.DEFAULT_SERVER_HOSTNAME;
    private int port = Globals.DEFAULT_SERVER_PORT;

    @SuppressWarnings("UnstableApiUsage")
    public ClientNodeManager() {
        socketEventBus   = new EventBus("ClientEventBus");
        commandProcessor = new ClientCommandProcessor(this);
        managerPool      = Executors.newFixedThreadPool(4);
    }

    @Override
    public synchronized void startSocketManager() {
        managedSocket = new Socket();
        InetSocketAddress addr = new InetSocketAddress(hostname, port);
        log("Conectando: " + addr.getAddress());
        try {
            managedSocket.connect(addr, Globals.CLIENT_CONNECT_TIMEOUT);
            setActive(true);
            setStreams();
            initializeChildProcesses(commandProcessor);
            poolUpChildProcesses();
            log("Conectado: " + addr.getAddress());
//        } catch (ConnectException ce) {
//            Flogger.atWarning().withStackTrace(StackSize.FULL).withCause(ce).log("ER-CSM-0001");
//        } catch (IOException ce) {
//            Flogger.atWarning().withStackTrace(StackSize.FULL).withCause(ce).log("ER-CSM-0001");
        } catch (Exception ce) {
            Flogger.atWarning().withStackTrace(StackSize.FULL).withCause(ce).log("ER-CSM-0000");
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

                managedSocket        = null;
                managerPool          = null;
                heartbeatPacket      = null;
                heartbeatDaemon      = null;
                commandReceiver      = null;
                commandTransmitter   = null;
                inputStream          = null;
                outputStream         = null;
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


    @SuppressWarnings("UnstableApiUsage")
    void sendLoginRequest(@Nonnull final String username, @Nonnull final String password) {
        if (username.length() < 4 || password.length() < 4)
            return;
        HashFunction hasher = Hashing.sha256();
        HashCode sha256 = hasher.newHasher()
                                .putUnencodedChars(password)
                                .hash();
        String hashedPass = sha256.toString();//1st time sha256
        AppPacket loginRequest = new AuthRequestPacket(username, hashedPass);
        queueTransmission(loginRequest);
    }

    @Override
    public synchronized void queueTransmission(@Nonnull AppPacket appPacket) {
        appPacket.setAuth(getSessionID());
        super.queueTransmission(appPacket);
    }

    public void sendLogOutAction() {
        queueTransmission(new AuthRemovePacket());
        setSessionID(-1);
    }
}
