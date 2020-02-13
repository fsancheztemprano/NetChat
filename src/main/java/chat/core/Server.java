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


    private volatile ServerSocketManager serverManager;

    public ServerSocketManager getServerManager() {
        return serverManager;
    }

    public void startServer() {
        stopServer();
        serverManager = new ServerSocketManager();
        serverManager.start();
    }

    public void stopServer() {
        if (serverManager != null) {
            serverManager.serverShutdown();
            serverManager.interrupt();
            serverManager = null;
        }
    }

    public boolean isServerAlive() {
        return serverManager != null && serverManager.isManagerAlive();
    }
}
