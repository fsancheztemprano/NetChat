package chat.model;

public interface IStatusListener {

    void onStatusChanged();

    void onLogOutput(String string);
}
