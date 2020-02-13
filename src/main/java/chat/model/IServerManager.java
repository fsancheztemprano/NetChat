package chat.model;

import java.util.concurrent.BlockingQueue;

public interface IServerManager extends IManagerStartable {

    BlockingQueue<AppPacket> getServerCommandQueue();

    void transmitToAllClients(AppPacket appPacket);

    @Override
    boolean isManagerAlive();

    void serverShutdown();
}
