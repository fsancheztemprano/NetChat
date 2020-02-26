package app.chat;

import app.core.WorkerNodeManager;
import app.core.packetmodel.AuthRemoveEvent;
import app.core.packetmodel.AuthRequestEvent;
import com.google.common.eventbus.Subscribe;
import java.util.concurrent.ConcurrentHashMap;
import tools.HashTools;

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

    private ConcurrentHashMap<String, User> userRepo = new ConcurrentHashMap<>(); //Mock user repo (accepts new users on login) TODO: move to Persistence layer
    private ConcurrentHashMap<Long, User> sessionMap = new ConcurrentHashMap<>();

    @Subscribe
    public void validateLoginRequest(AuthRequestEvent loginRequest) {
        boolean validated = false;
        String reHashedPass = HashTools.getSha256(loginRequest.getPassword());

        User receivedUserDetails = new User(loginRequest.getUsername(), reHashedPass);
        User existingUser = userRepo.putIfAbsent(loginRequest.getUsername(), receivedUserDetails);

        if (existingUser == null) {                                             // new user
            validated = true;
            sessionMap.put(loginRequest.getAuth(), receivedUserDetails);
        } else if (existingUser.getPassword().equals(reHashedPass)) {           // receivedUser isValid
            validated = true;
            sessionMap.put(loginRequest.getAuth(), existingUser);
        }
        ((WorkerNodeManager) loginRequest.getHandler()).sendAuthApproval(validated);
//        if(validated){
//            sendListOfUsersLoggedIn(loginRequest.getUsername());
//        }
    }

    @Subscribe
    public void userLogOut(AuthRemoveEvent authRemoveEvent) {
        sessionMap.remove(authRemoveEvent.getAuth());
    }
}
