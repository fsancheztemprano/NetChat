package chat.core;

import chat.model.ActivableThread;
import chat.model.AppPacket;
import chat.model.IHeartBeatTimeHolder;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

public class WorkerCommandTransmitter extends ActivableThread {

    private BlockingQueue<AppPacket> workerCommandQueue;
    private OutputStream outputStream;
    private IHeartBeatTimeHolder heartBeatTimeHolder;

    public WorkerCommandTransmitter(BlockingQueue<AppPacket> workerCommandQueue, OutputStream outputStream, IHeartBeatTimeHolder heartBeatTimeHolder) {
        this.workerCommandQueue  = workerCommandQueue;
        this.outputStream        = outputStream;
        this.heartBeatTimeHolder = heartBeatTimeHolder;
    }

    @Override
    public void run() {
        setActive(true);
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(outputStream));

            while (isActive()) {
                try {
                    AppPacket appPacket = this.workerCommandQueue.take();
                    objectOutputStream.writeObject(appPacket);
                    objectOutputStream.flush();
                    heartBeatTimeHolder.updateHeartBeatTime();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
