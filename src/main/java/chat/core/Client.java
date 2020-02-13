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

    private volatile ClientThread clientThread;

    public ClientThread getClientThread() {
        return clientThread;
    }

    public void connect() {
        disconnect();
        clientThread = new ClientThread();
        clientThread.isConnected();
    }

    public void disconnect() {
        if (clientThread != null) {
            clientThread.disconnect();
            clientThread = null;
        }
    }





}
