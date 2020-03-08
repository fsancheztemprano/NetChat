package app.core.events;

public class ClientGroupListEvent extends ClientEvent {

    private final String[] groupList;

    public ClientGroupListEvent(String[] groupList) {
        this.groupList = groupList;
    }

    public String[] getGroupList() {
        return groupList;
    }

}
