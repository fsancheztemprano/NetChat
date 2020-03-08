package app.chat;

import app.core.AppPacket;
import app.core.AppPacket.ProtocolSignal;
import app.core.ServerSocketManager;
import app.core.events.WorkerLoginEvent;
import app.core.events.WorkerLogoutEvent;
import app.core.events.WorkerNewGroupEvent;
import app.core.events.WorkerPrivateMessageEvent;
import app.core.events.WorkerUserListEvent;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
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
    private final SetMultimap<String, String> groupsMultimap = Multimaps.synchronizedSetMultimap(MultimapBuilder.SetMultimapBuilder.hashKeys().hashSetValues().build());
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

    private void transmitGroupList(long id) {
        chatServer.transmitTo(id, getGroupListAppPacket());
    }

    private AppPacket getUserListAppPacket() {
        Stream<String> usernameStream = sessionMap.values().stream()
                                                  .map(User::getUsername)
                                                  .distinct();
        return AppPacket.ofType(ProtocolSignal.SERVER_SEND_USER_LIST)
                        .setList(usernameStream.toArray(String[]::new));
    }

    private AppPacket getGroupListAppPacket() {
        Stream<String> groupNameStream = sessionMap.values().stream()
                                                   .map(User::getUsername)
                                                   .distinct();
        return AppPacket.ofType(ProtocolSignal.SERVER_SEND_GROUP_LIST)
                        .setList(groupNameStream.toArray(String[]::new));
    }

    public Set<Long> getUsernameSessionIDs(String username) {
        return sessionMap.entrySet()
                         .stream()
                         .filter(longUserEntry -> longUserEntry.getValue().getUsername().equals(username))
                         .mapToLong(Entry::getKey)
                         .boxed()
                         .collect(Collectors.toSet());
    }

    @Subscribe
    public void validateLoginRequest(WorkerLoginEvent workerLoginEvent) {

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
    }

    @Subscribe
    public void logoutRequest(WorkerLogoutEvent workerLogoutEvent) {
        sessionMap.remove(workerLogoutEvent.getSessionID());
        broadcastUserList();
    }

    @Subscribe
    public void userListRequest(WorkerUserListEvent workerUserListEvent) {
        transmitUserList(workerUserListEvent.getSessionID());
    }

    @Subscribe
    public void groupListRequest(WorkerUserListEvent workerUserListEvent) {
        transmitGroupList(workerUserListEvent.getSessionID());
    }

    @Subscribe
    public void userPmRequest(WorkerPrivateMessageEvent pmEvent) {
        User origin = sessionMap.get(pmEvent.getSessionID());
        if (origin == null)
            return;
        Set<Long> originSessions = getUsernameSessionIDs(origin.getUsername());
        if (originSessions.size() < 1)
            return;
        Set<Long> destinySessions = getUsernameSessionIDs(pmEvent.getDestiny());
        if (destinySessions.size() < 1)
            return;

        chatServer.transmitTo(destinySessions,
                              AppPacket.ofType(ProtocolSignal.CLIENT_PM)
                                       .setUsername(origin.getUsername())
                                       .setDestiny(pmEvent.getDestiny())
                                       .setMessage(pmEvent.getMessage()));

        chatServer.transmitTo(originSessions,
                              AppPacket.ofType(ProtocolSignal.CLIENT_PM_ACK)
                                       .setUsername(origin.getUsername())
                                       .setDestiny(pmEvent.getDestiny())
                                       .setMessage(pmEvent.getMessage()));
    }

    @Subscribe
    public void newGroupRequest(WorkerNewGroupEvent newGroupEvent) {
        String newGroupName = newGroupEvent.getNewGroupName();
        if (newGroupName.length() > 4) {

        } else
            chatServer.transmitTo(newGroupEvent.getSessionID(),
                                  AppPacket.ofType(ProtocolSignal.SERVER_RESPONSE_NEW_GROUP_DENIED)
                                           .setMessage("Group Name too short (<5)"));

    }
}
