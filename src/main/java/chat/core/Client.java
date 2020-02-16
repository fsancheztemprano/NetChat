package chat.core;

import chat.model.IClientStatusListener;

public class Client {

    private static Client instance;

    private Client() {
    }

    public static Client inst() {
        if (instance == null) {
            synchronized (Client.class) {
                if (instance == null) {
                    instance = new Client();
                }
            }
        }
        return instance;
    }

    private ClientSocketManager clientSocketManager = null;
    private String hostname;
    private int port;
    private IClientStatusListener listener = null;


    public void connect() {
        if (clientSocketManager != null)
            disconnect();
        clientSocketManager = new ClientSocketManager();
        clientSocketManager.setHostname(hostname);
        clientSocketManager.setPort(port);
        clientSocketManager.subscribe(listener);
        new Thread(() -> clientSocketManager.startSocketManager()).start();
    }

    public void disconnect() {
        if (clientSocketManager != null) {
            ClientSocketManager c = clientSocketManager;
            new Thread(c::stopSocketManager).start();
        }
        clientSocketManager = null;
    }


    public boolean isConnected() {
        return clientSocketManager != null && clientSocketManager.isActive();
    }

    public void sendMessage(String username, String message) {
        new Thread(() -> clientSocketManager.queueTransmission(username, message)).start();
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setListener(IClientStatusListener listener) {
        this.listener = listener;
    }
}
