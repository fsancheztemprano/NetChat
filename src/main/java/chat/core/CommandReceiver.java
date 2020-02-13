package chat.core;

import chat.model.ActivableThread;
import chat.model.AppPacket;
import chat.model.IHeartBeatDaemon;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.BlockingQueue;

public class CommandReceiver extends ActivableThread {

    public BlockingQueue<AppPacket> inboundCommandQueue;

    private InputStream inputStream;
    private IHeartBeatDaemon heartBeatTimeHolder;

    public CommandReceiver(BlockingQueue<AppPacket> inboundCommandQueue, InputStream inputStream, IHeartBeatDaemon heartBeatManager) {
        this.inboundCommandQueue = inboundCommandQueue;
        this.inputStream         = inputStream;
        this.heartBeatTimeHolder = heartBeatManager;
    }

    @Override
    public void run() {
        setActive(true);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            while (isActive()) {
                try {
                    AppPacket receivedMessage = (AppPacket) objectInputStream.readObject();
                    System.out.println("Recieved: " + receivedMessage);
                    heartBeatTimeHolder.updateHeartBeatTime();

                    inboundCommandQueue.put(receivedMessage);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (ClassNotFoundException cnfe) {
                    cnfe.printStackTrace();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
