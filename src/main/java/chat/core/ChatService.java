package chat.core;

import java.util.ArrayList;

public class ChatService {


    private static ChatService instance;

    private ChatService() {
    }

    public static ChatService getInstance() {
        if (instance == null) {
            synchronized (ChatService.class) {
                if (instance == null) {
                    instance = new ChatService();
                }
            }
        }
        return instance;
    }


    private ArrayList<String> chat = new ArrayList<>();
    private ArrayList<String> chatters = new ArrayList<>();

    public boolean newMessage(String clientMessage) {
        return chat.add(clientMessage);
    }

    public boolean userJoin(String username) {
        return chatters.add(username);
    }

    public boolean userQuit(String username) {
        return chatters.remove(username);
    }


}
