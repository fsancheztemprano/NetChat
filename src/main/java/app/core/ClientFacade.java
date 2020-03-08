package app.core;

import app.Globals;

public class ClientFacade {

    private static ClientFacade instance;

    private ClientFacade() {
    }

    public static ClientFacade inst() {
        if (instance == null) {
            synchronized (ClientFacade.class) {
                if (instance == null) {
                    instance = new ClientFacade();
                }
            }
        }
        return instance;
    }

    private ClientNodeManager clientSocketManager = null;
    private String hostname = Globals.DEFAULT_SERVER_HOSTNAME;
    private int port = Globals.DEFAULT_SERVER_PORT;
    private Object listener = null;


    public void connect() {
        if (clientSocketManager != null)
            disconnect();
        clientSocketManager = new ClientNodeManager();
        clientSocketManager.setHostname(hostname);
        clientSocketManager.setPort(port);
        if (listener != null)
            clientSocketManager.register(listener);
        new Thread(() -> clientSocketManager.startSocketManager()).start();
    }

    public void disconnect() {
        if (clientSocketManager != null) {
            ClientNodeManager c = clientSocketManager;
            new Thread(() -> {
                c.stopSocketManager();
                if (listener != null)
                    c.unregister(listener);
            }).start();
        }
        clientSocketManager = null;
    }


    public boolean isConnected() {
        return clientSocketManager != null && clientSocketManager.isActive();
    }

//    public void sendMessage(String username, String message) {
//        new Thread(() -> clientSocketManager.queueTransmission(username, message)).start();
//    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setListener(Object listener) {
        this.listener = listener;
    }

    public void login(String username, String password) {
        clientSocketManager.sendLoginRequest(username, password);
    }

    public void logout() {
        clientSocketManager.sendLogOutAction();
    }

    public void requestUserList() {
        clientSocketManager.sendUserListRequest();
    }

    public void requestGroupList() {
        clientSocketManager.sendGroupListRequest();
    }

    public void sendPM(String username, String draft) {
        clientSocketManager.sendPM(username, draft);
    }

    public void requestNewGroup(String newGroupName) {
        clientSocketManager.requestNewGroup(newGroupName);
    }
}
