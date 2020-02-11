package chat;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {

    public static String DEFAULT_SERVER_HOSTNAME = "localhost";
    public static int DEFAULT_SERVER_PORT = 5555;

    private static Server instance;

    private Server() {
    }

    public static Server getInstance() {
        if (instance == null) {
            synchronized (Server.class) {
                if (instance == null) {
                    instance = new Server();
                }
            }
        }
        return instance;
    }


    private int maxActiveClients = 4;
    //        private int maxActiveClients = Runtime.getRuntime().availableProcessors();
    private String hostname = DEFAULT_SERVER_HOSTNAME;
    private int port = DEFAULT_SERVER_PORT;
    private ServerThread serverThread;

    public int getMaxActiveClients() {
        return maxActiveClients;
    }

    public void setMaxActiveClients(int maxActiveClients) {
        this.maxActiveClients = maxActiveClients;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ServerThread getServerThread() {
        return serverThread;
    }

    public void setServerThread(ServerThread serverThread) {
        this.serverThread = serverThread;
    }

    private void log(String msg) {
        System.out.println(msg);
        notifyLogOutput(msg);
    }

    public void startServer() {
        stopServer();
        serverThread = new ServerThread();
        serverThread.start();
    }

    public void stopServer() {
        if (serverThread != null) {
            try {
                serverThread.getServerSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                serverThread = null;
            }
        }
        notifyServerStatus(isServerAlive());
    }

    public boolean isServerAlive() {
        return getServerThread() != null && getServerThread().getServerSocket() != null && getServerThread().getServerSocket().isBound();
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

    private class ServerThread extends Thread {

        private boolean active = false;
        private int activeClients = 0;
        private ServerSocket serverSocket = null;
        private InetSocketAddress inetSocketAddress;
        private ExecutorService pool = Executors.newFixedThreadPool(maxActiveClients);

        @Override
        public void run() {
            try {
                log("Creando socket servidor. Clientes Max: " + maxActiveClients);
                serverSocket      = new ServerSocket();
                inetSocketAddress = (hostname.length() > 6 && InetAddress.getByName(hostname).isReachable(100))
                                    ? new InetSocketAddress(hostname, port)
                                    : new InetSocketAddress(InetAddress.getLocalHost(), port);

                log("Realizando el bind: " + inetSocketAddress.getAddress() + ":" + inetSocketAddress.getPort());
                serverSocket.bind(inetSocketAddress);
                setActive(true);
                notifyServerStatus(active);
                notifyActiveClients(activeClients);

                log("Aceptando conexiones: " + serverSocket.getLocalSocketAddress().toString());
                while (isActive()) {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setKeepAlive(true);
//                    WorkerThread workerThread = new WorkerThread(clientSocket);
//                    pool.execute(workerThread);
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
                e.printStackTrace();
            } finally {
                killServerThread();
            }
        }

        public synchronized void killServerThread() {
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
            pool.shutdown(); // Disable new tasks from being submitted
            try {
                // Wait a while for existing tasks to terminate
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    pool.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                        System.err.println("Pool did not terminate");
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                pool.shutdownNow();
            } finally {
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public ServerSocket getServerSocket() {
            return serverSocket;
        }
    }


}
