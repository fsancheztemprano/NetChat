package chat.model;

public interface IServerStatusListener extends IStatusListener {

    void onActiveClientsChange(int activeClients);
}
