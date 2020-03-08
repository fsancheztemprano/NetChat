package app.core;

import app.Globals;
import app.chat.ChatService;
import app.core.AppPacket.ProtocolSignal;
import app.core.events.ServerActiveClientsEvent;
import app.core.events.WorkerLoginEvent;
import app.core.events.WorkerStatusEvent;
import com.google.common.eventbus.Subscribe;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import tools.log.Flogger;

@SuppressWarnings("UnstableApiUsage")
public class ServerSocketManager extends ActivableSocketManager implements Runnable {

    private String hostname = Globals.DEFAULT_SERVER_HOSTNAME;
    private int port = Globals.DEFAULT_SERVER_PORT;

    private final ServerSocket serverSocket;
    private InetSocketAddress inetSocketAddress;


    private final ConcurrentHashMap<Long, WorkerNodeManager> workerList;


    public ServerSocketManager() throws IOException {
        serverSocket = new ServerSocket();
        workerList   = new ConcurrentHashMap<>();
        register(ChatService.getInstance());
        ChatService.getInstance().setChatServer(this);
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
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
            } catch (Exception e) {
                Flogger.atWarning().withCause(e).log("ER-SSM-0001");
            } finally {
                setActive(false);
                ChatService.getInstance().setChatServer(null);
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean isServerSocketBound() {
        return serverSocket != null && serverSocket.isBound();
    }

    public boolean isServerSocketOpen() {
        return serverSocket != null && !serverSocket.isClosed();
    }

    private void closeServerSocket() {
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


    //Subscribe methods listening to workers
    @Subscribe
    public void workerStatusChange(WorkerStatusEvent workerStatusEvent) {
        if (workerStatusEvent.isActive()) {
            workerList.put(workerStatusEvent.getSessionID(), workerStatusEvent.getWorker());
            socketEventBus.post(new ServerActiveClientsEvent(this, workerList.size()));
        } else {
            workerStatusEvent.getWorker().unregister(this);
            workerList.remove(workerStatusEvent.getSessionID());
            socketEventBus.post(new ServerActiveClientsEvent(this, workerList.size()));
            socketEventBus.post(new WorkerLoginEvent(workerStatusEvent.getWorker()));
        }
    }

    @Subscribe
    public void outputLog(String output) {
        getSocketEventBus().post(output);
    }

    private void transmitTo(final Set<Long> sessionIDs, final AppPacket appPacket) {
        sessionIDs.forEach(sessionID -> transmitTo(sessionID, appPacket));
    }

    private void transmitTo(final long sessionID, final AppPacket appPacket) {
        new Thread(() -> {
            final WorkerNodeManager worker = workerList.get(sessionID);
            if (worker != null)
                worker.queueTransmission(appPacket);
        }).start();
    }

    public void broadcast(final AppPacket appPacket) {
        transmitTo(new HashSet<>(workerList.keySet()), appPacket);
    }

    public void broadcastMessage(String message) {
        broadcast(AppPacket.ofType(ProtocolSignal.SERVER_BROADCAST)
                           .setUsername("SERVER")
                           .setMessage(message));
    }

    public void sendLoginSuccess(final long sessionID) {
        transmitTo(sessionID, AppPacket.ofType(ProtocolSignal.SERVER_RESPONSE_LOGIN_SUCCESS)
                                       .setAuth(sessionID));
    }

    public void sendAlertMessage(long sessionID, String alertMessage) {
        transmitTo(sessionID,
                   AppPacket.ofType(ProtocolSignal.SERVER_RESPONSE_ALERT_MESSAGE)
                            .setMessage(alertMessage));
    }

    public void broadcastUserList(String[] usernameList) {
        broadcast(AppPacket.ofType(ProtocolSignal.SERVER_SEND_USER_LIST)
                           .setList(usernameList));
    }

    public void broadcastGroupList(String[] groupList) {
        broadcast(AppPacket.ofType(ProtocolSignal.SERVER_SEND_GROUP_LIST)
                           .setList(groupList));
    }

    public void sendUserList(long id, String[] usernameList) {
        transmitTo(id, AppPacket.ofType(ProtocolSignal.SERVER_SEND_GROUP_LIST)
                                .setList(usernameList));
    }

    public void sendGroupList(long id, String[] groupList) {
        transmitTo(id, AppPacket.ofType(ProtocolSignal.SERVER_SEND_GROUP_LIST)
                                .setList(groupList));
    }

    public void sendPMAck(Set<Long> originSessions, String username, String destiny, String message) {
        transmitTo(originSessions, AppPacket.ofType(ProtocolSignal.CLIENT_SENT_PM_ACK)
                                            .setUsername(username)
                                            .setDestiny(destiny)
                                            .setMessage(message));
    }

    public void sendPM(Set<Long> destinySessions, String username, String destiny, String message) {
        transmitTo(destinySessions, AppPacket.ofType(ProtocolSignal.CLIENT_SEND_PM)
                                             .setUsername(username)
                                             .setDestiny(destiny)
                                             .setMessage(message));
    }

    public void sendGroupUserList(long sessionID, String groupName, String[] groupUserList) {
        transmitTo(sessionID, AppPacket.ofType(ProtocolSignal.SERVER_SEND_GROUP_USER_LIST).setDestiny(groupName).setList(groupUserList));
    }

    public void broadcastGroupUserList(Set<Long> sessionIDs, String groupName, String[] groupUserList) {
        transmitTo(sessionIDs, AppPacket.ofType(ProtocolSignal.SERVER_SEND_GROUP_USER_LIST).setDestiny(groupName).setList(groupUserList));
    }
}
