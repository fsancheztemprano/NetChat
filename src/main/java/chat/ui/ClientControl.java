package chat.ui;

import static tools.Asserts.isValidPort;

import chat.core.Client;
import chat.model.IClientStatusListener;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class ClientControl extends TabPane implements IClientStatusListener {

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
    private Tab tabClient;

    @FXML
    private Button btnExit;

    @FXML
    void btnConnectAction(ActionEvent event) {
        if (isValidPort(txtFieldPort.getText())) {
            Client.inst().setHostname(txtFieldIP.getText());
            Client.inst().setPort(Integer.parseInt(txtFieldPort.getText()));
            Client.inst().connect();
        } else
            onLogOutput("Puerto invalido");
    }

    @FXML
    void btnDisconnectAction(ActionEvent event) {
        Client.inst().disconnect();
    }

    @FXML
    void btnExitAction(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }


    private ChatControl chatControl;

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

        loadClientPane();
        Client.inst().setListener(this);
    }

    private void loadClientPane() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChatPane.fxml"));
        try {
            VBox root = loader.load();
            chatControl = loader.getController();
            tabClient.setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
            tabClient.setDisable(true);
        }
    }

    @Override
    public void onStatusChanged(boolean active) {
        if (active) {
            Platform.runLater(() -> {
                circleClientStatus.setFill(Color.LIMEGREEN);
                chatControl.circleClientStatus.setFill(Color.LIMEGREEN);
                btnConnect.setDisable(true);
                tabClient.setDisable(false);
                getSelectionModel().select(tabClient);
            });
        } else {
            Platform.runLater(() -> {
                circleClientStatus.setFill(Color.ORANGERED);
                chatControl.circleClientStatus.setFill(Color.ORANGERED);
                btnConnect.setDisable(false);
                tabClient.setDisable(true);
                getSelectionModel().select(tabConnect);
            });
        }
    }

    @Override
    public void onLogOutput(String string) {
        Platform.runLater(() -> txtAreaClientLog.appendText(string + "\n"));
    }

    @Override
    public void onChatMessageReceived(String username, String message) {
        Platform.runLater(() -> chatControl.areaChatLog.appendText(username + ": " + message + "\n"));
    }
}
