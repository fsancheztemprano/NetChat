package app.core.packetmodel;

public class AuthRequestPacket extends AppPacket {

    public AuthRequestPacket(String username, String hashedPass) {
        super(ProtocolSignal.AUTH_REQUEST);
        setUsername(username);
        setPassword(hashedPass);
        setDestiny("server");
    }
}
