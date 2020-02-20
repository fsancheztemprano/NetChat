package app.core;

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

    private ClientSocketManager clientSocketManager = null;
    private String hostname;
    private int port;
    private Object listener = null;


    public void connect() {
        if (clientSocketManager != null)
            disconnect();
        clientSocketManager = new ClientSocketManager();
        clientSocketManager.setHostname(hostname);
        clientSocketManager.setPort(port);
        clientSocketManager.getSocketEventBus().register(listener);
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

    public void setListener(Object listener) {
        this.listener = listener;
    }

    public void login(String username, String password) {
        clientSocketManager.sendLoginRequest(username, password);
    }
}
