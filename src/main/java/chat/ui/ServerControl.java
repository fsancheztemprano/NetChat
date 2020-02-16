package chat.ui;

import static tools.Asserts.isValidPort;

import chat.core.IServerStatusListener;
import chat.core.Server;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class ServerControl extends VBox implements IServerStatusListener {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField txtFieldHostname;

    @FXML
    private TextField txtFieldPort;

    @FXML
    private Button btnServerStart;

    @FXML
    private Label labelNumClients;

    @FXML
    private Button btnServerStop;

    @FXML
    private Circle circleServerStatus;

    @FXML
    private TextArea txtAreaServerLog;

    @FXML
    private Button btnExit;

    @FXML
    void btnExitAction(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    void btnServerStartAction(ActionEvent event) {
        if (isValidPort(txtFieldPort.getText())) {
            Server.inst().setHostname(txtFieldHostname.getText());
            Server.inst().setPort(Integer.parseInt(txtFieldPort.getText()));
            Server.inst().startServer();
        } else
            onLogOutput("Puerto invalido");
    }

    @FXML
    void btnServerStopAction(ActionEvent event) {
        Server.inst().stopServer();
    }

    @FXML
    void initialize() {
        assert txtFieldHostname != null : "fx:id=\"txtFieldHostname\" was not injected: check your FXML file 'ServerPane.fxml'.";
        assert txtFieldPort != null : "fx:id=\"txtFieldPort\" was not injected: check your FXML file 'ServerPane.fxml'.";
        assert btnServerStart != null : "fx:id=\"btnServerStart\" was not injected: check your FXML file 'ServerPane.fxml'.";
        assert btnServerStop != null : "fx:id=\"btnServerStop\" was not injected: check your FXML file 'ServerPane.fxml'.";
        assert circleServerStatus != null : "fx:id=\"circleServerStatus\" was not injected: check your FXML file 'ServerPane.fxml'.";
        assert txtAreaServerLog != null : "fx:id=\"txtAreaServerLog\" was not injected: check your FXML file 'ServerPane.fxml'.";
        assert btnExit != null : "fx:id=\"btnExit\" was not injected: check your FXML file 'ServerPane.fxml'.";
        assert labelNumClients != null : "fx:id=\"labelNumClients\" was not injected: check your FXML file 'ServerPane.fxml'.";

        Server.inst().setListener(this);
        Platform.runLater(() -> btnServerStart.requestFocus());
    }

    @Override
    public void onActiveClientsChange(int activeClients) {
        Platform.runLater(() -> labelNumClients.setText(Integer.toString(activeClients)));
    }

    @Override
    public void onStatusChanged(boolean active) {
        if (active)
            Platform.runLater(() -> {
                circleServerStatus.setFill(Color.LIMEGREEN);
                btnServerStart.setDisable(true);
            });
        else
            Platform.runLater(() -> {
                circleServerStatus.setFill(Color.ORANGERED);
                btnServerStart.setDisable(false);
            });
    }

    @Override
    public void onLogOutput(String string) {
        Platform.runLater(() -> txtAreaServerLog.appendText(string + "\n"));
    }
}
