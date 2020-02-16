package chat.model;

import chat.core.WorkerSocketManager;
import java.util.concurrent.BlockingQueue;

public interface IServerSocketManager extends IActivable {

    void serverShutdown();

    BlockingQueue<AppPacket> getServerCommandQueue();

    void transmitToAllClients(AppPacket appPacket);

    BlockingQueue<WorkerSocketManager> getWorkerList();

    void notifyLogOutput(String output);

    void removeWorker(WorkerSocketManager workerSocketManager);
}
