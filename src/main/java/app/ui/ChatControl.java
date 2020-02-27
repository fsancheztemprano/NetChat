package app.ui;

import app.core.ClientFacade;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import tools.log.Flogger;

public class ChatControl extends VBox {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextArea areaChatLog;

    @FXML
    private TextField fieldMessageDraft;

    @FXML
    private Button btnSend1;

    private Tab tab;
    private String username;

    {
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ChatPane.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (final IOException e) {
            Flogger.atSevere().withCause(e).log("ER-UI-CC-0001");
        }
    }

    public ChatControl(String username) {
        this.username = username;
        tab           = new Tab(username);
        tab.setClosable(true);
        tab.setContent(this);
    }

    @FXML
    void initialize() {
        assert areaChatLog != null : "fx:id=\"areaChatLog\" was not injected: check your FXML file 'ChatPane.fxml'.";
        assert fieldMessageDraft != null : "fx:id=\"fieldMessageDraft\" was not injected: check your FXML file 'ChatPane.fxml'.";
        assert btnSend1 != null : "fx:id=\"btnSend1\" was not injected: check your FXML file 'ChatPane.fxml'.";
    }


    @FXML
    void sendMessageAction(ActionEvent event) {
        String draft = fieldMessageDraft.getText();
        if (draft.length() > 0)
            ClientFacade.inst().sendPM(username, draft);

    }

    public Tab getTab() {
        return tab;
    }

    public String getUsername() {
        return username;
    }

    public void newMessage(String origin, String message) {
        areaChatLog.appendText(origin + ": " + message + "\n");
    }
}
