package app.core;

import app.core.packetmodel.AppPacket;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import tools.log.Flogger;

public class CommandReceiver extends Activable implements Runnable {

    private AbstractSocketManager socketManager;
    public BlockingQueue<AppPacket> inboundCommandQueue;


    public CommandReceiver(AbstractSocketManager socketManager) {
        this.socketManager       = socketManager;
        this.inboundCommandQueue = socketManager.getInboundCommandQueue();
    }

    @Override
    public void run() {
        try {
            if (socketManager.isSocketOpen()) {
                ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(socketManager.getInputStream()));
                setActive(true);
                while (isActive()) {
                    try {
                        AppPacket appPacket = (AppPacket) objectInputStream.readObject();
                        appPacket.setHandler(socketManager instanceof WorkerSocketManager
                                             ? ((WorkerSocketManager) socketManager)
                                             : null);
                        socketManager.updateHeartbeatDaemonTime();                  //TODO migrate to event bus
                        socketManager.log("In: " + appPacket.toString());    //TODO migrate to event bus
                        inboundCommandQueue.put(appPacket);
                    } catch (SocketException se) {
                        setActive(false);
                        Flogger.atWarning().withCause(se).log("ER-CR-0001");       //(inputStream closed)TODO msg:Server connection lost
                        Thread.currentThread().interrupt();
                    } catch (IOException ioe) {
                        setActive(false);
                        Flogger.atWarning().withCause(ioe).log("ER-CR-0002");
                    } catch (ClassNotFoundException cnfe) {
                        Flogger.atWarning().withCause(cnfe).log("ER-CR-0003");
                        setActive(false);
                    } catch (InterruptedException ie) {
                        Flogger.atWarning().withCause(ie).log("ER-CR-0004");
                        setActive(false);
                    } catch (Exception e) {
                        Flogger.atWarning().withCause(e).log("ER-CR-0000");
                        setActive(false);
                    }
                }
            }
        } catch (IOException e) {
            Flogger.atWarning().withCause(e).log("ER-CR-0005");
        } finally {
            socketManager.stopSocketManager();
        }
    }

}
