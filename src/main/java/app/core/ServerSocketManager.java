package app.core;

import app.core.packetmodel.AppPacket;
import app.core.packetmodel.AppPacket.ProtocolSignal;
import com.google.common.eventbus.EventBus;
import com.google.common.flogger.StackSize;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import tools.log.Flogger;

public class ServerSocketManager extends AbstractSocketManager implements Runnable {

    private String hostname = Globals.DEFAULT_SERVER_HOSTNAME;
    private int port = Globals.DEFAULT_SERVER_PORT;

    private ServerSocket serverSocket = null;
    private InetSocketAddress inetSocketAddress;


    private final ConcurrentHashMap<Long, WorkerNodeManager> workerList;


    public ServerSocketManager() {
        socketEventBus   = new EventBus("ServerEventBus");
        workerList       = new ConcurrentHashMap<>();
        commandProcessor = new ServerCommandProcessor(this);
    }

    @Override
    public void run() {
        try {
            new Thread(commandProcessor).start();
            log("Creando socket servidor. Clientes Max: " + Globals.MAX_ACTIVE_CLIENTS);
            serverSocket      = new ServerSocket();
            inetSocketAddress = (hostname.length() > 6 && InetAddress.getByName(hostname).isReachable(100))
                                ? new InetSocketAddress(hostname, port)
                                : new InetSocketAddress(InetAddress.getLocalHost(), port);

            log("Realizando el bind: " + inetSocketAddress.getAddress() + ":" + inetSocketAddress.getPort());
            serverSocket.bind(inetSocketAddress);
            setActive(true);
            log("Aceptando conexiones: " + serverSocket.getLocalSocketAddress().toString());
            while (isActive()) {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setKeepAlive(true);
                log("Conexion entrante: " + clientSocket.getRemoteSocketAddress());
                WorkerNodeManager workerSocketManager = new WorkerNodeManager(this, clientSocket);
                if (workerList.size() < Globals.MAX_ACTIVE_CLIENTS) {
                    workerList.put(workerSocketManager.getSessionID(), workerSocketManager);
                    log("Conexion aceptada: " + clientSocket.getRemoteSocketAddress());
                    workerSocketManager.startSocketManager();
                    socketEventBus.post(new Integer(workerList.size()));
                } else {
                    log("Conexion Rechazada, Servidor Lleno. (" + Globals.MAX_ACTIVE_CLIENTS + ")");
                    clientSocket.close();
                }
            }
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
            Flogger.atWarning().withStackTrace(StackSize.FULL).withCause(e).log("ER-SSM-0000");
        } finally {
            serverShutdown();
        }
    }

    public synchronized void serverShutdown() {
        if (isActive() || isServerSocketBound() || isServerSocketOpen()) {
            try {
                closeServerSocket();
                stopAllClients();
                disableCommandProcessor();

                serverSocket      = null;
                inetSocketAddress = null;
                commandProcessor  = null;
            } catch (Exception e) {
                Flogger.atWarning().withCause(e).log("ER-SSM-0001");
            } finally {
                setActive(false);
                Thread.currentThread().interrupt();
            }
        }
    }


    public ConcurrentHashMap<Long, WorkerNodeManager> getWorkerList() {
        return workerList;
    }

    public void removeWorker(WorkerNodeManager workerSocketManager) {
        log("Conexion finalizada: " + workerSocketManager.managedSocket.getRemoteSocketAddress());
        workerList.remove(workerSocketManager.getSessionID());
        socketEventBus.post(new Integer(workerList.size()));
    }

    public void transmitToAllClients(AppPacket appPacket) {
        workerList.forEachValue(1, workerSocketManager -> workerSocketManager.queueTransmission(appPacket));
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
                Flogger.atWarning().withCause(e).log("ER-SSM-0004");
            } catch (Exception e) {
                Flogger.atWarning().withCause(e).log("ER-SSM-0003");
            }
        }

    }

    private void stopAllClients() {
        workerList.forEachValue(1, WorkerNodeManager::stopSocketManager);
        workerList.clear();
    }



    public void queueServerBroadcast(String message) {
        AppPacket newMessage = new AppPacket(ProtocolSignal.SERVER_BROADCAST);
        newMessage.setUsername("SERVER");
        newMessage.setMessage(message);
        transmitToAllClients(newMessage);
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
