package chat.core;

import chat.model.AppPacket;
import chat.model.IHeartBeater;
import chat.model.ProtocolSignal;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WorkerManager implements IHeartBeater {

    private Socket workerSocket;
    private ExecutorService workerManagerPool;

    private final AppPacket heartbeatPacket;
    private HeartbeatDaemon heartbeatDaemon;
    private CommandReceiver commandReceiver;
    private CommandTransmitter commandTransmitter;

    private BlockingQueue<AppPacket> workerCommandQueue;
    private BlockingQueue<WorkerManager> workerList;

    public WorkerManager(Socket workerSocket, BlockingQueue<AppPacket> serverCommandQueue, BlockingQueue<WorkerManager> workerList) throws IOException {
        this.workerSocket  = workerSocket;
        this.workerList    = workerList;
        heartbeatPacket    = new AppPacket(ProtocolSignal.HEARTBEAT, workerSocket.getLocalSocketAddress(), "kokoro", "heartbeat");
        workerCommandQueue = new ArrayBlockingQueue<>(Byte.MAX_VALUE);
        workerManagerPool  = Executors.newFixedThreadPool(3);
        heartbeatDaemon    = new HeartbeatDaemon(this);
        commandReceiver    = new CommandReceiver(serverCommandQueue, workerSocket.getInputStream(), heartbeatDaemon);
        commandTransmitter = new CommandTransmitter(workerCommandQueue, workerSocket.getOutputStream(), heartbeatDaemon);
    }


    public void startWorker() {

        workerManagerPool.submit(commandTransmitter);
        workerManagerPool.submit(commandReceiver);
        workerManagerPool.submit(heartbeatDaemon);
    }


    public void stopWorker() {
        heartbeatDaemon.setActive(false);
        commandTransmitter.setActive(false);
        commandReceiver.setActive(false);

        workerManagerPool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!workerManagerPool.awaitTermination(60, TimeUnit.SECONDS)) {
                workerManagerPool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!workerManagerPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            workerManagerPool.shutdownNow();
        } finally {
            closeSocket();
            workerList.remove(this);
        }
    }

    private void closeSocket() {
        try {
            workerSocket.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workerSocket.getInputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    workerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    @Override
    public void sendHeartbeatPacket() {
        try {
            workerCommandQueue.put(heartbeatPacket);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void timeout() {
        stopWorker();
    }
}
