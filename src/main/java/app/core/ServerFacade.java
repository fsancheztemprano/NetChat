package app.core;

import app.chat.ChatService;

public class ServerFacade {

    private static ServerFacade instance;

    private ServerFacade() {
    }

    public static ServerFacade inst() {
        if (instance == null) {
            synchronized (ServerFacade.class) {
                if (instance == null) {
                    instance = new ServerFacade();
                }
            }
        }
        return instance;
    }

    private ServerSocketManager serverManager;
    private String hostname;
    private int port;
    private Object listener = null;

    public ServerSocketManager getServerManager() {
        return serverManager;
    }

    public void startServer() {
        stopServer();
        serverManager = new ServerSocketManager();
        serverManager.setHostname(hostname);
        serverManager.setPort(port);
        serverManager.register(ChatService.getInstance());
        serverManager.register(listener);
        new Thread(serverManager).start();
    }

    public void stopServer() {
        if (serverManager != null) {
            new Thread(() -> {
                serverManager.serverShutdown();
                serverManager.unregister(ChatService.getInstance());
                serverManager.unregister(listener);
                serverManager = null;
            }).start();
        }
    }

    public boolean isServerAlive() {
        return serverManager != null && serverManager.isActive();
    }

    public void setListener(Object listener) {
        this.listener = listener;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }


}