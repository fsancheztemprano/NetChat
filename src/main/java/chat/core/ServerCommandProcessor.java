package chat.core;

import chat.model.ActivableThread;
import chat.model.ChatPacket;
import java.util.concurrent.BlockingQueue;

public class ServerCommandProcessor extends ActivableThread {

    private BlockingQueue<ChatPacket> serverCommandQueue;

    public ServerCommandProcessor(BlockingQueue<ChatPacket> serverCommandQueue) {
        this.serverCommandQueue = serverCommandQueue;
    }

    @Override
    public void run() {
        setActive(true);
        while (isActive()) {
            try {
                ChatPacket chatPacket = this.serverCommandQueue.take();
                processMessage(chatPacket);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void processMessage(ChatPacket chatPacket) {
        if (chatPacket == null || chatPacket.getUsername() == null || chatPacket.getOriginSocketAddress() == null)
            return;

        switch (chatPacket.getSignal()) {
            case CLIENT_JOIN:
                ChatService.getInstance().userJoin(chatPacket.getUsername());

                break;
            case NEW_MESSAGE:
                ChatService.getInstance().newMessage(String.format("%s: %s",
                                                                   chatPacket.getUsername(),
                                                                   chatPacket.getMessage()));

                break;
            case CLIENT_QUIT:
                ChatService.getInstance().userQuit(chatPacket.getUsername());

                break;
            case HEARTBEAT:

                break;

        }
    }
}