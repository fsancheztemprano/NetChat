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


    private volatile ServerManager serverManager;

    public ServerManager getServerManager() {
        return serverManager;
    }

    public void startServer() {
        stopServer();
        serverManager = new ServerManager();
        serverManager.start();
    }

    public void stopServer() {
        if (serverManager != null) {
            serverManager.serverShutdown();
            serverManager.interrupt();
            serverManager = null;
        }
    }
}
