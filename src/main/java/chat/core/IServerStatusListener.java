package chat.core;

public interface IServerStatusListener extends IStatusListener {

    void onActiveClientsChange(int activeClients);
}
