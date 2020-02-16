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
    private IServerSocketManager serverSocketManager;

    public WorkerSocketManager(IServerSocketManager serverSocketManager, Socket managedSocket) {
        this.serverSocketManager = serverSocketManager;
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
            Flogger.atWarning().withCause(e).log("ER-WSM-0001");
            stopSocketManager();
        } catch (Exception e) {
            Flogger.atWarning().withCause(e).log("ER-WSM-0000");
        }
    }

    @Override
    public void stopSocketManager() {
        if (isActive()) {
            try {
                serverSocketManager.removeWorker(this);
                deactivateChildProcesses();
                closeSocket();
                closePool();
            } catch (Exception e) {
                Flogger.atWarning().withCause(e).log("ER-WSM-0002");
            } finally {
                setActive(false);
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    protected void notifySocketStatus(boolean active) {

    }
}
