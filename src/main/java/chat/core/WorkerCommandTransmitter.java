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

    private BlockingQueue<AppPacket> workerOutBoundCommandQueue;
    private ObjectOutputStream outputStream;
    private IHeartBeatTimeHolder heartBeatTimeHolder;

    public WorkerCommandTransmitter(BlockingQueue<AppPacket> workerOutBoundCommandQueue, OutputStream outputStream, IHeartBeatTimeHolder heartBeatTimeHolder) throws IOException {
        this.workerOutBoundCommandQueue = workerOutBoundCommandQueue;
        this.outputStream               = new ObjectOutputStream(new BufferedOutputStream(outputStream));
        this.heartBeatTimeHolder        = heartBeatTimeHolder;
    }

    @Override
    public void run() {
        setActive(true);
        while (isActive()) {
            try {
                AppPacket appPacket = this.workerOutBoundCommandQueue.take();
                outputStream.writeObject(appPacket);
                outputStream.flush();
                heartBeatTimeHolder.updateHeartBeatTime();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
