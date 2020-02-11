package chat.model;

public interface IClientStatusListener extends IStatusListener {

    void onResultReceived(String expression, boolean valid, double result);
}
