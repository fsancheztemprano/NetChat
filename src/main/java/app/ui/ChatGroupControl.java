package app.ui;

import app.core.ClientFacade;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener.Change;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import tools.log.Flogger;

public class ChatGroupControl extends AbstractChatControl {

    @FXML
    private ListView<String> listViewGroupUserList;
    private final ObservableSet<String> groupUsersObservableSet = FXCollections.observableSet();

    {
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ChatGroupPane.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (final IOException e) {
            Flogger.atSevere().withCause(e).log("ER-UI-CGC-0001");
        }
    }

    public ChatGroupControl(String title) {
        super(title);
    }

    @FXML
    @Override
    protected void initialize() {
        super.initialize();
        assert listViewGroupUserList != null : "fx:id=\"listViewUserList\" was not injected: check your FXML file 'ChatGroupPane.fxml'.";
        listViewGroupUserList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        groupUsersObservableSet.addListener((Change<? extends String> c) -> {
            if (c.wasAdded()) {
                listViewGroupUserList.getItems().add(c.getElementAdded());
            }
            if (c.wasRemoved()) {
                listViewGroupUserList.getItems().remove(c.getElementRemoved());
            }
        });
    }

    @FXML
    @Override
    public void sendMessageAction(ActionEvent event) {
        String draft = fieldMessageDraft.getText().trim();
        if (draft.length() > 0)
            ClientFacade.inst().sendGroupMessage(title, draft);
    }

    public void groupUserListUpdate(String[] userList) {
        Platform.runLater(() -> {
            List<String> list = Arrays.asList(userList);
            groupUsersObservableSet.addAll(list);
            groupUsersObservableSet.retainAll(list);
        });
    }
}
