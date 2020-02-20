package app.core;

import app.core.packetmodel.AppPacket;
import app.core.packetmodel.AppPacket.ProtocolSignal;
import app.core.packetmodel.AuthResponsePacket;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import tools.log.Flogger;

public class WorkerSocketManager extends AbstractSocketManager {

    private ConcurrentHashMap<Long, WorkerSocketManager> workerList;
    private ServerSocketManager serverSocketManager;
    private long serverID;


    public WorkerSocketManager(ServerSocketManager serverSocketManager, Socket managedSocket) {
        this.serverSocketManager = serverSocketManager;
        this.managedSocket       = managedSocket;
        this.inboundCommandQueue = serverSocketManager.getServerCommandQueue();
        this.workerList          = serverSocketManager.getWorkerList();
        socketEventBus           = serverSocketManager.getSocketEventBus();
        setSessionID(generateTimeHashID());
        serverID = serverSocketManager.getSessionID();
    }

    @Override
    public synchronized void startSocketManager() {
        try {
            setStreams();
            setActive(true);
            outboundCommandQueue = new ArrayBlockingQueue<>(Byte.MAX_VALUE);
            managerPool          = Executors.newFixedThreadPool(3);

            initializeChildProcesses();
            poolUpChildProcesses();
        } catch (IOException e) {
            Flogger.atWarning().withCause(e).log("ER-WSM-0001");
            stopSocketManager();
        } catch (Exception e) {
            Flogger.atWarning().withCause(e).log("ER-WSM-0000");
        }
    }

    @Override
    public void stopSocketManager() {
        if (isActive()) {
            try {
                serverSocketManager.removeWorker(this);
                deactivateChildProcesses();
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

    @Override
    public synchronized void queueTransmission(@Nonnull AppPacket appPacket) {
        if (appPacket.getSignal() != ProtocolSignal.AUTH_RESPONSE)//only AUTH_RESPONSE delivers sessionID, others return serverID
            appPacket.setAuth(getSessionID());
        super.queueTransmission(appPacket);
    }
}
