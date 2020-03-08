package app.chat;

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
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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
        chatServer.broadcastUserList(getUserListArray());
    }

    private void broadcastGroupList() {
        chatServer.broadcastGroupList(getGroupListArray());
    }

    private void transmitUserList(long sessionID) {
        chatServer.sendUserList(sessionID, getUserListArray());
    }

    private void transmitGroupList(long sessionID) {
        chatServer.sendGroupList(sessionID, getGroupListArray());
    }

    private String[] getUserListArray() {
        return sessionMap.values().stream()
                         .map(User::getUsername)
                         .distinct()
                         .toArray(String[]::new);
    }

    private String[] getGroupListArray() {
        return groupsMultimap.keys().toArray(new String[0]);
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
        if (!(workerLoginEvent.getUsername().trim().length() < 5) && !workerLoginEvent.getUsername().trim().equalsIgnoreCase("servidor")) {

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
        }

        if (validated) {
            chatServer.sendLoginSuccess(workerLoginEvent.getSessionID());
            broadcastUserList();
        } else
            chatServer.sendAlertMessage(workerLoginEvent.getSessionID(), "Login Denied");
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
        if (destinySessions.size() < 1) {
            chatServer.sendAlertMessage(pmEvent.getSessionID(), "No recepient found");
            return;
        }
        chatServer.sendPM(destinySessions, origin.getUsername(), pmEvent.getDestiny(), pmEvent.getMessage());
        chatServer.sendPMAck(originSessions, origin.getUsername(), pmEvent.getDestiny(), pmEvent.getMessage());
    }

    @Subscribe
    public void newGroupRequest(WorkerNewGroupEvent newGroupEvent) {
        String newGroupName = newGroupEvent.getNewGroupName().trim();
        if (newGroupName.length() < 5) {
            chatServer.sendAlertMessage(newGroupEvent.getSessionID(), "Group Name too short (<5)");
            return;
        }
        if (groupsMultimap.containsKey(newGroupName)) {
            chatServer.sendAlertMessage(newGroupEvent.getSessionID(), "Group name exists");
            return;
        }
        groupsMultimap.put(newGroupName, "SERVIDOR");
        broadcastGroupList();
    }
}
