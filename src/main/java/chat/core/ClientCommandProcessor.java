package chat.core;

import chat.model.ActivableThread;
import chat.model.AppPacket;
import java.util.concurrent.BlockingQueue;

public class ClientCommandProcessor extends ActivableThread {

    private BlockingQueue<AppPacket> clientCommandQueue;

    public ClientCommandProcessor(BlockingQueue<AppPacket> clientCommandQueue) {
        this.clientCommandQueue = clientCommandQueue;
    }

    @Override
    public void run() {
        setActive(true);
        while (isActive()) {
            try {
                AppPacket appPacket = this.clientCommandQueue.take();
                processCommand(appPacket);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processCommand(AppPacket appPacket) {
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