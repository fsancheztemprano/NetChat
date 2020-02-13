package chat.core;

import chat.model.ActivableThread;
import chat.model.AppPacket;
import chat.model.IHeartbeatDaemon;
import chat.model.ISocketManager;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import tools.log.Flogger;

public class CommandReceiver extends ActivableThread {

    public BlockingQueue<AppPacket> inboundCommandQueue;

    private InputStream inputStream;
    private IHeartbeatDaemon heartbeatDaemon;
    private ISocketManager socketManager;

    public CommandReceiver(ISocketManager socketManager) {
        this.socketManager       = socketManager;
        this.inboundCommandQueue = socketManager.getInboundCommandQueue();
        this.inputStream         = socketManager.getInputStream();
        this.heartbeatDaemon     = socketManager.getHeartbeatDaemon();
    }

    @Override
    public void run() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(inputStream));
            setActive(true);
            while (isActive()) {
                try {
                    AppPacket receivedMessage = (AppPacket) objectInputStream.readObject();
                    System.out.println("Received: " + receivedMessage);
                    heartbeatDaemon.updateHeartBeatTime();

                    inboundCommandQueue.put(receivedMessage);
                } catch (SocketException se) {
                    setActive(false);
                    Flogger.atWarning().withCause(se).log("ER-CR-0001");       //(inputStream closed)TODO msg:Server connection lost
                    Thread.currentThread().interrupt();
                } catch (IOException ioe) {
                    Flogger.atWarning().withCause(ioe).log("ER-CR-0002");
                } catch (ClassNotFoundException cnfe) {
                    Flogger.atWarning().withCause(cnfe).log("ER-CR-0003");
                } catch (InterruptedException ie) {
                    Flogger.atWarning().withCause(ie).log("ER-CR-0004");
                } catch (Exception e) {
                    Flogger.atWarning().withCause(e).log("ER-CR-0000");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socketManager.stopSocketManager();
        }
    }

}
