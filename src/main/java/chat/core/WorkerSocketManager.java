package chat.core;

import chat.model.IServerSocketManager;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import tools.log.Flogger;

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
            poolUpChildProcesses();
        } catch (IOException e) {
            Flogger.atInfo().withCause(e).log("ER-WSM-0001");
            stopSocketManager();
        } catch (Exception e) {
            Flogger.atInfo().withCause(e).log("ER-WSM-0000");
        }
    }

    @Override
    public synchronized void stopSocketManager() {
        if (isActive()) {
            try {
                workerList.remove(this);
                deactivateChildProcesses();
                closePool();
                closeSocket();
            } catch (Exception e) {
                Flogger.atInfo().withCause(e).log("ER-WSM-0002");
            }
        }
    }
}
