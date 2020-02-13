package chat.core;

import chat.model.IServerStatusListener;
import java.net.Socket;
import java.util.Optional;

class ClientThread {

    private String hostname = Globals.DEFAULT_SERVER_HOSTNAME;
    private int port = Globals.DEFAULT_SERVER_PORT;
    private Socket clientSocket = null;


    public boolean isConnected() {
        return false;//TODO
    }

    public void disconnect() {

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
}
