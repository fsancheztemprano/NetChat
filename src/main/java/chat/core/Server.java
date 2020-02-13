package chat.core;

public class Server {

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


    private volatile ServerThread serverThread;

    public ServerThread getServerThread() {
        return serverThread;
    }

    public void startServer() {
        stopServer();
        serverThread = new ServerThread();
        serverThread.start();
    }

    public void stopServer() {
        if (serverThread != null) {
            serverThread.serverShutdown();
            serverThread.interrupt();
            serverThread = null;
        }
    }
}
