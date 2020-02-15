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

    private static ServerSocketManager serverManager;

    public ServerSocketManager getServerManager() {
        return serverManager;
    }

    public void startServer() {
        stopServer();
        serverManager = new ServerSocketManager();
        new Thread(serverManager).start();
    }

    public void stopServer() {
        if (serverManager != null) {
            serverManager.serverShutdown();
            serverManager = null;
        }
    }

    public boolean isServerAlive() {
        return serverManager != null && serverManager.isActive();
    }
}
