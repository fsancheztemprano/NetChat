package chat.model;

import java.util.Optional;

public abstract class ActivableNotifier extends Activable {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected Optional<IServerStatusListener> listener = Optional.empty();

    public void subscribe(IServerStatusListener serverStatusListener) {
        listener = (serverStatusListener != null
                    ? Optional.of(serverStatusListener)
                    : Optional.empty());
    }

    protected void notifySocketStatus(boolean active) {
        listener.ifPresent(listener -> listener.onStatusChanged(active));
    }

    protected void notifyLogOutput(String output) {
        listener.ifPresent(listener -> listener.onLogOutput(output));
    }

    @Override
    public void setActive(boolean active) {
        this.active.set(active);
        notifySocketStatus(active);
    }

    protected void log(String output) {
        System.out.println(output);
        notifyLogOutput(output);
    }
}
