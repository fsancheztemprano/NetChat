package chat.core;

import chat.model.IServerStatusListener;

public class Server {

    private static Server instance;

    private Server() {
    }

    public static Server inst() {
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
    private String hostname;
    private int port;
    private IServerStatusListener listener = null;

    public ServerSocketManager getServerManager() {
        return serverManager;
    }

    public void startServer() {
        stopServer();
        serverManager = new ServerSocketManager();
        serverManager.setHostname(hostname);
        serverManager.setPort(port);
        serverManager.subscribe(listener);
        new Thread(serverManager).start();
    }

    public void stopServer() {
        if (serverManager != null) {
            new Thread(() -> {
                serverManager.serverShutdown();
                serverManager = null;
            }).start();
        }
    }

    public boolean isServerAlive() {
        return serverManager != null && serverManager.isActive();
    }

    public void setListener(IServerStatusListener listener) {
        this.listener = listener;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }


}
