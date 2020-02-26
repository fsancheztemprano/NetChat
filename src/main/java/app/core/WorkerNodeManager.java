package app.core;

import app.core.packetmodel.AppPacket;
import app.core.packetmodel.AppPacket.ProtocolSignal;
import app.core.packetmodel.AuthResponsePacket;
import com.google.common.flogger.StackSize;
import java.net.Socket;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import tools.log.Flogger;

public class WorkerNodeManager extends AbstractNodeManager {

    private ServerSocketManager serverSocketManager;
    private final long serverID;


    public WorkerNodeManager(ServerSocketManager serverSocketManager, Socket managedSocket) {
        this.serverSocketManager = serverSocketManager;
        this.managedSocket       = managedSocket;
        this.commandProcessor    = new WorkerCommandProcessor(this);
        this.socketEventBus      = serverSocketManager.getSocketEventBus();
        this.serverID            = serverSocketManager.getSessionID();
        managerPool              = Executors.newFixedThreadPool(4);
    }

    @Override
    public synchronized void startSocketManager() {
        try {
            setActive(true);
            setStreams();
            initializeChildProcesses(commandProcessor);
            poolUpChildProcesses();
//        } catch (IOException e) {
//            Flogger.atWarning().withCause(e).log("ER-WSM-0001");
        } catch (Exception e) {
            Flogger.atWarning().withStackTrace(StackSize.FULL).withCause(e).log("ER-WSM-0000");
            stopSocketManager();
        }
    }

    @Override
    public void stopSocketManager() {
        if (isActive()) {
            try {
                serverSocketManager.removeWorker(this);
                disableChildProcesses();
                closeSocket();
                closePool();
            } catch (Exception e) {
                Flogger.atWarning().withCause(e).log("ER-WSM-0002");
            } finally {
                setActive(false);
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override //no notification sent
    public void setActive(boolean active) {
        this.active.set(active);
    }

    public void sendAuthApproval(boolean approved) {
        AuthResponsePacket authResponsePacket = new AuthResponsePacket(approved ? getSessionID() : -1);
        queueTransmission(authResponsePacket);
    }

    //only AUTH_RESPONSE delivers sessionID, others return serverID
    @Override
    public synchronized void queueTransmission(@Nonnull AppPacket appPacket) {
        if (appPacket.getSignal() != ProtocolSignal.AUTH_RESPONSE) {
            appPacket.setAuth(serverID);
        }
        super.queueTransmission(appPacket);
    }

    public ServerSocketManager getServerSocketManager() {
        return serverSocketManager;
    }
}
