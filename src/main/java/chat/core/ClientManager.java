package chat.core;

import chat.model.AppPacket;
import chat.model.IHeartBeater;
import chat.model.IServerStatusListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ClientManager implements IHeartBeater {

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


    public ClientManager() {

    }

    public boolean isConnected() {
        return clientSocket != null && clientSocket.isBound() && clientSocket.isClosed();
    }

    public void connect() {
        clientSocket = new Socket();
        InetSocketAddress addr = new InetSocketAddress(hostname, port);
        log("Conectando: " + addr.getAddress());
        try {
            clientSocket.connect(addr, Globals.CLIENT_CONNECT_TIMEOUT);

            log("Conectado: " + addr.getAddress());
            notifyClientStatus(true);

            inboundCommandQueue  = new ArrayBlockingQueue<>(Byte.MAX_VALUE);
            outboundCommandQueue = new ArrayBlockingQueue<>(Byte.MAX_VALUE);
            clientManagerPool    = Executors.newFixedThreadPool(4);

            heartbeatDaemon    = new HeartbeatDaemon(this);
            commandReceiver    = new CommandReceiver(inboundCommandQueue, clientSocket.getInputStream(), heartbeatDaemon);
            commandTransmitter = new CommandTransmitter(outboundCommandQueue, clientSocket.getOutputStream(), heartbeatDaemon);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void disconnect() {
        heartbeatDaemon.setActive(false);
        commandTransmitter.setActive(false);
        commandReceiver.setActive(false);

        clientManagerPool.shutdown(); // Disable new tasks from being submitted
        try {
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
        } finally {
            closeSocket();
        }
        notifyClientStatus(isConnected());
    }


    public void stopWorker() {

    }

    private void closeSocket() {
        try {
            clientSocket.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.getInputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
        disconnect();
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
