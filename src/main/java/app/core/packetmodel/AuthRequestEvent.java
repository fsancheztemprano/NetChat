package app.core.packetmodel;

public class AuthRequestEvent extends AppPacket {

    public AuthRequestEvent(String username, String hashedPass) {
        super(ProtocolSignal.AUTH_REQUEST);
        setUsername(username);
        setPassword(hashedPass);
        setDestiny("server");
    }
}
