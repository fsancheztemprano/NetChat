package app.core;

import app.core.packetmodel.AppPacket;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;
import tools.log.Flogger;

public class CommandReceiver extends Activable implements Runnable {

    private final AbstractNodeManager socketManager;

    public CommandReceiver(AbstractNodeManager socketManager) {
        this.socketManager = socketManager;
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
                        appPacket.setHandler(socketManager);
                        socketManager.updateHeartbeatDaemonTime();                  //TODO migrate to event bus
                        socketManager.log("In: " + appPacket.toString());
                        socketManager.getCommandProcessor().queueCommandProcess(appPacket);
                    } catch (SocketException se) {
                        setActive(false);
                        Flogger.atWarning().withCause(se).log("ER-CR-0001");       //(inputStream closed)TODO msg:Server connection lost, Event Bus?
                        Thread.currentThread().interrupt();
                    } catch (IOException ioe) {
                        setActive(false);
                        Flogger.atWarning().withCause(ioe).log("ER-CR-0002");
                    } catch (ClassNotFoundException cnfe) {
                        Flogger.atWarning().withCause(cnfe).log("ER-CR-0003");
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
