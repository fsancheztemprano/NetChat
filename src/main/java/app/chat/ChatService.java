package app.chat;

import app.core.packetmodel.AuthRequestPacket;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.Multimaps;
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

    private ConcurrentHashMap<String, User> userTable = new ConcurrentHashMap<>();

    private Multimap<String, Long> sessions = Multimaps.synchronizedSetMultimap(SetMultimapBuilder.hashKeys().hashSetValues().build());


    @Subscribe
    public void validateLoginRequest(AuthRequestPacket loginRequest) {
        boolean validated = false;
        HashFunction hasher = Hashing.sha256();
        HashCode sha256 = hasher.newHasher()
                                .putUnencodedChars(loginRequest.getPassword())
                                .hash();
        String reHashedPass = sha256.toString();

        User user = userTable.putIfAbsent(loginRequest.getUsername(), new User(loginRequest.getUsername(), reHashedPass));
        if (user == null || (user.getPassword().equals(reHashedPass))) { //new user request case
            validated = true;
        }
        loginRequest.getHandler().sendAuthApproval(validated);
    }
}
