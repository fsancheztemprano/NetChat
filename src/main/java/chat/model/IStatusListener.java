package chat.model;

public interface IStatusListener {

    void onStatusChanged(boolean active);

    void onLogOutput(String string);
}
