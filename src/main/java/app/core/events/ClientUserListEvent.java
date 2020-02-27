package app.core.events;

public class ClientUserListEvent extends ClientEvent {

    private final String[] userList;

    public ClientUserListEvent(String[] userList) {
        this.userList = userList;
    }

    public String[] getUserList() {
        return userList;
    }

}
