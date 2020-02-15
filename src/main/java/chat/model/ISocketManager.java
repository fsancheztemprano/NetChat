package chat.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

public interface ISocketManager extends IActivable {

    void startSocketManager();

    void stopSocketManager();

    void queueTransmission(String message);

    void sendHeartbeatPacket();

    void queueTransmission(AppPacket appPacket);

    boolean isSocketOpen();

    InputStream getInputStream();

    OutputStream getOutputStream();

    BlockingQueue<AppPacket> getInboundCommandQueue();

    BlockingQueue<AppPacket> getOutboundCommandQueue();

    void updateHeartbeatDaemonTime();
}
