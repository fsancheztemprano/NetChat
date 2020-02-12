package chat.core;

import chat.model.ChatPacket;
import java.io.IOException;
import java.net.Socket;
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

    private BlockingQueue<ChatPacket> outBoundCommandQueue;


    public WorkerManager(Socket workerSocket, BlockingQueue<ChatPacket> serverCommandQueue) throws IOException {
        this.workerSocket    = workerSocket;
        outBoundCommandQueue = new ArrayBlockingQueue<>(63);

        workerHeartbeatDaemon = new WorkerHeartbeatDaemon(outBoundCommandQueue, workerSocket.getLocalSocketAddress());

        workerCommandReceiver    = new WorkerCommandReceiver(serverCommandQueue, workerSocket.getInputStream(), workerHeartbeatDaemon);
        workerCommandTransmitter = new WorkerCommandTransmitter(outBoundCommandQueue, workerSocket.getOutputStream(), workerHeartbeatDaemon);

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
}
