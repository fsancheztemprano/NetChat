package chat.core;

import chat.model.AppPacket;
import chat.model.IServerStatusListener;
import chat.model.ISocketManager;
import chat.model.ProtocolSignal;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import tools.log.Flogger;

class ClientSocketManager implements ISocketManager {

    private String hostname = Globals.DEFAULT_SERVER_HOSTNAME;
    private int port = Globals.DEFAULT_SERVER_PORT;

    private Socket clientSocket = null;
    private ExecutorService clientManagerPool;

    private ClientCommandProcessor clientCommandProcessor;

    private HeartbeatDaemon heartbeatDaemon;
    private CommandReceiver commandReceiver;
    private CommandTransmitter commandTransmitter;

    private BlockingQueue<AppPacket> outboundCommandQueue;
    private BlockingQueue<AppPacket> inboundCommandQueue;

    private AtomicBoolean active = new AtomicBoolean(false);


    public ClientSocketManager() {

    }

    @Override
    public synchronized boolean isManagerAlive() {
        return isActive() && clientSocket != null && !clientSocket.isClosed();
    }

    @Override
    public synchronized void startSocketManager() {
        clientSocket = new Socket();
        InetSocketAddress addr = new InetSocketAddress(hostname, port);
        log("Conectando: " + addr.getAddress());
        try {
            clientSocket.connect(addr, Globals.CLIENT_CONNECT_TIMEOUT);

            log("Conectado: " + addr.getAddress());
            setActive(true);
            notifyClientStatus(true);

            inboundCommandQueue  = new ArrayBlockingQueue<>(Byte.MAX_VALUE);
            outboundCommandQueue = new ArrayBlockingQueue<>(Byte.MAX_VALUE);
            clientManagerPool    = Executors.newFixedThreadPool(4);

            clientCommandProcessor = new ClientCommandProcessor(this);

            heartbeatDaemon    = new HeartbeatDaemon(this);
            commandReceiver    = new CommandReceiver(this);
            commandTransmitter = new CommandTransmitter(this);

            clientManagerPool.submit(clientCommandProcessor);
            clientManagerPool.submit(commandTransmitter);
            clientManagerPool.submit(commandReceiver);
            clientManagerPool.submit(heartbeatDaemon);
        } catch (ConnectException ce) {
            Flogger.atInfo().withCause(ce).log("ER-CSM-0009");
        } catch (IOException ioe) {
            Flogger.atInfo().withCause(ioe).log("ER-CSM-0009");
        } catch (Exception e) {
            Flogger.atInfo().withCause(e).log("ER-CSM-0010");
        }

    }

    @Override
    public synchronized void stopSocketManager() {
        if (isActive()) {
            try {
                if (heartbeatDaemon != null)
                    commandTransmitter.setActive(false);
                if (commandReceiver != null)
                    commandReceiver.setActive(false);
                if (clientCommandProcessor != null)
                    clientCommandProcessor.setActive(false);
                if (heartbeatDaemon != null)
                    heartbeatDaemon.setActive(false);

                clientManagerPool.shutdown(); // Disable new tasks from being submitted

                // Wait a while for existing tasks to terminate
                if (!clientManagerPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    clientManagerPool.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!clientManagerPool.awaitTermination(60, TimeUnit.SECONDS)) {
                        System.err.println("Pool did not terminate");
                    }
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                clientManagerPool.shutdownNow();
                Flogger.atInfo().withCause(ie).log("ER-CSM-0001");
            } catch (NullPointerException npw) {
                Flogger.atInfo().withCause(npw).log("ER-CSM-0002");
            } catch (Exception e) {
                Flogger.atInfo().withCause(e).log("ER-CSM-0000");
            } finally {
                closeSockets();
                notifyClientStatus(isManagerAlive());
            }
        }
    }

    private void closeSockets() {
        if (clientSocket != null) {
            try {
                clientSocket.getOutputStream().close();
            } catch (IOException e) {
                Flogger.atInfo().withCause(e).log("ER-CSM-0003");
            } finally {
                try {
                    clientSocket.getInputStream().close();
                } catch (IOException e) {
                    Flogger.atInfo().withCause(e).log("ER-CSM-0004");
                } finally {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        Flogger.atInfo().withCause(e).log("ER-CSM-0005");
                    } finally {
                        setActive(false);
                    }
                }
            }
        }
    }


    @Override
    public void sendHeartbeatPacket() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void timeout() {
        stopSocketManager();
    }

    public void queueTransmission(String message) {
        AppPacket newMessage = new AppPacket(ProtocolSignal.NEW_MESSAGE, clientSocket.getLocalSocketAddress(), "cli", message);
        queueTransmission(newMessage);
    }

    @Override
    public void queueTransmission(AppPacket appPacket) {
        try {
            outboundCommandQueue.put(appPacket);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    public HeartbeatDaemon getHeartbeatDaemon() {
        return heartbeatDaemon;
    }

    private boolean isActive() {
        return active.get();
    }

    private void setActive(boolean active) {
        this.active.set(active);
    }

    @Override
    public OutputStream getOutputStream() {
        OutputStream outputStream = null;
        try {
            outputStream = clientSocket.getOutputStream();
        } catch (IOException e) {
            Flogger.atInfo().withCause(e).log("ER-CSM-0006");
        }
        return outputStream;
    }

    @Override
    public InputStream getInputStream() {
        InputStream inputStream = null;
        try {
            inputStream = clientSocket.getInputStream();
        } catch (IOException e) {
            Flogger.atInfo().withCause(e).log("ER-CSM-0007");
        }
        return inputStream;
    }


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<IServerStatusListener> listener = Optional.empty();

    public void subscribe(IServerStatusListener iServerStatusListener) {
        listener = (iServerStatusListener != null
                    ? Optional.of(iServerStatusListener)
                    : Optional.empty());
    }

    private void notifyClientStatus(boolean active) {
        listener.ifPresent(listener -> listener.onStatusChanged(active));
    }

    private void notifyLogOutput(String msg) {
        listener.ifPresent(listener -> listener.onLogOutput(msg));
    }

    private void log(String msg) {
        System.out.println(msg);
        notifyLogOutput(msg);
    }
}
