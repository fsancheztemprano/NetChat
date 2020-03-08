package app.ui;

import app.MainUI;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import tools.fx.FxDialogs;
import tools.log.Flogger;

public class LauncherControl extends VBox {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnServidor;

    @FXML
    private Button btnCliente;

    @FXML
    void btnClienteAction() {

        Platform.runLater(() -> {
            try {
                TabPane root = FXMLLoader.load(getClass().getResource("/fxml/ClientPane.fxml"));
                MainUI.setPaneWithNewScene(root);
            } catch (IOException e) {
                FxDialogs.showException("FXML Error", "Error loading client FXML", e);
                Flogger.atWarning().withCause(e).log("ER-LC-001");
            }
        });

    }

    @FXML
    void btnServidorAction() {
        try {
            VBox serverPane = FXMLLoader.load(getClass().getResource("/fxml/ServerPane.fxml"));
            MainUI.setPaneWithNewScene(serverPane);
        } catch (IOException e) {
            FxDialogs.showException("FXML Error", "Error loading server FXML", e);
            Flogger.atWarning().withCause(e).log("ER-LC-002");
        }
    }

    @FXML
    void initialize() {
        assert btnServidor != null : "fx:id=\"btnServidor\" was not injected: check your FXML file 'LauncherPane.fxml'.";
        assert btnCliente != null : "fx:id=\"btnCliente\" was not injected: check your FXML file 'LauncherPane.fxml'.";
    }
}
