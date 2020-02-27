package app.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import tools.log.Flogger;

public abstract class AbstractNodeManager extends ActivableSocketManager {

    protected final Socket managedSocket;
    protected final ExecutorService managerPool;

    protected final HeartbeatDaemon heartbeatDaemon;
    protected final CommandReceiver commandReceiver;
    protected final CommandTransmitter commandTransmitter;
    protected AbstractCommandProcessor commandProcessor;

    protected InputStream inputStream;
    protected OutputStream outputStream;

    public AbstractNodeManager(Socket managedSocket) {
        this.managedSocket      = managedSocket;
        this.managerPool        = Executors.newFixedThreadPool(4);
        this.heartbeatDaemon    = new HeartbeatDaemon(this);
        this.commandReceiver    = new CommandReceiver(this);
        this.commandTransmitter = new CommandTransmitter(this);
    }

    public Socket getManagedSocket() {
        return managedSocket;
    }

    public ExecutorService getManagerPool() {
        return managerPool;
    }

    public AbstractCommandProcessor getCommandProcessor() {
        return commandProcessor;
    }

    public HeartbeatDaemon getHeartbeatDaemon() {
        return heartbeatDaemon;
    }

    public CommandReceiver getCommandReceiver() {
        return commandReceiver;
    }

    public CommandTransmitter getCommandTransmitter() {
        return commandTransmitter;
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

    public synchronized boolean queueTransmission(@Nonnull AppPacket appPacket) {
        return commandTransmitter.queueCommandTransmission(appPacket);
    }

    public boolean isSocketOpen() {
        return managedSocket != null && !managedSocket.isClosed();
    }

    public void closeSocket() {
        try {
            if (managedSocket != null)
                managedSocket.close();
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
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
        if (commandProcessor != null)
            commandProcessor.setActive(false);
    }


    public void updateHeartbeatDaemonTime() { //TODO change to EventBus
        heartbeatDaemon.updateHeartBeatTime();
    }

    public abstract void startSocketManager();

    public abstract void stopSocketManager();

}
