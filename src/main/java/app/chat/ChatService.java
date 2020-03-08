package app.chat;

import app.core.ServerSocketManager;
import app.core.events.WorkerGroupListEvent;
import app.core.events.WorkerGroupMessageEvent;
import app.core.events.WorkerJoinGroupEvent;
import app.core.events.WorkerLoginEvent;
import app.core.events.WorkerLogoutEvent;
import app.core.events.WorkerNewGroupEvent;
import app.core.events.WorkerPrivateMessageEvent;
import app.core.events.WorkerQuitGroupEvent;
import app.core.events.WorkerUserListEvent;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.eventbus.Subscribe;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import tools.HashTools;

public class ChatService {

    private final ConcurrentHashMap<String, User> userRepo = new ConcurrentHashMap<>(); //Mock user repo (accepts new users on login)
    private final ConcurrentHashMap<Long, User> sessionMap = new ConcurrentHashMap<>();
    private final SetMultimap<String, Long> groupsMultimap = Multimaps.synchronizedSetMultimap(MultimapBuilder.SetMultimapBuilder.hashKeys().hashSetValues().build());
    private ServerSocketManager chatServer = null;


    private ChatService() {
    }

    private static ChatService instance;

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

    public void setChatServer(ServerSocketManager chatServer) {
        this.chatServer = chatServer;
    }

    private Optional<String> getUsername(final long sessionID) {
        User user = sessionMap.get(sessionID);
        if (user == null)
            return Optional.empty();
        else
            return Optional.of(user.getUsername());
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

    private void broadcastGroupUserList(String groupName) {
        chatServer.broadcastGroupUserList(groupsMultimap.get(groupName), groupName, getGroupUserList(groupName));
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

    private String[] getGroupUserList(String groupName) {
        return groupsMultimap.get(groupName).stream()
                             .map(sessionMap::get)
                             .filter(Objects::nonNull)
                             .map(User::getUsername)
                             .distinct()
                             .toArray(String[]::new);
    }

    public Set<Long> getUsernameSessionIDs(String username) {
        return sessionMap.entrySet()
                         .stream()
                         .filter(longUserEntry -> longUserEntry.getValue().getUsername().equals(username))
                         .mapToLong(Entry::getKey)
                         .boxed()
                         .collect(Collectors.toSet());
    }

    public Set<Long> getGroupSessionIDs(String groupName) {
        return groupsMultimap.get(groupName).stream()
                             .filter(l -> l != -1)
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
    public void groupListRequest(WorkerGroupListEvent groupListEvent) {
        transmitGroupList(groupListEvent.getSessionID());
    }

    @Subscribe
    public void userPrivateMessageRequest(WorkerPrivateMessageEvent pmEvent) {
        Optional<String> usernameO = getUsername(pmEvent.getSessionID());
        if (!usernameO.isPresent())
            return;
        Set<Long> originSessions = getUsernameSessionIDs(usernameO.get());
        if (originSessions.size() < 1)
            return;
        Set<Long> destinySessions = getUsernameSessionIDs(pmEvent.getDestiny());
        if (destinySessions.size() < 1) {
            chatServer.sendAlertMessage(pmEvent.getSessionID(), "No recepient found");
            return;
        }
        chatServer.pipePrivateMessage(destinySessions, usernameO.get(), "RCV", pmEvent.getMessage());
        if (!usernameO.get().equals(pmEvent.getDestiny()))
            chatServer.pipePrivateMessage(originSessions, "ACK", pmEvent.getDestiny(), pmEvent.getMessage());
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
        groupsMultimap.put(newGroupName, -1L);
        broadcastGroupList();
    }

    @Subscribe
    public void joinGroupRequest(WorkerJoinGroupEvent joinGroupEvent) {
        String joinGroup = joinGroupEvent.getGroupName().trim();
        if (!groupsMultimap.containsKey(joinGroup)) {
            chatServer.sendAlertMessage(joinGroupEvent.getSessionID(), "Group does not exist");
            return;
        }
        groupsMultimap.put(joinGroup, joinGroupEvent.getSessionID());
        broadcastGroupUserList(joinGroup);
    }

    @Subscribe
    public void quitGroupRequest(WorkerQuitGroupEvent quitGroupEvent) {
        String joinGroup = quitGroupEvent.getGroupName().trim();
        if (!groupsMultimap.containsKey(joinGroup)) {
            chatServer.sendAlertMessage(quitGroupEvent.getSessionID(), "Group does not exist");
            return;
        }
        groupsMultimap.remove(joinGroup, quitGroupEvent.getSessionID());
        broadcastGroupUserList(joinGroup);
    }

    @Subscribe
    public void userGroupMessageRequest(WorkerGroupMessageEvent groupMessageEvent) {
        if (!groupsMultimap.get(groupMessageEvent.getDestiny()).contains(groupMessageEvent.getSessionID())) {
            chatServer.sendAlertMessage(groupMessageEvent.getSessionID(), "You are not on that group (" + groupMessageEvent.getDestiny() + ")");
            return;
        }
        Set<Long> destinySessions = getGroupSessionIDs(groupMessageEvent.getDestiny());
        if (destinySessions.size() < 1) {
            chatServer.sendAlertMessage(groupMessageEvent.getSessionID(), "No recepient found");
            return;
        }
        Optional<String> usernameO = getUsername(groupMessageEvent.getSessionID());
        if (!usernameO.isPresent()) {
            chatServer.sendAlertMessage(groupMessageEvent.getSessionID(), "No remitent found");
            return;
        }
        chatServer.pipeGroupMessage(destinySessions, usernameO.get(), groupMessageEvent.getDestiny(), groupMessageEvent.getMessage());
    }

}
