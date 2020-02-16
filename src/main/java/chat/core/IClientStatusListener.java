package chat.core;

public interface IClientStatusListener extends IStatusListener {

    void onChatMessageReceived(String username, String message);

}
