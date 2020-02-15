package chat.core;

import chat.model.IServerSocketManager;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

public class WorkerSocketManager extends AbstractSocketManager {


    private BlockingQueue<WorkerSocketManager> workerList;

    public WorkerSocketManager(IServerSocketManager serverSocketManager, Socket managedSocket) {
        this.managedSocket       = managedSocket;
        this.inboundCommandQueue = serverSocketManager.getServerCommandQueue();
        this.workerList          = serverSocketManager.getWorkerList();
    }

    @Override
    public synchronized void startSocketManager() {
        try {
            setStreams();
            setActive(true);
            outboundCommandQueue = new ArrayBlockingQueue<>(Byte.MAX_VALUE);
            managerPool          = Executors.newFixedThreadPool(3);

            initializeChildProcesses();
        } catch (IOException e) {
            e.printStackTrace();
            stopSocketManager();
        }
    }

    @Override
    public synchronized void stopSocketManager() {
        if (isActive()) {
            deactivateChildProcesses();
            closePool();
            closeSocket();
            workerList.remove(this);
        }
    }
}
