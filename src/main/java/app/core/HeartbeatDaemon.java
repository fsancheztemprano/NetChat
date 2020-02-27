package app.core;

import app.Globals;
import app.core.AppPacket.ProtocolSignal;
import java.time.LocalDateTime;
import tools.log.Flogger;

public class HeartbeatDaemon extends Activable implements Runnable {

    private final AbstractNodeManager manager;
    private final AppPacket heartbeatPacket = new AppPacket(ProtocolSignal.HEARTBEAT);
    private LocalDateTime lastHeartbeat;

    public HeartbeatDaemon(AbstractNodeManager socketManager) {
        this.manager  = socketManager;
        lastHeartbeat = LocalDateTime.now().minusSeconds(Globals.HEARTBEAT_DELAY - Globals.HEARTBEAT_FIRST);

    }

    @Override
    public void run() {
        setActive(true);
        while (isActive()) {
            try {
                if (Globals.HEARTBEAT_TIMEOUT != 0 && lastHeartbeat != null && lastHeartbeat.plusSeconds(Globals.HEARTBEAT_TIMEOUT).isBefore(LocalDateTime.now())) {
                    manager.log("Heartbeat Timeout");
                    manager.stopSocketManager();
                } else if (manager instanceof WorkerNodeManager && (lastHeartbeat == null || lastHeartbeat.plusSeconds(Globals.HEARTBEAT_DELAY).isBefore(LocalDateTime.now()))) {
                    manager.queueTransmission(heartbeatPacket);
                }
                Thread.sleep(Globals.HEARTBEAT_SLEEP_INTERVAL);
            } catch (InterruptedException | NullPointerException ie) {
                Flogger.atWarning().withCause(ie).log("ER-HD-0001");
                setActive(false);
            } catch (Exception e) {
                Flogger.atWarning().withCause(e).log("ER-HD-0000");
                setActive(false);
            }
        }
    }

    public synchronized void updateHeartBeatTime() {
        lastHeartbeat = LocalDateTime.now();
    }
}
