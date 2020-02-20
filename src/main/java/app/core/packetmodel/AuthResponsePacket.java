package app.core.packetmodel;

public class AuthResponsePacket extends AppPacket {

    public AuthResponsePacket(long sessionId) {
        super(ProtocolSignal.AUTH_RESPONSE);
        setAuth(sessionId);
    }
}
