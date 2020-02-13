package chat.core;

import chat.model.ActivableThread;
import chat.model.IHeartBeatDaemon;
import chat.model.IHeartBeater;
import java.time.LocalDateTime;

public class HeartbeatDaemon extends ActivableThread implements IHeartBeatDaemon {

    private IHeartBeater manager;
    private LocalDateTime lastHeartbeat = null;

    public HeartbeatDaemon(IHeartBeater context) {
        this.manager = context;

    }

    @Override
    public void run() {
        setActive(true);
        while (isActive()) {
            try {
                if (Globals.HEARTBEAT_TIMEOUT != 0 && lastHeartbeat != null && lastHeartbeat.plusSeconds(Globals.HEARTBEAT_TIMEOUT).isBefore(LocalDateTime.now())) {
                    manager.timeout();
                } else if (manager instanceof WorkerManager && (lastHeartbeat == null || lastHeartbeat.plusSeconds(Globals.HEARTBEAT_DELAY).isBefore(LocalDateTime.now()))) {
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
