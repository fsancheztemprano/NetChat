package chat.model;

public interface IHeartBeater {

    void sendHeartbeatPacket();

    void timeout();
}
