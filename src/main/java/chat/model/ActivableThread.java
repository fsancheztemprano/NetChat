package chat.model;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ActivableThread extends Thread {

    protected AtomicBoolean active = new AtomicBoolean(false);

    public boolean isActive() {
        return active.get();
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    @Override
    public abstract void run();
}
