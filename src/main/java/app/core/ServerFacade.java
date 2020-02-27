package app.core;

import app.Globals;
import app.chat.ChatService;
import java.io.IOException;
import tools.log.Flogger;

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
    private String hostname = Globals.DEFAULT_SERVER_HOSTNAME;
    private int port = Globals.DEFAULT_SERVER_PORT;
    private Object listener = null;

    public ServerSocketManager getServerManager() {
        return serverManager;
    }

    public void startServer() {
        stopServer();
        try {
            serverManager = new ServerSocketManager();
            serverManager.setHostname(hostname);
            serverManager.setPort(port);
            if (listener != null)
                serverManager.register(listener);
            new Thread(serverManager).start();
        } catch (IOException e) {
            Flogger.atWarning().withCause(e).log("ER-SF-0001");
            stopServer();
        }
    }

    public void stopServer() {
        if (serverManager != null) {
            new Thread(() -> {
                serverManager.serverShutdown();
                serverManager.unregister(ChatService.getInstance());
                if (listener != null)
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
