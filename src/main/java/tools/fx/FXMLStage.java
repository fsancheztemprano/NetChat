//package tools.fx;
//
//import java.io.IOException;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.layout.Pane;
//import javafx.stage.Modality;
//import javafx.stage.Stage;
//
//public class FXMLStage extends Stage {
//
//    private FXMLLoader fxmlLoader = new FXMLLoader();
//
//
//    private FXMLStage(String title) {
//        setTitle(title);
//        initModality(Modality.NONE);
//        initOwner(FxApplication.getMainStage());
//    }
//
//    public FXMLStage(String fxml, String title) {
//        this(title);
//        try {
//            fxmlLoader.setLocation(getClass().getResource(fxml));
//            Parent root = fxmlLoader.load();
//            setScene(new Scene(root));
//            root.requestFocus();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public FXMLLoader getFxmlLoader() {
//        return fxmlLoader;
//    }
//
//    public Pane getRootPane() {
//        return (Pane) getActiveScene().getRoot();
//    }
//
//    public Scene getActiveScene() {
//        return this.getScene();
//    }
//
//    public <T> T getController() {
//        return fxmlLoader.getController();
//    }
//}
