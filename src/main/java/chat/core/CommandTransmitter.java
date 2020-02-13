package chat.core;

import chat.model.ActivableThread;
import chat.model.AppPacket;
import chat.model.IHeartbeatDaemon;
import chat.model.ISocketManager;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import tools.log.Flogger;

public class CommandTransmitter extends ActivableThread {

    private ISocketManager socketManager;
    private BlockingQueue<AppPacket> outboundCommandQueue;
    private OutputStream outputStream;
    private IHeartbeatDaemon heartbeatDaemon;

    public CommandTransmitter(ISocketManager socketManager) {
        this.socketManager        = socketManager;
        this.outboundCommandQueue = socketManager.getOutboundCommandQueue();
        this.outputStream         = socketManager.getOutputStream();
        this.heartbeatDaemon      = socketManager.getHeartbeatDaemon();
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(outputStream));
            setActive(true);
            AppPacket appPacket = null;
            while (isActive()) {
                try {
                    if (!socketManager.isManagerAlive())
                        throw new SocketException();
                    appPacket = this.outboundCommandQueue.poll(Globals.TRANSMITTER_THREAD_TIMEOUT, TimeUnit.SECONDS);
                    if (appPacket != null) {
                        objectOutputStream.writeObject(appPacket);
                        objectOutputStream.flush();
                        heartbeatDaemon.updateHeartBeatTime();
                        System.out.println("Transmitted: " + appPacket);
                    }
                } catch (SocketException se) {
                    Flogger.atWarning().withCause(se).log("ER-CT-0001");       //(outputStream closed) TODO msg:Server connection lost
                    setActive(false);
                } catch (InterruptedException ie) {
                    Flogger.atWarning().withCause(ie).log("ER-CT-0002");
                } catch (IOException ioe) {
                    Flogger.atWarning().withCause(ioe).log("ER-CT-0003");
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
