package app.core;

import app.core.packetmodel.AppPacket;
import com.google.common.flogger.StackSize;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import tools.log.Flogger;

public abstract class AbstractCommandProcessor extends Activable implements Runnable {

    protected final BlockingQueue<AppPacket> toProcessCommandQueue;
    protected final IActivable manager;

    public AbstractCommandProcessor(IActivable manager) {
        this.manager               = manager;
        this.toProcessCommandQueue = new ArrayBlockingQueue<>(Byte.MAX_VALUE);
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
                if (!manager.isActive())
                    throw new SocketException();
            } catch (SocketException se) {
                Flogger.atWarning().withStackTrace(StackSize.FULL).withCause(se).log("ER-CP-0001");       //(manager closed) TODO msg: connection lost
                setActive(false);
                Thread.currentThread().interrupt();
            } catch (InterruptedException ie) {
                Flogger.atWarning().withStackTrace(StackSize.FULL).withCause(ie).log("ER-CP-0002");
                setActive(false);
            } catch (Exception e) {
                Flogger.atWarning().withStackTrace(StackSize.FULL).withCause(e).log("ER-CP-0000");
                setActive(false);
            }
        }
    }

    public boolean queueCommandProcess(AppPacket appPacket) {
        return toProcessCommandQueue.offer(appPacket);
    }

    protected abstract void processCommand(AppPacket appPacket);
}
