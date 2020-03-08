package app.core.events;

public class ClientGroupUserListEvent {

    private final String groupName;
    private final String[] userList;

    public ClientGroupUserListEvent(String groupName, String[] userList) {
        this.groupName = groupName;
        this.userList  = userList;
    }

    public String getGroupName() {
        return groupName;
    }

    public String[] getUserList() {
        return userList;
    }
}
