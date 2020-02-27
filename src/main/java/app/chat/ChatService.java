package app.chat;

import app.core.AppPacket;
import app.core.AppPacket.ProtocolSignal;
import app.core.ServerSocketManager;
import app.core.events.WorkerAuthEvent;
import app.core.events.WorkerAuthEvent.AuthType;
import app.core.events.WorkerRequestEvent;
import app.core.events.WorkerRequestEvent.RequestType;
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

    private void broadcastUserList() {
        chatServer.transmitTo(new HashSet<>(sessionMap.keySet()), getUserListAppPacket());
    }

    private void transmitUserList(long id) {
        chatServer.transmitTo(id, getUserListAppPacket());
    }

    private AppPacket getUserListAppPacket() {
        AppPacket appPacket = new AppPacket(ProtocolSignal.BROADCAST_USER_LIST);
        appPacket.setUsername("SERVER");
        Stream<String> usernameStream = sessionMap.values().stream().map(User::getUsername).distinct();
        appPacket.setList(usernameStream.toArray(String[]::new));
        return appPacket;
    }

    @Subscribe
    public void validateLoginRequest(WorkerAuthEvent workerAuthEvent) {
        if (workerAuthEvent.getAuthType() == AuthType.REQUEST) {

            boolean validated = false;
            String reHashedPass = HashTools.getSha256(workerAuthEvent.getHashedPassword());

            User receivedUserDetails = new User(workerAuthEvent.getUsername(), reHashedPass);
            User existingUser = userRepo.putIfAbsent(workerAuthEvent.getUsername(), receivedUserDetails);

            if (existingUser == null) {                                             // new user
                validated = true;
                sessionMap.put(workerAuthEvent.getSessionID(), receivedUserDetails);
            } else if (existingUser.getPassword().equals(reHashedPass)) {           // receivedUser isValid
                validated = true;
                sessionMap.put(workerAuthEvent.getSessionID(), existingUser);
            }
            chatServer.getSocketEventBus().post(sessionMap.toString());
            chatServer.sendAuthApproval(workerAuthEvent.getSessionID(), validated);

            if (validated) {
                broadcastUserList();
            }
        } else {
            sessionMap.remove(workerAuthEvent.getSessionID());
            broadcastUserList();
        }
    }

    @Subscribe
    public void userRequestReceived(WorkerRequestEvent workerRequestEvent) {
        if (workerRequestEvent.getRequestType() == RequestType.USER_LIST_REQUEST) {
            transmitUserList(workerRequestEvent.getSessionID());
        }
    }

}
