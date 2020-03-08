package app.core;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Activable extends Identifiable implements IActivable {

    protected final AtomicBoolean active = new AtomicBoolean(false);

    public boolean isActive() {
        return active.get();
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

}
