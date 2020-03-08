package app.core;

import app.core.AppPacket.ProtocolSignal;
import app.core.events.WorkerStatusEvent;
import com.google.common.eventbus.EventBus;
import java.net.Socket;
import javax.annotation.Nonnull;
import tools.log.Flogger;

public class WorkerNodeManager extends AbstractNodeManager {

    private final long serverID;
    private final EventBus serverEventBus;

    public WorkerNodeManager(ServerSocketManager serverSocketManager, Socket managedSocket) {
        super(managedSocket);
        this.serverID         = serverSocketManager.getSessionID();
        this.serverEventBus   = serverSocketManager.getSocketEventBus();
        this.commandProcessor = new WorkerCommandProcessor(this);
    }

    public EventBus getServerEventBus() {
        return serverEventBus;
    }

    @Override
    public synchronized void startSocketManager() {
        try {
            setActive(true);
            setStreams();
            poolUpChildProcesses();
//        } catch (IOException e) {
//            Flogger.atWarning().withCause(e).log("ER-WSM-0001");
        } catch (Exception e) {
            Flogger.atWarning().withCause(e).log("ER-WSM-0000");
            stopSocketManager();
        }
    }

    @Override
    public synchronized void stopSocketManager() {
        if (isActive()) {
            try {
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

    @Override
    public void setActive(boolean active) {
        this.active.set(active);
        socketEventBus.post(new WorkerStatusEvent(this, active));
    }


    //only AUTH_RESPONSE delivers sessionID, others return serverID
    @Override
    public synchronized boolean queueTransmission(@Nonnull AppPacket appPacket) {
        if (appPacket.getSignal() != ProtocolSignal.SERVER_RESPONSE_LOGIN_SUCCESS) {
            appPacket.setAuth(serverID);
        }
        return super.queueTransmission(appPacket);
    }

}
