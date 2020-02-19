package app.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private volatile ArrayList<User> userRepo = new ArrayList<>(initUsers());

    private volatile ArrayList<User> loggedInUsers = new ArrayList<>();

    public static List<User> initUsers(){
        return Arrays.asList(
            new User("admin", "admadm"),
            new User("user1", "user1"),
            new User("user2", "user2"),
            new User("user3", "user3"));
    }



}
