package chat.core;

import chat.model.AppPacket;
import chat.model.ISocketManager;
import chat.model.ProtocolSignal;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WorkerSocketManager implements ISocketManager {

    private Socket workerSocket;
    private ExecutorService workerManagerPool;

    private final AppPacket heartbeatPacket;
    private HeartbeatDaemon heartbeatDaemon;
    private CommandReceiver commandReceiver;
    private CommandTransmitter commandTransmitter;

    private BlockingQueue<AppPacket> serverInboundCommandQueue;
    private BlockingQueue<AppPacket> workerOutboundCommandQueue;
    private BlockingQueue<WorkerSocketManager> workerList;

    public WorkerSocketManager(Socket workerSocket, BlockingQueue<AppPacket> serverInboundCommandQueue, BlockingQueue<WorkerSocketManager> workerList) {
        this.workerSocket              = workerSocket;
        this.serverInboundCommandQueue = serverInboundCommandQueue;
        this.workerList                = workerList;
        heartbeatPacket                = new AppPacket(ProtocolSignal.HEARTBEAT, workerSocket.getLocalSocketAddress(), "kokoro", "heartbeat");

    }


    @Override
    public void startSocketManager() {
        workerOutboundCommandQueue = new ArrayBlockingQueue<>(Byte.MAX_VALUE);
        workerManagerPool          = Executors.newFixedThreadPool(3);
        heartbeatDaemon            = new HeartbeatDaemon(this);
        commandReceiver            = new CommandReceiver(this);
        commandTransmitter         = new CommandTransmitter(this);

        workerManagerPool.submit(commandTransmitter);
        workerManagerPool.submit(commandReceiver);
        workerManagerPool.submit(heartbeatDaemon);
    }


    @Override
    public void stopSocketManager() {
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
            closeSockets();
            workerList.remove(this);
        }
    }

    private void closeSockets() {
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

    @Override
    public void queueTransmission(AppPacket appPacket) {
        try {
            workerOutboundCommandQueue.put(appPacket);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public BlockingQueue<AppPacket> getInboundCommandQueue() {
        return serverInboundCommandQueue;
    }

    @Override
    public BlockingQueue<AppPacket> getOutboundCommandQueue() {
        return workerOutboundCommandQueue;
    }

    @Override
    public HeartbeatDaemon getHeartbeatDaemon() {
        return heartbeatDaemon;
    }

    @Override
    public OutputStream getOutputStream() {
        OutputStream outputStream = null;
        try {
            outputStream = workerSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream;
    }

    @Override
    public InputStream getInputStream() {
        InputStream inputStream = null;
        try {
            inputStream = workerSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputStream;
    }


    @Override
    public void sendHeartbeatPacket() {
        if (!workerOutboundCommandQueue.contains(heartbeatPacket)) {
            try {
                workerOutboundCommandQueue.put(heartbeatPacket);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void timeout() {
        stopSocketManager();
    }

    @Override
    public boolean isManagerAlive() {
        return workerSocket != null && !workerSocket.isClosed();
    }
}
