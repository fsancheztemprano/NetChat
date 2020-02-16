package chat.model;

public class ActivableNotifierServer extends ActivableNotifier {

    void notifyActiveClientsChange(int activeClients) {
        if (listener instanceof IServerStatusListener) {
            ((IServerStatusListener) listener).onActiveClientsChange(activeClients);
        }
    }
}
