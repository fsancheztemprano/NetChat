package chat.ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;

public class ClientControl {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tabConnect;

    @FXML
    private TextField txtFieldPort;

    @FXML
    private TextField txtFieldIP;

    @FXML
    private Button btnConnect;

    @FXML
    private Circle circleClientStatus;

    @FXML
    private Button btnDisconnect;

    @FXML
    private TextArea txtAreaClientLog;

    @FXML
    private Button btnExit;

    @FXML
    void btnConnectAction(ActionEvent event) {

    }

    @FXML
    void btnDisconnectAction(ActionEvent event) {

    }

    @FXML
    void btnExitAction(ActionEvent event) {

    }

    @FXML
    void initialize() {
        assert tabPane != null : "fx:id=\"tabPane\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert tabConnect != null : "fx:id=\"tabConnect\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert txtFieldPort != null : "fx:id=\"txtFieldPort\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert txtFieldIP != null : "fx:id=\"txtFieldIP\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert btnConnect != null : "fx:id=\"btnConnect\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert circleClientStatus != null : "fx:id=\"circleClientStatus\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert btnDisconnect != null : "fx:id=\"btnDisconnect\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert txtAreaClientLog != null : "fx:id=\"txtAreaClientLog\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert btnExit != null : "fx:id=\"btnExit\" was not injected: check your FXML file 'ClientPane.fxml'.";

    }
}
