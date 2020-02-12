package chat.model;

public abstract class ActivableThread extends Thread {

    protected boolean active = false;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public abstract void run();
}
