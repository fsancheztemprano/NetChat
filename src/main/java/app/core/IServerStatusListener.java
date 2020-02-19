package app.core;

public interface IServerStatusListener extends IStatusListener {

    void onActiveClientsChange(int activeClients);
}
