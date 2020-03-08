package app.ui;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import tools.log.Flogger;

public class ChatGroupControl extends AbstractChatControl {

    @FXML
    private ListView<String> listViewUserList;

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
        assert listViewUserList != null : "fx:id=\"listViewUserList\" was not injected: check your FXML file 'ChatGroupPane.fxml'.";
    }

    @FXML
    @Override
    public void sendMessageAction(ActionEvent event) {

    }
}
