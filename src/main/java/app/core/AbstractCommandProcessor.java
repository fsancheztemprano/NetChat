package app.core;

import app.core.packetmodel.AppPacket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import tools.log.Flogger;

public abstract class AbstractCommandProcessor extends Activable implements Runnable {

    protected BlockingQueue<AppPacket> toProcessCommandQueue;
    protected IActivable manager;

    public AbstractCommandProcessor(IActivable manager, BlockingQueue<AppPacket> toProcessCommandQueue) {
        this.manager               = manager;
        this.toProcessCommandQueue = toProcessCommandQueue;
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
                Flogger.atWarning().withCause(se).log("ER-CP-0001");       //(manager closed) TODO msg: connection lost
                setActive(false);
                Thread.currentThread().interrupt();
            } catch (InterruptedException ie) {
                Flogger.atWarning().withCause(ie).log("ER-CP-0002");
                setActive(false);
            } catch (Exception e) {
                Flogger.atWarning().withCause(e).log("ER-CP-0000");
                setActive(false);
            }
        }
    }

    protected abstract void processCommand(AppPacket appPacket);
}
