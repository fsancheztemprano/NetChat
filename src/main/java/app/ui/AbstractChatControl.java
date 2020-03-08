package app.ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public abstract class AbstractChatControl extends VBox {

    protected final String title;
    protected final Tab tab;

    @FXML
    protected ResourceBundle resources;

    @FXML
    protected URL location;

    @FXML
    protected TextArea areaChatLog;

    @FXML
    protected TextField fieldMessageDraft;

    @FXML
    protected Button btnSend;

    public AbstractChatControl(String title) {
        this.title = title;
        this.tab   = new Tab(title);
    }

    @FXML
    protected void initialize() {
        assert areaChatLog != null : "fx:id=\"areaChatLog\" was not injected: check your FXML file 'ChatPane.fxml'.";
        assert fieldMessageDraft != null : "fx:id=\"fieldMessageDraft\" was not injected: check your FXML file 'ChatPane.fxml'.";
        assert btnSend != null : "fx:id=\"btnSend1\" was not injected: check your FXML file 'ChatPane.fxml'.";
    }

    public String getTitle() {
        return title;
    }

    public Tab getTab() {
        return tab;
    }

    @FXML
    public abstract void sendMessageAction(ActionEvent event);
}
