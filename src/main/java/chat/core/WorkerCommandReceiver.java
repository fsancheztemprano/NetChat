package chat.core;

import chat.model.ActivableThread;
import chat.model.AppPacket;
import chat.model.IHeartBeatTimeHolder;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.BlockingQueue;

public class WorkerCommandReceiver extends ActivableThread {

    public BlockingQueue<AppPacket> commandQueue;

    private InputStream inputStream;
    private IHeartBeatTimeHolder heartBeatTimeHolder;

    public WorkerCommandReceiver(BlockingQueue<AppPacket> serverCommandQueue, InputStream inputStream, IHeartBeatTimeHolder heartBeatTimeHolder) throws IOException {
        this.commandQueue        = serverCommandQueue;
        this.inputStream         = inputStream;
        this.heartBeatTimeHolder = heartBeatTimeHolder;
    }

    @Override
    public void run() {
        setActive(true);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(inputStream));
            while (isActive()) {
                try {
                    AppPacket receivedMessage = (AppPacket) objectInputStream.readObject();
                    heartBeatTimeHolder.updateHeartBeatTime();

                    commandQueue.put(receivedMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
