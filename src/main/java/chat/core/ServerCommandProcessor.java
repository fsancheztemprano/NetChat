package chat.core;

import chat.model.AppPacket;
import chat.model.IServerManager;

public class ServerCommandProcessor extends AbstractCommandProcessor {

    private IServerManager serverManager;

    public ServerCommandProcessor(IServerManager serverManager) {
        super(serverManager, serverManager.getServerCommandQueue());
        this.serverManager = serverManager;
    }

    @Override
    protected void processCommand(AppPacket appPacket) {
        if (appPacket == null || appPacket.getUsername() == null || appPacket.getOriginSocketAddress() == null)
            return;

        switch (appPacket.getSignal()) {
            case CLIENT_JOIN:
                ChatService.getInstance().userJoin(appPacket.getUsername());
                serverManager.transmitToAllClients(appPacket);
                break;
            case NEW_MESSAGE:
                ChatService.getInstance().newMessage(String.format("%s: %s",
                                                                   appPacket.getUsername(),
                                                                   appPacket.getMessage()));
                serverManager.transmitToAllClients(appPacket);
                break;
            case CLIENT_QUIT:
                ChatService.getInstance().userQuit(appPacket.getUsername());
                serverManager.transmitToAllClients(appPacket);
                break;
        }
    }

}