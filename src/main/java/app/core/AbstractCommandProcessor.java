package app.core;

import app.Globals;
import com.google.common.eventbus.EventBus;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import tools.log.Flogger;

public abstract class AbstractCommandProcessor extends Activable implements Runnable {

    protected final BlockingQueue<AppPacket> toProcessCommandQueue = new ArrayBlockingQueue<>(Byte.MAX_VALUE);

    protected final AbstractNodeManager socketManager;

    protected final EventBus eventBus;

    public AbstractCommandProcessor(AbstractNodeManager socketManager, EventBus eventBus) {
        this.socketManager = socketManager;
        this.eventBus      = eventBus;
    }

    @Override
    public void run() {
        AppPacket appPacket = null;
        setActive(true);
        while (isActive()) {
            try {
                appPacket = this.toProcessCommandQueue.poll(Globals.PROCESSORS_THREAD_TIMEOUT, TimeUnit.SECONDS);
                if (appPacket != null) {
                    processCommand(appPacket);
                }
                if (!socketManager.isActive())
                    throw new SocketException();
//            } catch (SocketException se) {
//                Flogger.atWarning().withCause(se).log("ER-CP-0001");       //(manager closed) TODO msg: connection lost
//                setActive(false);
//            } catch (InterruptedException ie) {
//                Flogger.atWarning().withCause(ie).log("ER-CP-0002");
//                setActive(false);
            } catch (Exception e) {
                Flogger.atWarning().withCause(e).log("ER-CP-0000");
                setActive(false);
                socketManager.stopSocketManager();
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean queueCommandProcess(AppPacket appPacket) {
        return toProcessCommandQueue.offer(appPacket);
    }

    protected abstract void processCommand(AppPacket appPacket);
}
