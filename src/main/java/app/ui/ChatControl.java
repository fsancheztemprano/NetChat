package app.ui;

import app.core.ClientFacade;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import tools.fx.FxDialogs;

public class ChatControl extends VBox {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    protected TextArea areaChatLog;

    @FXML
    private TextField fieldMessageDraft;

    @FXML
    private Button btnSend1;

    @FXML
    protected Circle circleClientStatus;

    @FXML
    private TextField fieldUsername;

    @FXML
    private Button btnSetUsername;

    @FXML
    private Button btnExit;

    @FXML
    void btnExitAction(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    void btnSetUsernameAction(ActionEvent event) {
        if (checkUsername().length() > 1)
            ClientFacade.inst().sendMessage(fieldUsername.getText(), "# Se ha unido al chat.");
    }


    @FXML
    void sendMessageAction(ActionEvent event) {
        if (checkUsername().length() > 1 && fieldMessageDraft.getText().length() > 0) {
            ClientFacade.inst().sendMessage(fieldUsername.getText(), fieldMessageDraft.getText());
            fieldMessageDraft.setText("");
        }
    }

    private String checkUsername() {
        String field = fieldUsername.getText();
        if (!btnSetUsername.isDisabled()) {
            if (field.length() < 4 || field.equalsIgnoreCase("user")) {
                FxDialogs.showError("Error", "invalid Username");
                return "";
            } else {
                fieldUsername.setDisable(true);
                btnSetUsername.setDisable(true);
            }
        }
        return field;
    }

    @FXML
    void initialize() {
        assert areaChatLog != null : "fx:id=\"areaChatLog\" was not injected: check your FXML file 'ChatPane.fxml'.";
        assert fieldMessageDraft != null : "fx:id=\"fieldMessageDraft\" was not injected: check your FXML file 'ChatPane.fxml'.";
        assert btnSend1 != null : "fx:id=\"btnSend1\" was not injected: check your FXML file 'ChatPane.fxml'.";
        assert circleClientStatus != null : "fx:id=\"circleClientStatus\" was not injected: check your FXML file 'ChatPane.fxml'.";
        assert fieldUsername != null : "fx:id=\"fieldUsername\" was not injected: check your FXML file 'ChatPane.fxml'.";
        assert btnSetUsername != null : "fx:id=\"btnSetUsername\" was not injected: check your FXML file 'ChatPane.fxml'.";
        assert btnExit != null : "fx:id=\"btnExit\" was not injected: check your FXML file 'ChatPane.fxml'.";

    }
}
