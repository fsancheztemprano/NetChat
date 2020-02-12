package chat.core;

import chat.model.ActivableThread;
import chat.model.AppPacket;
import chat.model.IHeartBeatTimeHolder;
import chat.model.ProtocolSignal;
import java.time.LocalDateTime;

public class WorkerHeartbeatDaemon extends ActivableThread implements IHeartBeatTimeHolder {

    private final AppPacket heartbeatPacket;

    private LocalDateTime lastHeartbeat;
    private WorkerManager workerManager;

    public WorkerHeartbeatDaemon(WorkerManager workerManager) {
        this.workerManager = workerManager;
        heartbeatPacket    = new AppPacket(ProtocolSignal.HEARTBEAT, workerManager.getLocalSocketAddress(), "kokoro", "heartbeat");
    }

    @Override
    public void run() {
        while (isActive()) {
            try {
                if (Globals.WORKER_TIMEOUT != 0 && lastHeartbeat != null && lastHeartbeat.plusSeconds(Globals.WORKER_TIMEOUT).isAfter(LocalDateTime.now())) {
                    workerManager.stopWorker();
                } else if (lastHeartbeat == null || lastHeartbeat.plusSeconds(Globals.HEARTBEAT_DELAY).isAfter(LocalDateTime.now())) {
                    sendHeartBeat();
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void updateHeartBeatTime() {
        lastHeartbeat = LocalDateTime.now();
    }

    private void sendHeartBeat() throws InterruptedException {
        workerManager.getWorkerCommandQueue().put(heartbeatPacket);
    }
}
