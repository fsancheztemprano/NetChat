package app.core;

import app.core.events.SessionEndEvent;
import app.core.events.SessionStartEvent;
import app.core.packetmodel.AppPacket;
import app.core.packetmodel.AppPacket.ProtocolSignal;
import app.core.packetmodel.AuthResponsePacket;
import java.net.Socket;
import javax.annotation.Nonnull;
import tools.log.Flogger;

public class WorkerNodeManager extends AbstractNodeManager {

    private final long serverID;


    public WorkerNodeManager(Socket managedSocket, long serverID) {
        super(managedSocket);
        this.commandProcessor = new WorkerCommandProcessor(this);
        this.serverID         = serverID;
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
        socketEventBus.post(active ? new SessionStartEvent(this) : new SessionEndEvent(this));
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

}