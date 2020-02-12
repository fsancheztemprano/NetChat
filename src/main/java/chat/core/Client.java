package chat.core;

import chat.model.IServerStatusListener;
import java.util.Optional;

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

    private String serverHostname = Globals.DEFAULT_SERVER_HOSTNAME;
    private int serverPort = Globals.DEFAULT_SERVER_PORT;
    private ClientThreadManager clientThreadManager;


    private void log(String msg) {
        System.out.println(msg);
        notifyLogOutput(msg);
    }


    public void connect() {
        clientThreadManager = new ClientThreadManager();
        clientThreadManager.initialize();
    }

    public void disconnect() {
        if (isConnected()) {
            clientThreadManager.terminate();
        }
        clientThreadManager = null;

    }

    public boolean isConnected() {
        return clientThreadManager != null;//TODO
    }


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<IServerStatusListener> listener = Optional.empty();

    public void subscribe(IServerStatusListener iServerStatusListener) {
        listener = (iServerStatusListener != null
                    ? Optional.of(iServerStatusListener)
                    : Optional.empty());
    }

    private void notifyClientStatus(boolean active) {
        listener.ifPresent(listener -> listener.onStatusChanged(active));
    }

    private void notifyLogOutput(String msg) {
        listener.ifPresent(listener -> listener.onLogOutput(msg));
    }

    private class ClientThreadManager {

        public void initialize() {

        }

        public void terminate() {

        }
    }
}
