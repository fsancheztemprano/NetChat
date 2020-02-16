package chat.core;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Activable implements IActivable {

    protected AtomicBoolean active = new AtomicBoolean(false);

    public boolean isActive() {
        return active.get();
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

}
