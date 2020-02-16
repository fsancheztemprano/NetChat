package chat.core;

public abstract class ActivableNotifier extends Activable {

    protected IStatusListener listener = null;

    public synchronized <T extends IStatusListener> void subscribe(T statusListener) {
        listener = statusListener;
    }

    public void unsubscribe() {
        listener = null;
    }

    protected void notifySocketStatus(boolean active) {
        if (listener != null)
            listener.onStatusChanged(active);
    }

    public void notifyLogOutput(String output) {
        if (listener != null)
            listener.onLogOutput(output);
    }

    @Override
    public void setActive(boolean active) {
        this.active.set(active);
        notifySocketStatus(active);
    }

    public void log(String output) {
        System.out.println(output);
        notifyLogOutput(output);
    }
}
