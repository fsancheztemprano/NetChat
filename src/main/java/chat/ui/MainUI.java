package chat.ui;

import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import tools.fx.FxApplication;
import tools.fx.FxDialogs;

public class MainUI extends FxApplication {

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
            Pane root = FXMLLoader.load(getClass().getResource("/fxml/LauncherPane.fxml"));
            setPaneWithNewScene(root);
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            FxDialogs.showException("Error", "Error Initializing Window", e);
            System.exit(1);
        }
    }
}
