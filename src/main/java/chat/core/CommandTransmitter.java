package chat.core;

import chat.model.ActivableThread;
import chat.model.AppPacket;
import chat.model.IHeartBeatDaemon;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

public class CommandTransmitter extends ActivableThread {

    private BlockingQueue<AppPacket> outboundCommandQueue;
    private OutputStream outputStream;
    private IHeartBeatDaemon heartBeatTimeHolder;

    public CommandTransmitter(BlockingQueue<AppPacket> outboundCommandQueue, OutputStream outputStream, IHeartBeatDaemon heartBeatManager) {
        this.outboundCommandQueue = outboundCommandQueue;
        this.outputStream         = outputStream;
        this.heartBeatTimeHolder  = heartBeatManager;
    }

    @Override
    public void run() {
        setActive(true);
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(outputStream));

            while (isActive()) {
                try {
                    AppPacket appPacket = this.outboundCommandQueue.take();
                    objectOutputStream.writeObject(appPacket);
                    objectOutputStream.flush();
                    heartBeatTimeHolder.updateHeartBeatTime();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
