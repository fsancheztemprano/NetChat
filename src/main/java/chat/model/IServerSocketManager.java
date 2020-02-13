package chat.model;

import java.util.concurrent.BlockingQueue;

public interface IServerSocketManager extends IManagerStartable {

    BlockingQueue<AppPacket> getServerCommandQueue();

    void transmitToAllClients(AppPacket appPacket);

    @Override
    boolean isManagerAlive();

    void serverShutdown();
}
