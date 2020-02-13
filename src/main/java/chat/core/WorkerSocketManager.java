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
import tools.log.Flogger;

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
        if (heartbeatDaemon != null)
            heartbeatDaemon.setActive(false);
        if (commandTransmitter != null)
            commandTransmitter.setActive(false);
        if (commandReceiver != null)
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
            Flogger.atInfo().withCause(ie).log("ER-WSM-0001");
        } catch (NullPointerException npw) {
            Flogger.atInfo().withCause(npw).log("ER-WSM-0002");
        } catch (Exception e) {
            Flogger.atInfo().withCause(e).log("ER-WSM-0000");
        } finally {
            try {
                workerSocket.close();
            } catch (IOException e) {
                Flogger.atInfo().withCause(e).log("ER-WSM-0003");
            } finally {
                workerList.remove(this);
            }
        }
    }

    @Override
    public void queueTransmission(AppPacket appPacket) {
        try {
            workerOutboundCommandQueue.put(appPacket);
        } catch (InterruptedException e) {
            Flogger.atInfo().withCause(e).log("ER-WSM-0004");
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
            Flogger.atInfo().withCause(e).log("ER-WSM-0005");
        }
        return outputStream;
    }

    @Override
    public InputStream getInputStream() {
        InputStream inputStream = null;
        try {
            inputStream = workerSocket.getInputStream();
        } catch (IOException e) {
            Flogger.atInfo().withCause(e).log("ER-WSM-0006");
        }
        return inputStream;
    }


    @Override
    public void sendHeartbeatPacket() {
        if (!workerOutboundCommandQueue.contains(heartbeatPacket)) {
            try {
                workerOutboundCommandQueue.put(heartbeatPacket);
            } catch (InterruptedException e) {
                Flogger.atInfo().withCause(e).log("ER-WSM-0007");
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
