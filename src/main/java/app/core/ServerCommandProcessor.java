package app.core;

public class ServerCommandProcessor extends AbstractCommandProcessor {

    private ServerSocketManager serverSocketManager;

    public ServerCommandProcessor(ServerSocketManager serverSocketManager) {
        super(serverSocketManager, serverSocketManager.getServerCommandQueue());
        this.serverSocketManager = serverSocketManager;
    }

    @Override
    protected void processCommand(AppPacket appPacket) {
        if (appPacket == null || appPacket.getUsername() == null || appPacket.getOriginSocketAddress() == null)
            return;
        switch (appPacket.getSignal()) {
            case CLIENT_JOIN:
//                ChatService.getInstance().userJoin(appPacket.getUsername());
//                serverManager.transmitToAllClients(appPacket);
                break;
            case NEW_MESSAGE:
//                ChatService.getInstance().newMessage(String.format("%s: %s",appPacket.getUsername(),appPacket.getMessage()));
                serverSocketManager.transmitToAllClients(appPacket);
                break;
            case CLIENT_QUIT:
//                ChatService.getInstance().userQuit(appPacket.getUsername());
//                serverManager.transmitToAllClients(appPacket);
                break;
        }
    }

}