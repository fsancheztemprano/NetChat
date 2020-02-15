package chat.core;

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
        clientSocketManager.startSocketManager();
    }

    public void disconnect() {
        if (clientSocketManager != null) {
            clientSocketManager.stopSocketManager();
            clientSocketManager = null;
        }
    }

    public void sendMessage(String message) {
        clientSocketManager.queueTransmission(message);
    }


    public boolean isConnected() {
        return clientSocketManager != null && clientSocketManager.isActive();
    }
}
