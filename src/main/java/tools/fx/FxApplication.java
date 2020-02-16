package tools.fx;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public abstract class FxApplication extends Application {

    protected static Stage mainStage;

    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static Scene getActiveScene() {
        return getMainStage().getScene();
    }

    public static void setActiveScene(Scene activeScene) {
        getMainStage().setScene(activeScene);
    }

    public static <T extends Parent> void setPaneWithNewScene(T pane) {
        setActiveScene(new Scene(pane));
    }

    public static <T extends Region> void setPaneOnActiveScene(T pane) {
        getActiveScene().setRoot(pane);
    }

    public static Pane getRootPane() {
        return (Pane) getActiveScene().getRoot();
    }
}