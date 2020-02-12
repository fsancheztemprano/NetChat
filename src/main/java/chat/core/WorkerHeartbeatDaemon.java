package chat.core;

import chat.model.ActivableThread;
import chat.model.AppPacket;
import chat.model.IHeartBeatTimeHolder;
import chat.model.ProtocolSignal;
import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;

public class WorkerHeartbeatDaemon extends ActivableThread implements IHeartBeatTimeHolder {

    private final BlockingQueue<AppPacket> outBoundCommandQueue;
    private final AppPacket heartbeatPacket;

    private volatile LocalDateTime lastHeartbeat;

    public WorkerHeartbeatDaemon(BlockingQueue<AppPacket> outBoundCommandQueue, SocketAddress localSocketAddress) {
        this.outBoundCommandQueue = outBoundCommandQueue;
        heartbeatPacket           = new AppPacket(ProtocolSignal.HEARTBEAT, localSocketAddress, "kokoro", "heartbeat");
    }

    @Override
    public void run() {
        while (isActive()) {
            try {
                if (lastHeartbeat == null || lastHeartbeat.plusSeconds(Globals.HEARTBEAT_DELAY).isAfter(LocalDateTime.now())) {
                    sendHeartBeat();
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateHeartBeatTime() {
        lastHeartbeat = LocalDateTime.now();
    }

    private void sendHeartBeat() throws InterruptedException {
        outBoundCommandQueue.put(heartbeatPacket);
    }
}
