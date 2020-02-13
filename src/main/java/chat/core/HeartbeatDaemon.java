package chat.core;

import chat.model.ActivableThread;
import chat.model.IHeartbeatDaemon;
import chat.model.ISocketManager;
import java.time.LocalDateTime;

public class HeartbeatDaemon extends ActivableThread implements IHeartbeatDaemon {

    private ISocketManager manager;
    private LocalDateTime lastHeartbeat = null;

    public HeartbeatDaemon(ISocketManager socketManager) {
        this.manager = socketManager;

    }

    @Override
    public void run() {
        setActive(true);
        while (isActive()) {
            try {
                if (Globals.HEARTBEAT_TIMEOUT != 0 && lastHeartbeat != null && lastHeartbeat.plusSeconds(Globals.HEARTBEAT_TIMEOUT).isBefore(LocalDateTime.now())) {
                    manager.timeout();
                } else if (manager instanceof WorkerSocketManager && (lastHeartbeat == null || lastHeartbeat.plusSeconds(Globals.HEARTBEAT_DELAY).isBefore(LocalDateTime.now()))) {
                    manager.sendHeartbeatPacket();
                }
                Thread.sleep(Globals.HEARTBEAT_INTERVAL);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void updateHeartBeatTime() {
        lastHeartbeat = LocalDateTime.now();
    }
}
