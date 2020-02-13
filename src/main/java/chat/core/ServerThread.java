package chat.core;

import chat.model.ActivableThread;
import chat.model.AppPacket;
import chat.model.IServerStatusListener;
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

class ServerThread extends ActivableThread {

    private String hostname = Globals.DEFAULT_SERVER_HOSTNAME;
    private int port = Globals.DEFAULT_SERVER_PORT;
    private int activeClients = 0;

    private ServerSocket serverSocket = null;
    private InetSocketAddress inetSocketAddress;

    private BlockingQueue<WorkerManager> workerList;

    private BlockingQueue<AppPacket> serverCommandQueue;
    private ServerCommandProcessor serverCommandProcessor;

    public ServerThread() {
        workerList             = new ArrayBlockingQueue<>(Globals.MAX_ACTIVE_CLIENTS);
        serverCommandQueue     = new ArrayBlockingQueue<>(Byte.MAX_VALUE);
        serverCommandProcessor = new ServerCommandProcessor(serverCommandQueue, workerList);
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    @Override
    public void run() {
        try {
            log("Creando socket servidor. Clientes Max: " + Globals.MAX_ACTIVE_CLIENTS);
            serverSocket      = new ServerSocket();
            inetSocketAddress = (hostname.length() > 6 && InetAddress.getByName(hostname).isReachable(100))
                                ? new InetSocketAddress(hostname, port)
                                : new InetSocketAddress(InetAddress.getLocalHost(), port);

            log("Realizando el bind: " + inetSocketAddress.getAddress() + ":" + inetSocketAddress.getPort());
            serverSocket.bind(inetSocketAddress);
            serverCommandProcessor.start();
            setActive(true);
            notifyServerStatus(active);
            notifyActiveClients(activeClients);

            log("Aceptando conexiones: " + serverSocket.getLocalSocketAddress().toString());
            while (isActive()) {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setKeepAlive(true);
                log("Conexion entrante: " + clientSocket.getRemoteSocketAddress());
                WorkerManager workerManager = new WorkerManager(clientSocket, serverCommandQueue, workerList);
                if (workerList.add(workerManager))
                    workerManager.startWorker();
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
            e.printStackTrace();
        } finally {
            killServer();
        }
    }

    public boolean isServerAlive() {
        return serverSocket != null && serverSocket.isBound();
    }

    private synchronized void killServer() {
        killServerSocket();
        killAllClients();
        killCommandProcessor();

        listener               = Optional.empty();
        serverSocket           = null;
        inetSocketAddress      = null;
        workerList             = null;
        serverCommandQueue     = null;
        serverCommandProcessor = null;
    }

    private void killAllClients() {
        workerList.forEach(WorkerManager::stopWorker);
        workerList.clear();
    }


    private void killServerSocket() {
        log("Cerrando el socket servidor");
        try {
            if (serverSocket != null) {
                serverSocket.close();
                log("Socket Cerrado");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            setActive(false);
            notifyServerStatus(isServerAlive());
            log("Server Thread Terminado");
        }
    }

    private void killCommandProcessor() {
        try {
            if (serverCommandProcessor != null)
                serverCommandProcessor.interrupt();
            if (serverCommandQueue != null)
                serverCommandQueue.clear();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            serverCommandProcessor = null;
        }
    }

    public void serverShutdown() {
        setActive(false);
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            log("Server shutdown :" + (isServerAlive() ? "fail" : "success"));
        }
    }

    private void log(String msg) {
        System.out.println(msg);
        notifyLogOutput(msg);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<IServerStatusListener> listener = Optional.empty();

    public void subscribe(IServerStatusListener iServerStatusListener) {
        listener = (iServerStatusListener != null
                    ? Optional.of(iServerStatusListener)
                    : Optional.empty());
    }

    private void notifyLogOutput(String msg) {
        listener.ifPresent(listener -> listener.onLogOutput(msg));
    }

    private void notifyServerStatus(boolean active) {
        listener.ifPresent(listener -> listener.onStatusChanged(active));
    }

    private void notifyActiveClients(int activeClients) {
        listener.ifPresent(listener -> listener.onActiveClientsChange(activeClients));
    }


}
