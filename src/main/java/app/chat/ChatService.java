package app.chat;

import app.core.AppPacket;
import app.core.AppPacket.ProtocolSignal;
import app.core.ServerSocketManager;
import app.core.events.WorkerLoginEvent;
import app.core.events.WorkerLoginEvent.AuthType;
import app.core.events.WorkerPrivateMessageEvent;
import app.core.events.WorkerRequestEvent;
import app.core.events.WorkerRequestEvent.RequestType;
import com.google.common.eventbus.Subscribe;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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
        Stream<String> usernameStream = sessionMap.values().stream().map(User::getUsername).distinct();
        appPacket.setList(usernameStream.toArray(String[]::new));
        return appPacket;
    }

    @Subscribe
    public void validateLoginRequest(WorkerLoginEvent workerLoginEvent) {
        if (workerLoginEvent.getAuthType() == AuthType.REQUEST) {

            boolean validated = false;
            String reHashedPass = HashTools.getSha256(workerLoginEvent.getHashedPassword());

            User receivedUserDetails = new User(workerLoginEvent.getUsername(), reHashedPass);
            User existingUser = userRepo.putIfAbsent(workerLoginEvent.getUsername(), receivedUserDetails);

            if (existingUser == null) {                                             // new user
                validated = true;
                sessionMap.put(workerLoginEvent.getSessionID(), receivedUserDetails);
            } else if (existingUser.getPassword().equals(reHashedPass)) {           // receivedUser isValid
                validated = true;
                sessionMap.put(workerLoginEvent.getSessionID(), existingUser);
            }
            chatServer.getSocketEventBus().post(sessionMap.toString());
            chatServer.sendAuthApproval(workerLoginEvent.getSessionID(), validated);

            if (validated) {
                broadcastUserList();
            }
        } else {
            sessionMap.remove(workerLoginEvent.getSessionID());
            broadcastUserList();
        }
    }

    @Subscribe
    public void userListRequest(WorkerRequestEvent workerRequestEvent) {
        if (workerRequestEvent.getRequestType() == RequestType.USER_LIST_REQUEST) {
            transmitUserList(workerRequestEvent.getSessionID());
        }
    }

    @Subscribe
    public void userPmRequest(WorkerPrivateMessageEvent pmEvent) {
        User origin = sessionMap.get(pmEvent.getSessionID());
        if (origin == null)
            return;
        Set<Long> originSessions = getIDs(origin.getUsername());
        if (originSessions.size() < 1)
            return;
        Set<Long> destinySessions = getIDs(pmEvent.getDestiny());
        if (destinySessions.size() < 1)
            return;

        AppPacket repackagedPm = new AppPacket(ProtocolSignal.CLIENT_PM);
        repackagedPm.setUsername(origin.getUsername());
        repackagedPm.setDestiny(pmEvent.getDestiny());
        repackagedPm.setMessage(pmEvent.getMessage());
        chatServer.transmitTo(destinySessions, repackagedPm);

        AppPacket ackPmPacket = new AppPacket(ProtocolSignal.CLIENT_PM_ACK);
        ackPmPacket.setUsername(origin.getUsername());
        ackPmPacket.setDestiny(pmEvent.getDestiny());
        ackPmPacket.setMessage(pmEvent.getMessage());
        chatServer.transmitTo(originSessions, ackPmPacket);
    }

    public Set<Long> getIDs(String username) {
        return sessionMap.entrySet()
                         .stream()
                         .filter(longUserEntry -> longUserEntry.getValue().getUsername().equals(username))
                         .mapToLong(Entry::getKey)
                         .boxed()
                         .collect(Collectors.toSet());
    }
}
