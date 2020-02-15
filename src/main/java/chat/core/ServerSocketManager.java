package chat.core;

import chat.model.ActivableNotifier;
import chat.model.AppPacket;
import chat.model.IServerSocketManager;
import chat.model.ProtocolSignal;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import tools.log.Flogger;

public class ServerSocketManager extends ActivableNotifier implements IServerSocketManager, Runnable {

    private String hostname = Globals.DEFAULT_SERVER_HOSTNAME;
    private int port = Globals.DEFAULT_SERVER_PORT;
    private int activeClients = 0;

    private ServerSocket serverSocket = null;
    private InetSocketAddress inetSocketAddress;

    private BlockingQueue<WorkerSocketManager> workerList;

    private BlockingQueue<AppPacket> serverCommandQueue;
    private ServerCommandProcessor serverCommandProcessor;

    @Override
    public void run() {
        try {
            workerList             = new ArrayBlockingQueue<>(Globals.MAX_ACTIVE_CLIENTS);
            serverCommandQueue     = new ArrayBlockingQueue<>(Byte.MAX_VALUE);
            serverCommandProcessor = new ServerCommandProcessor(this);

            log("Creando socket servidor. Clientes Max: " + Globals.MAX_ACTIVE_CLIENTS);
            serverSocket      = new ServerSocket();
            inetSocketAddress = (hostname.length() > 6 && InetAddress.getByName(hostname).isReachable(100))
                                ? new InetSocketAddress(hostname, port)
                                : new InetSocketAddress(InetAddress.getLocalHost(), port);

            log("Realizando el bind: " + inetSocketAddress.getAddress() + ":" + inetSocketAddress.getPort());
            serverSocket.bind(inetSocketAddress);
            new Thread(serverCommandProcessor).start();
            setActive(true);
            log("Aceptando conexiones: " + serverSocket.getLocalSocketAddress().toString());
            while (isActive()) {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setKeepAlive(true);
                log("Conexion entrante: " + clientSocket.getRemoteSocketAddress());
                WorkerSocketManager workerSocketManager = new WorkerSocketManager(this, clientSocket);
                if (!workerList.contains(workerSocketManager) && workerList.add(workerSocketManager))
                    workerSocketManager.startSocketManager();
            }
        } catch (IllegalStateException ise) {
            log("Rejecting incoming con, Server Full");
        } catch (BindException be) {
            log("Unbindable Socket Address: " + inetSocketAddress);
        } catch (IllegalArgumentException iae) {
            log("Puerto Invalido");
        } catch (SocketException se) {
            log("Interrumpiendo Bind");
        } catch (UnknownHostException uhe) {
            log("Hostname invalido");
        } catch (IOException ioe) {
            log("Deteniendo Servidor");
        } catch (Exception e) {
            Flogger.atInfo().withCause(e).log("ER-SSM-0000");
        } finally {
            serverShutdown();
        }
    }

    @Override
    public synchronized void serverShutdown() {
        if (isActive() || isServerSocketBound() || isServerSocketOpen()) {
            closeServerSocket();
            stopAllClients();
            stopCommandProcessor();

            listener               = Optional.empty();
            serverSocket           = null;
            inetSocketAddress      = null;
            workerList             = null;
            serverCommandQueue     = null;
            serverCommandProcessor = null;
        }
    }

    @Override
    public BlockingQueue<AppPacket> getServerCommandQueue() {
        return serverCommandQueue;
    }

    @Override
    public BlockingQueue<WorkerSocketManager> getWorkerList() {
        return workerList;
    }

    @Override
    public void transmitToAllClients(AppPacket appPacket) {
        workerList.forEach(worker -> worker.queueTransmission(appPacket));
    }

    public boolean isServerSocketBound() {
        return serverSocket != null && serverSocket.isBound();
    }

    public boolean isServerSocketOpen() {
        return serverSocket != null && !serverSocket.isClosed();
    }

    void closeServerSocket() {
        log("Cerrando el ServerSocket");
        if (isServerSocketOpen()) {
            try {
                serverSocket.close();
                setActive(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void stopAllClients() {
        workerList.forEach(WorkerSocketManager::stopSocketManager);
        workerList.clear();
    }

    private void stopCommandProcessor() {
        try {
            if (serverCommandProcessor != null)
                serverCommandProcessor.setActive(false);
        } catch (Exception e) {
            Flogger.atInfo().withCause(e).log("ER-SSM-000x");
        }
    }

    @Override
    public void notifyActiveClients(int activeClients) {
        listener.ifPresent(listener -> listener.onActiveClientsChange(activeClients));
    }


    public void queueTransmission(String message) {
        AppPacket newMessage = new AppPacket(ProtocolSignal.NEW_MESSAGE, serverSocket.getLocalSocketAddress(), "server", message);
        transmitToAllClients(newMessage);
    }
}
