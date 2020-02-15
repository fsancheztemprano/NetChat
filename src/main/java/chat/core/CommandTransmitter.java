package chat.core;

import chat.model.Activable;
import chat.model.AppPacket;
import chat.model.ISocketManager;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import tools.log.Flogger;

public class CommandTransmitter extends Activable implements Runnable {

    private ISocketManager socketManager;
    private BlockingQueue<AppPacket> outboundCommandQueue;

    public CommandTransmitter(ISocketManager socketManager) {
        this.socketManager        = socketManager;
        this.outboundCommandQueue = socketManager.getOutboundCommandQueue();
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(socketManager.getOutputStream()));
            setActive(true);
            AppPacket appPacket = null;
            while (isActive()) {
                try {
                    if (!socketManager.isActive())
                        throw new SocketException();
                    appPacket = this.outboundCommandQueue.poll(Globals.TRANSMITTER_THREAD_TIMEOUT, TimeUnit.SECONDS);
                    if (appPacket != null) {
                        objectOutputStream.writeObject(appPacket);
                        objectOutputStream.flush();
                        socketManager.updateHeartbeatDaemonTime();
                        System.out.println("Transmitted: " + appPacket);                    //TODO log output
                    }
                } catch (SocketException se) {
                    Flogger.atWarning().withCause(se).log("ER-CT-0001");       //(outputStream closed) TODO msg:Server connection lost
                    setActive(false);
                } catch (InterruptedException ie) {
                    Flogger.atWarning().withCause(ie).log("ER-CT-0002");
                    setActive(false);
                } catch (IOException ioe) {
                    Flogger.atWarning().withCause(ioe).log("ER-CT-0003");
                    setActive(false);
                } catch (Exception e) {
                    Flogger.atWarning().withCause(e).log("ER-CT-0004");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socketManager.stopSocketManager();
        }
    }
}
