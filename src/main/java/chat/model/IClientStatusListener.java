package chat.model;

public interface IClientStatusListener extends IStatusListener {

    void onChatMessageReceived(String username, String message);

}
