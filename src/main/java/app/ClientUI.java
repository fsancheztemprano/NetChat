package app;

import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import tools.fx.FxApplication;
import tools.fx.FxDialogs;

public class ClientUI extends FxApplication {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

//        mainStage.setMinWidth(600);
//        mainStage.setMinHeight(400);
        try {
            TabPane root = FXMLLoader.load(getClass().getResource("/fxml/ClientPane.fxml"));
            setPaneWithNewScene(root);
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            FxDialogs.showException("Error", "Error Initializing Window", e);
            System.exit(1);
        }
    }
}
