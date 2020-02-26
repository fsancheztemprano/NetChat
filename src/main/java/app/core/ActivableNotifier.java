package app.core;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("UnstableApiUsage")
public abstract class ActivableNotifier extends Activable {


    protected final EventBus socketEventBus = new EventBus(getSessionID() + "");

    public EventBus getSocketEventBus() {
        return socketEventBus;
    }

    public void register(Object listener) {
        socketEventBus.register(listener);
    }

    public void unregister(Object listener) {
        socketEventBus.unregister(listener);
    }

    @Override
    public void setActive(boolean active) {
        this.active.set(active);
        Boolean boxedActive = active;
        socketEventBus.post(boxedActive);
    }

    public void log(String output) {
        System.out.println(output);
        socketEventBus.post(output);
    }

}
