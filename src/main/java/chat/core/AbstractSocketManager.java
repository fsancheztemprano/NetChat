package chat.core;

import chat.model.ActivableNotifier;
import chat.model.AppPacket;
import chat.model.ISocketManager;
import chat.model.ProtocolSignal;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import tools.log.Flogger;

public abstract class AbstractSocketManager extends ActivableNotifier implements ISocketManager {

    protected Socket managedSocket;
    protected ExecutorService managerPool;

    protected AppPacket heartbeatPacket;
    protected HeartbeatDaemon heartbeatDaemon;
    protected CommandReceiver commandReceiver;
    protected CommandTransmitter commandTransmitter;

    protected BlockingQueue<AppPacket> inboundCommandQueue;
    protected BlockingQueue<AppPacket> outboundCommandQueue;
    protected InputStream inputStream;
    protected OutputStream outputStream;


    public Socket getManagedSocket() {
        return managedSocket;
    }

    public void setManagedSocket(Socket managedSocket) {
        this.managedSocket = managedSocket;
    }

    public ExecutorService getManagerPool() {
        return managerPool;
    }

    public void setManagerPool(ExecutorService managerPool) {
        this.managerPool = managerPool;
    }

    public AppPacket getHeartbeatPacket() {
        return heartbeatPacket != null
               ? heartbeatPacket
               : new AppPacket(ProtocolSignal.HEARTBEAT,
                               managedSocket.getLocalSocketAddress(),
                               "kokoro",
                               "heartbeat");
    }

    public void setHeartbeatPacket(AppPacket heartbeatPacket) {
        this.heartbeatPacket = heartbeatPacket;
    }

    public HeartbeatDaemon getHeartbeatDaemon() {
        return heartbeatDaemon;
    }

    public void setHeartbeatDaemon(HeartbeatDaemon heartbeatDaemon) {
        this.heartbeatDaemon = heartbeatDaemon;
    }

    public CommandReceiver getCommandReceiver() {
        return commandReceiver;
    }

    public void setCommandReceiver(CommandReceiver commandReceiver) {
        this.commandReceiver = commandReceiver;
    }

    public CommandTransmitter getCommandTransmitter() {
        return commandTransmitter;
    }

    public void setCommandTransmitter(CommandTransmitter commandTransmitter) {
        this.commandTransmitter = commandTransmitter;
    }


    public void setInboundCommandQueue(BlockingQueue<AppPacket> inboundCommandQueue) {
        this.inboundCommandQueue = inboundCommandQueue;
    }

    public void setOutboundCommandQueue(BlockingQueue<AppPacket> outboundCommandQueue) {
        this.outboundCommandQueue = outboundCommandQueue;
    }

    @Override
    public BlockingQueue<AppPacket> getInboundCommandQueue() {
        return inboundCommandQueue;
    }

    @Override
    public BlockingQueue<AppPacket> getOutboundCommandQueue() {
        return outboundCommandQueue;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void setOutputStream() throws IOException {
        this.outputStream = managedSocket.getOutputStream();
    }

    public void setInputStream() throws IOException {
        this.inputStream = managedSocket.getInputStream();
    }

    public void setStreams() throws IOException {
        setInputStream();
        setOutputStream();
    }

    @Override
    public void queueTransmission(AppPacket appPacket) {
        try {
            outboundCommandQueue.put(appPacket);
        } catch (InterruptedException e) {
            Flogger.atInfo().withCause(e).log("ER-WSM-0004");
        }

    }

    @Override
    public void queueTransmission(String message) {
        AppPacket newMessage = new AppPacket(ProtocolSignal.NEW_MESSAGE, managedSocket.getLocalSocketAddress(), "cli", message);
        queueTransmission(newMessage);
    }


    @Override
    public void sendHeartbeatPacket() {
        if (!outboundCommandQueue.contains(heartbeatPacket)) {
            try {
                outboundCommandQueue.put(getHeartbeatPacket());
            } catch (InterruptedException e) {
                Flogger.atInfo().withCause(e).log("ER-WSM-0007");
            }
        }
    }


    @Override
    public boolean isSocketOpen() {
        return managedSocket != null && !managedSocket.isClosed();
    }

    public void closeSocket() {
        try {
            if (isSocketOpen()) {
                managedSocket.close();
                setActive(false);
            }
        } catch (IOException e) {
            Flogger.atInfo().withCause(e).log("ER-WSM-0003");
        }
    }

    protected void closePool() {
        if (managerPool != null) {
            managerPool.shutdown(); // Disable new tasks from being submitted
            try {
                // Wait a while for existing tasks to terminate
                if (!managerPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    managerPool.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!managerPool.awaitTermination(60, TimeUnit.SECONDS)) {
                        System.err.println("Pool did not terminate");
                    }
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                managerPool.shutdownNow();
                Flogger.atInfo().withCause(ie).log("ER-WSM-0001");
            } catch (NullPointerException npw) {
                Flogger.atInfo().withCause(npw).log("ER-WSM-0002");
            } catch (Exception e) {
                Flogger.atInfo().withCause(e).log("ER-WSM-0000");
            }
        }
    }

    protected void initializeChildProcesses() {
        heartbeatDaemon    = new HeartbeatDaemon(this);
        commandReceiver    = new CommandReceiver(this);
        commandTransmitter = new CommandTransmitter(this);
        managerPool.submit(commandTransmitter);
        managerPool.submit(commandReceiver);
        managerPool.submit(heartbeatDaemon);
    }

    protected void deactivateChildProcesses() {
        if (heartbeatDaemon != null)
            heartbeatDaemon.setActive(false);
        if (commandReceiver != null)
            commandReceiver.setActive(false);
        if (commandTransmitter != null)
            commandTransmitter.setActive(false);
    }


    @Override
    public synchronized void updateHeartbeatDaemonTime() {
        heartbeatDaemon.updateHeartBeatTime();
    }

    @Override
    public abstract void startSocketManager();

    @Override
    public abstract void stopSocketManager();

}
