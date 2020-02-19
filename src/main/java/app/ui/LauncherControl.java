package app.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

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
    void btnClienteAction(ActionEvent event) {
        try {
            TabPane root = FXMLLoader.load(getClass().getResource("/fxml/ClientPane.fxml"));
            MainUI.setPaneWithNewScene(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnServidorAction(ActionEvent event) {
        try {
            VBox serverPane = FXMLLoader.load(getClass().getResource("/fxml/ServerPane.fxml"));
            MainUI.setPaneWithNewScene(serverPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        assert btnServidor != null : "fx:id=\"btnServidor\" was not injected: check your FXML file 'LauncherPane.fxml'.";
        assert btnCliente != null : "fx:id=\"btnCliente\" was not injected: check your FXML file 'LauncherPane.fxml'.";

    }
}
