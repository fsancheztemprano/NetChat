package app.core;

import app.Globals;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import tools.log.Flogger;

public class CommandTransmitter extends Activable implements Runnable {

    private final BlockingQueue<AppPacket> outboundCommandQueue = new ArrayBlockingQueue<>(Byte.MAX_VALUE);

    private final AbstractNodeManager socketManager;
    private final SocketAddress localSocketAddress;

    public CommandTransmitter(AbstractNodeManager socketManager) {
        this.socketManager      = socketManager;
        this.localSocketAddress = socketManager.getManagedSocket().getLocalSocketAddress();
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
                        appPacket.setOriginSocketAddress(localSocketAddress);
                        objectOutputStream.writeObject(appPacket);
                        objectOutputStream.flush();
                        socketManager.updateHeartbeatDaemonTime();
                        socketManager.log("Out: " + appPacket.toString());
                    }
                } catch (SocketException se) {
                    Flogger.atWarning().withCause(se).log("ER-CT-0001");
                    setActive(false);
                    Thread.currentThread().interrupt();
                } catch (InterruptedException ie) {
                    Flogger.atWarning().withCause(ie).log("ER-CT-0002");
                    setActive(false);
                } catch (IOException ioe) {
                    Flogger.atWarning().withCause(ioe).log("ER-CT-0003");
                    setActive(false);
                } catch (Exception e) {
                    Flogger.atWarning().withCause(e).log("ER-CT-0004");
                    setActive(false);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socketManager.stopSocketManager();
        }
    }

    public boolean queueCommandTransmission(AppPacket appPacket) {
        return outboundCommandQueue.offer(appPacket);
    }
}
