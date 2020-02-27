package app.chat;

import app.core.ServerSocketManager;
import app.core.WorkerNodeManager;
import app.core.packetmodel.AppPacket;
import app.core.packetmodel.AppPacket.ProtocolSignal;
import app.core.packetmodel.AuthRemoveEvent;
import app.core.packetmodel.AuthRequestEvent;
import com.google.common.eventbus.Subscribe;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
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

    private final ConcurrentHashMap<String, User> userRepo = new ConcurrentHashMap<>(); //Mock user repo (accepts new users on login) TODO: move to Persistence layer
    private final ConcurrentHashMap<Long, User> sessionMap = new ConcurrentHashMap<>();
    private ServerSocketManager chatServer = null;

    public ServerSocketManager getChatServer() {
        return chatServer;
    }

    public void setChatServer(ServerSocketManager chatServer) {
        this.chatServer = chatServer;
    }

    @Subscribe
    public void validateLoginRequest(AuthRequestEvent loginRequest) {
        boolean validated = false;
        String reHashedPass = HashTools.getSha256(loginRequest.getPassword());

        User receivedUserDetails = new User(loginRequest.getUsername(), reHashedPass);
        User existingUser = userRepo.putIfAbsent(loginRequest.getUsername(), receivedUserDetails);

        if (existingUser == null) {                                             // new user
            validated = true;
            sessionMap.put(loginRequest.getHandler().getSessionID(), receivedUserDetails);
        } else if (existingUser.getPassword().equals(reHashedPass)) {           // receivedUser isValid
            validated = true;
            sessionMap.put(loginRequest.getHandler().getSessionID(), existingUser);
        }
        sessionMap.forEach((k, v) -> System.out.println(k + " " + v));
        ((WorkerNodeManager) loginRequest.getHandler()).sendAuthApproval(validated);

        if (validated) {
            broadcastUserList();
        }
    }

    private void broadcastUserList() {
        AppPacket appPacket = new AppPacket(ProtocolSignal.BROADCAST_USER_LIST);
        appPacket.setUsername("SERVER");
        Stream<String> usernameStream = sessionMap.values().stream().map(User::getUsername).distinct();
        appPacket.setList(usernameStream.toArray(String[]::new));
        chatServer.transmitToListOfIds(new HashSet<>(sessionMap.keySet()), appPacket);
    }

    @Subscribe
    public void userLogOut(AuthRemoveEvent authRemoveEvent) {
        sessionMap.remove(authRemoveEvent.getAuth());
    }
}
