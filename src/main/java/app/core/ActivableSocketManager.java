package app.core;

import app.core.events.SocketStatusEvent;
import com.google.common.eventbus.EventBus;

@SuppressWarnings("UnstableApiUsage")
public abstract class ActivableSocketManager extends Activable {


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
        socketEventBus.post(new SocketStatusEvent(this, active));
    }

    public void log(String output) {
        System.out.println(output);
        socketEventBus.post(output);
    }

}
