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

    private volatile ClientManager clientManager;

    public ClientManager getClientManager() {
        return clientManager;
    }

    public void connect() {
        disconnect();
        clientManager = new ClientManager();
        clientManager.connect();
    }

    public void disconnect() {
        if (clientManager != null) {
            clientManager.disconnect();
            clientManager = null;
        }
    }

    public void sendMessage(String message) {
        clientManager.sendMessage(message);
    }


}
