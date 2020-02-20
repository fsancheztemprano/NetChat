package app.core.packetmodel;

public class HeartbeatPacket extends AppPacket {

    public HeartbeatPacket() {
        super(ProtocolSignal.HEARTBEAT);
    }
}
