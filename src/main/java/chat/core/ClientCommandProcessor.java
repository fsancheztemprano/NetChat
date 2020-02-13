package chat.core;

import chat.model.AppPacket;

public class ClientCommandProcessor extends AbstractCommandProcessor {


    public ClientCommandProcessor(ClientSocketManager clientManager) {
        super(clientManager, clientManager.getInboundCommandQueue());
    }

    @Override
    protected void processCommand(AppPacket appPacket) {
        if (appPacket == null || appPacket.getUsername() == null || appPacket.getOriginSocketAddress() == null)
            return;

        switch (appPacket.getSignal()) {
            case CLIENT_JOIN:
                ChatService.getInstance().userJoin(appPacket.getUsername());
                break;
            case NEW_MESSAGE:
                ChatService.getInstance().newMessage(String.format("%s: %s",
                                                                   appPacket.getUsername(),
                                                                   appPacket.getMessage()));
                break;
            case CLIENT_QUIT:
                ChatService.getInstance().userQuit(appPacket.getUsername());
                break;
        }
    }
}