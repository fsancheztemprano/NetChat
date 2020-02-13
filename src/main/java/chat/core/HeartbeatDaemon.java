package chat.core;

import chat.model.ActivableThread;
import chat.model.IHeartBeatDaemon;
import chat.model.IHeartBeater;
import java.time.LocalDateTime;

public class HeartbeatDaemon extends ActivableThread implements IHeartBeatDaemon {

    private IHeartBeater manager;
    private LocalDateTime lastHeartbeat;

    public HeartbeatDaemon(IHeartBeater context) {
        this.manager = context;

    }

    @Override
    public void run() {
        while (isActive()) {
            try {
                if (Globals.WORKER_HEARTBEAT_TIMEOUT != 0 && lastHeartbeat != null && lastHeartbeat.plusSeconds(Globals.WORKER_HEARTBEAT_TIMEOUT).isAfter(LocalDateTime.now())) {
                    manager.timeout();
                } else if (lastHeartbeat == null || lastHeartbeat.plusSeconds(Globals.HEARTBEAT_DELAY).isAfter(LocalDateTime.now())) {
                    sendHeartBeat();
                }
                Thread.sleep(1000);
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

    private void sendHeartBeat() {
        if (manager instanceof WorkerManager)
            manager.sendHeartbeatPacket();
    }
}
