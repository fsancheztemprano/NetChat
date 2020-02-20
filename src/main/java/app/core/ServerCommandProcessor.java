package app.core;

import app.core.packetmodel.AppPacket;
import app.core.packetmodel.AppPacket.ProtocolSignal;
import app.core.packetmodel.AuthRequestPacket;

public class ServerCommandProcessor extends AbstractCommandProcessor {

    private ServerSocketManager serverSocketManager;

    public ServerCommandProcessor(ServerSocketManager serverSocketManager) {
        super(serverSocketManager, serverSocketManager.getServerCommandQueue());
        this.serverSocketManager = serverSocketManager;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected void processCommand(AppPacket appPacket) {
        if (appPacket == null || appPacket.getUsername() == null || appPacket.getOriginSocketAddress() == null)
            return;
        if ((appPacket.getSignal() != ProtocolSignal.AUTH_REQUEST || appPacket.getSignal() != ProtocolSignal.HEARTBEAT) && (appPacket.getAuth() == -1))
            return;  //TODO send reject message
        switch (appPacket.getSignal()) {
            case AUTH_REQUEST:
                AuthRequestPacket authRequestPacket = (AuthRequestPacket) appPacket;
                serverSocketManager.getSocketEventBus().post((authRequestPacket));
                break;
            case CLIENT_QUIT:
//                ChatService.getInstance().userQuit(appPacket.getUsername());
//                serverManager.transmitToAllClients(appPacket);
                break;
            case CLIENT_JOIN:
//                ChatService.getInstance().userJoin(appPacket.getUsername());
//                serverManager.transmitToAllClients(appPacket);
                break;
            case NEW_MESSAGE:
//                ChatService.getInstance().newMessage(String.format("%s: %s",appPacket.getUsername(),appPacket.getMessage()));
                serverSocketManager.transmitToAllClients(appPacket);
                break;
        }
    }

}