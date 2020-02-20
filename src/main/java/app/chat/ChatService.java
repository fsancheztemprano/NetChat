package app.chat;

import app.core.packetmodel.AuthRemovePacket;
import app.core.packetmodel.AuthRequestPacket;
import com.google.common.eventbus.Subscribe;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.util.concurrent.ConcurrentHashMap;

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

    private ConcurrentHashMap<String, User> userRepo = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, User> sessionMap = new ConcurrentHashMap<>();


    @Subscribe
    public void validateLoginRequest(AuthRequestPacket loginRequest) {
        boolean validated = false;
        HashFunction hasher = Hashing.sha256();
        HashCode sha256 = hasher.newHasher()
                                .putUnencodedChars(loginRequest.getPassword())
                                .hash();
        String reHashedPass = sha256.toString();

        User receivedUserDetails = new User(loginRequest.getUsername(), reHashedPass);
        User existingUser = userRepo.putIfAbsent(loginRequest.getUsername(), receivedUserDetails);

        if (existingUser == null) {                                             // new user
            validated = true;
            sessionMap.put(loginRequest.getAuth(), receivedUserDetails);
        } else if (existingUser.getPassword().equals(reHashedPass)) {           // receivedUser isValid
            validated = true;
            sessionMap.put(loginRequest.getAuth(), existingUser);
        }
        loginRequest.getHandler().sendAuthApproval(validated);
    }

    @Subscribe
    public void userLogOut(AuthRemovePacket authRemovePacket) {
        sessionMap.remove(authRemovePacket.getAuth());
    }
}
