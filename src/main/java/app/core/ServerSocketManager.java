package app.core;

import app.core.events.ServerActiveClientsEvent;
import app.core.events.WorkerSessionEndEvent;
import app.core.events.WorkerSessionStartEvent;
import app.core.packetmodel.AppPacket;
import app.core.packetmodel.AppPacket.ProtocolSignal;
import com.google.common.eventbus.Subscribe;
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

@SuppressWarnings("UnstableApiUsage")
public class ServerSocketManager extends AbstractSocketManager implements Runnable {

    private String hostname = Globals.DEFAULT_SERVER_HOSTNAME;
    private int port = Globals.DEFAULT_SERVER_PORT;

    private final ServerSocket serverSocket;
    private InetSocketAddress inetSocketAddress;


    private final ConcurrentHashMap<Long, WorkerNodeManager> workerList;


    public ServerSocketManager() throws IOException {
        serverSocket     = new ServerSocket();
        workerList       = new ConcurrentHashMap<>();
        commandProcessor = new ServerCommandProcessor(this);
    }

    @Override
    public void run() {
        try {
            new Thread(commandProcessor).start();
            log("Creando socket servidor. Clientes Max: " + Globals.MAX_ACTIVE_CLIENTS);
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
                workerSocketManager.register(this);
                if (workerList.size() <= Globals.MAX_ACTIVE_CLIENTS) {
                    log("Conexion aceptada: " + clientSocket.getRemoteSocketAddress());
                    workerSocketManager.startSocketManager();
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
            Flogger.atWarning().withCause(e).log("ER-SSM-0000");
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


    //Subscribe methods listening to workers
    @Subscribe
    public void sessionStarted(WorkerSessionStartEvent event) {
        workerList.put(event.getEmitter().getSessionID(), event.getEmitter());
        socketEventBus.post(new ServerActiveClientsEvent(this, workerList.size()));
    }

    @Subscribe
    public void sessionEnded(WorkerSessionEndEvent event) {
        event.getEmitter().unregister(this);
        workerList.remove(event.getEmitter().getSessionID());
        socketEventBus.post(new ServerActiveClientsEvent(this, workerList.size()));

    }

    @Subscribe
    public void outputLog(String output) {
        getSocketEventBus().post(output);
    }
}
