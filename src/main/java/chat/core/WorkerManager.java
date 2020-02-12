package chat.core;

import chat.model.AppPacket;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WorkerManager {

    private Socket workerSocket;
    private ExecutorService workerPool = Executors.newFixedThreadPool(3);

    private WorkerHeartbeatDaemon workerHeartbeatDaemon;
    private WorkerCommandReceiver workerCommandReceiver;
    private WorkerCommandTransmitter workerCommandTransmitter;

    private BlockingQueue<AppPacket> workerCommandQueue;


    public WorkerManager(Socket workerSocket, BlockingQueue<AppPacket> serverCommandQueue) throws IOException {
        this.workerSocket        = workerSocket;
        workerCommandQueue       = new ArrayBlockingQueue<>(Byte.MAX_VALUE);
        workerHeartbeatDaemon    = new WorkerHeartbeatDaemon(this);
        workerCommandReceiver    = new WorkerCommandReceiver(serverCommandQueue, workerSocket.getInputStream(), workerHeartbeatDaemon);
        workerCommandTransmitter = new WorkerCommandTransmitter(workerCommandQueue, workerSocket.getOutputStream(), workerHeartbeatDaemon);
    }


    public void startWorker() {
        workerPool.submit(workerCommandTransmitter);
        workerPool.submit(workerCommandReceiver);

        workerPool.submit(workerHeartbeatDaemon);
    }


    public void stopWorker() {
        workerHeartbeatDaemon.setActive(false);
        workerCommandTransmitter.setActive(false);
        workerCommandReceiver.setActive(false);

        workerPool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!workerPool.awaitTermination(60, TimeUnit.SECONDS)) {
                workerPool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!workerPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            workerPool.shutdownNow();
        } finally {
            try {
                workerSocket.getOutputStream().close();
                workerSocket.getInputStream().close();
                workerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //TODO remove from workerList
            }
        }
    }

    public void queueTransmission(AppPacket appPacket) {
        try {
            workerCommandQueue.put(appPacket);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    BlockingQueue<AppPacket> getWorkerCommandQueue() {
        return workerCommandQueue;
    }

    SocketAddress getLocalSocketAddress() {
        return workerSocket.getLocalSocketAddress();
    }
}
