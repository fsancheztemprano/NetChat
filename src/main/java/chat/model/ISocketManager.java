package chat.model;

import chat.core.HeartbeatDaemon;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

public interface ISocketManager extends IManagerStartable {

    void sendHeartbeatPacket();

    void timeout();

    @Override
    boolean isManagerAlive();

    void startSocketManager();

    void stopSocketManager();

    void queueTransmission(AppPacket appPacket);

    BlockingQueue<AppPacket> getInboundCommandQueue();

    BlockingQueue<AppPacket> getOutboundCommandQueue();

    HeartbeatDaemon getHeartbeatDaemon();

    OutputStream getOutputStream();

    InputStream getInputStream();
}
