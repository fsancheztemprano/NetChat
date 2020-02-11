package tools.fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public abstract class FxApplication extends Application {

    protected static Stage mainStage;

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void setMainStage(Stage mainStage) {
        FxApplication.mainStage = mainStage;
    }

    public static Scene getActiveScene() {
        return getMainStage().getScene();
    }

    public static void setActiveScene(Scene activeScene) {
        getMainStage().setScene(activeScene);
    }

    public static void setPaneWithNewScene(Pane pane) {
        setActiveScene(new Scene(pane));
    }

    public static void setPaneOnActiveScene(Pane pane) {
        getActiveScene().setRoot(pane);
    }

    public static Pane getRootPane() {
        return (Pane) getActiveScene().getRoot();
    }
}