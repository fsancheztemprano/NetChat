package chat.core;

import chat.model.ActivableThread;
import chat.model.AppPacket;
import java.util.concurrent.BlockingQueue;

public class ServerCommandProcessor extends ActivableThread {

    private BlockingQueue<AppPacket> serverCommandQueue;
    private BlockingQueue<WorkerManager> workerList;

    public ServerCommandProcessor(BlockingQueue<AppPacket> serverCommandQueue, BlockingQueue<WorkerManager> workerList) {
        this.serverCommandQueue = serverCommandQueue;
        this.workerList         = workerList;
    }

    @Override
    public void run() {
        setActive(true);
        while (isActive()) {
            try {
                AppPacket appPacket = this.serverCommandQueue.take();
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
                forwardToAll(appPacket);
                break;
            case NEW_MESSAGE:
                ChatService.getInstance().newMessage(String.format("%s: %s",
                                                                   appPacket.getUsername(),
                                                                   appPacket.getMessage()));
                forwardToAll(appPacket);
                break;
            case CLIENT_QUIT:
                ChatService.getInstance().userQuit(appPacket.getUsername());
                forwardToAll(appPacket);
                break;
        }
    }

    private void forwardToAll(AppPacket appPacket) {
        workerList.forEach(worker -> worker.queueTransmission(appPacket));
    }
}