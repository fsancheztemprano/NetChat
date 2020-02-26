package app.core;

import app.core.packetmodel.AppPacket;
import app.core.packetmodel.HeartbeatPacket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import tools.log.Flogger;

public abstract class AbstractNodeManager extends AbstractSocketManager {

    protected Socket managedSocket;
    protected ExecutorService managerPool;

    protected AppPacket heartbeatPacket;
    protected HeartbeatDaemon heartbeatDaemon;
    protected CommandReceiver commandReceiver;
    protected CommandTransmitter commandTransmitter;

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
               : new HeartbeatPacket();
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


    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

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

    public synchronized void queueTransmission(@Nonnull AppPacket appPacket) {
        commandTransmitter.queueCommandTransmission(appPacket);
    }

    public void queueTransmission(String username, String message) {
//        AppPacket newMessage = new AppPacket(ProtocolSignal.NEW_MESSAGE, managedSocket.getLocalSocketAddress(), username, message);
//        queueTransmission(newMessage);
    }


    public void sendHeartbeatPacket() {
        queueTransmission(getHeartbeatPacket());
    }


    public boolean isSocketOpen() {
        return managedSocket != null && !managedSocket.isClosed();
    }

    public void closeSocket() {
        try {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (managedSocket != null)
                managedSocket.close();

        } catch (IOException e) {
            Flogger.atWarning().withCause(e).log("ER-ASM-0005");
        } catch (Exception e) {
            Flogger.atWarning().withCause(e).log("ER-ASM-0004");
        } finally {
            setActive(false);
        }
    }

    protected void closePool() {
        if (managerPool != null) {
            managerPool.shutdown(); // Disable new tasks from being submitted
            try {
                // Wait a while for existing tasks to terminate
                if (!managerPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    managerPool.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!managerPool.awaitTermination(30, TimeUnit.SECONDS)) {
                        System.err.println("Pool did not terminate");
                    }
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                managerPool.shutdownNow();
                Flogger.atWarning().withCause(ie).log("ER-ASM-0008");
            } catch (NullPointerException npw) {
                Flogger.atWarning().withCause(npw).log("ER-ASM-0007");
            } catch (Exception e) {
                Flogger.atWarning().withCause(e).log("ER-ASM-0006");
            } finally {
//                Thread.currentThread().interrupt();
            }
        }
    }

    protected void initializeChildProcesses(@Nonnull AbstractCommandProcessor commandProcessor) {
        this.commandProcessor   = commandProcessor;
        this.heartbeatDaemon    = new HeartbeatDaemon(this);
        this.commandReceiver    = new CommandReceiver(this);
        this.commandTransmitter = new CommandTransmitter(this);
    }

    protected void poolUpChildProcesses() {
        managerPool.submit(commandProcessor);
        managerPool.submit(commandTransmitter);
        managerPool.submit(commandReceiver);
        managerPool.submit(heartbeatDaemon);
    }

    protected void disableChildProcesses() {
        if (heartbeatDaemon != null)
            heartbeatDaemon.setActive(false);
        if (commandReceiver != null)
            commandReceiver.setActive(false);
        if (commandTransmitter != null)
            commandTransmitter.setActive(false);
        disableCommandProcessor();
    }


    public void updateHeartbeatDaemonTime() { //TODO change to EventBus
        heartbeatDaemon.updateHeartBeatTime();
    }

    public abstract void startSocketManager();

    public abstract void stopSocketManager();

}
