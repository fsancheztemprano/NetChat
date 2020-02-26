package app.core.packetmodel;

public class AuthRemoveEvent extends AppPacket {

    public AuthRemoveEvent() {
        super(ProtocolSignal.AUTH_REMOVE);
    }
}
