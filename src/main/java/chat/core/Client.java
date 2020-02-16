package chat.core;

import chat.model.IServerStatusListener;

public class Client {

    private static Client instance;

    private Client() {
    }

    public static Client getInstance() {
        if (instance == null) {
            synchronized (Client.class) {
                if (instance == null) {
                    instance = new Client();
                }
            }
        }
        return instance;
    }

    private volatile ClientSocketManager clientSocketManager = null;

    public void connect() {
        disconnect();
        clientSocketManager = new ClientSocketManager();
        new Thread(() -> clientSocketManager.startSocketManager()).start();
    }

    public void disconnect() {
        if (clientSocketManager != null) {
            new Thread(() -> clientSocketManager.stopSocketManager()).start();
            clientSocketManager = null;
        }
    }


    public boolean isConnected() {
        return clientSocketManager != null && clientSocketManager.isActive();
    }

    public void subscribe(IServerStatusListener serverStatusListener) {
        clientSocketManager.subscribe(serverStatusListener);
    }

    public void sendMessage(String username, String message) {
        new Thread(() -> clientSocketManager.queueTransmission(username, message)).start();
    }
}
