package app.core.packetmodel;

public class AuthRemovePacket extends AppPacket {

    public AuthRemovePacket() {
        super(ProtocolSignal.AUTH_REMOVE);
    }
}
