package app.ui;

import app.core.ClientFacade;
import app.core.events.ClientLoginResponseEvent;
import app.core.events.ClientPmEvent;
import app.core.events.ClientUserListEvent;
import app.core.events.SocketStatusEvent;
import com.google.common.eventbus.Subscribe;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener.Change;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import tools.Asserts;
import tools.fx.FxDialogs;

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
    private Tab tabLogin;

    @FXML
    private TextField fieldLoginUsername;

    @FXML
    private PasswordField fieldLoginPassword;

    @FXML
    public Button btnLogOut;

    @FXML
    private Button btnLogin;

    @FXML
    private Tab tabChat;

    @FXML
    private BorderPane chatMenuPane;

    @FXML
    private Button btnNavGroups;

    @FXML
    private Button btnNavChats;

    @FXML
    private TabPane tabPaneGroups;

    @FXML
    private Tab tabGroupsList;

    @FXML
    private ListView<String> listViewGroups;

    @FXML
    private Button btnGroupsRemove;

    @FXML
    private Button btnGroupsNew;

    @FXML
    private Button btnGroupsEnter;

    @FXML
    private TabPane tabPaneChats;

    @FXML
    private Tab tabUsersList;

    @FXML
    private Button btnOpenChat;

    @FXML
    private ListView<String> listViewUsers;

    private final ObservableSet<String> usersObservableSet = FXCollections.observableSet();

    private final HashMap<String, ChatControl> openChats = new HashMap<>();

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
        assert tabLogin != null : "fx:id=\"tabLogin\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert fieldLoginUsername != null : "fx:id=\"fieldLoginUsername\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert fieldLoginPassword != null : "fx:id=\"fieldLoginPassword\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert btnLogin != null : "fx:id=\"btnLogin\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert tabChat != null : "fx:id=\"tabChat\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert chatMenuPane != null : "fx:id=\"chatMenuPane\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert btnNavGroups != null : "fx:id=\"btnNavGroups\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert btnNavChats != null : "fx:id=\"btnNavChats\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert tabPaneGroups != null : "fx:id=\"tabPaneGroups\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert tabGroupsList != null : "fx:id=\"tabGroupsList\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert listViewGroups != null : "fx:id=\"listViewGroups\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert btnGroupsRemove != null : "fx:id=\"btnGroupsRemove\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert btnGroupsNew != null : "fx:id=\"btnGroupsNew\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert btnGroupsEnter != null : "fx:id=\"btnGroupsEnter\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert tabPaneChats != null : "fx:id=\"tabPaneChats\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert tabUsersList != null : "fx:id=\"tabUsersList\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert btnOpenChat != null : "fx:id=\"btnChatPM\" was not injected: check your FXML file 'ClientPane.fxml'.";
        assert listViewUsers != null : "fx:id=\"listViewChats\" was not injected: check your FXML file 'ClientPane.fxml'.";

        chatMenuPane.setLeft(null);
        chatMenuPane.setRight(null);
        chatMenuPane.setCenter(tabPaneChats);
        tabLogin.setDisable(true);
        tabChat.setDisable(true);

        listViewUsers.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listViewGroups.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        usersObservableSet.addListener((Change<? extends String> c) -> {
            if (c.wasAdded()) {
                listViewUsers.getItems().add(c.getElementAdded());
            }
            if (c.wasRemoved()) {
                listViewUsers.getItems().remove(c.getElementRemoved());
            }
        });

        ClientFacade.inst().setListener(this);
    }

    @FXML
    void btnOpenChatAction(ActionEvent event) {
        String selectedUser = listViewUsers.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            activateTab(selectedUser);
        }

    }

    private void activateTab(String selectedUser) {
        Tab tab;
        if (openChats.containsKey(selectedUser)) {
            tab = openChats.get(selectedUser).getTab();
        } else {
            ChatControl userChat = new ChatControl(selectedUser);
            tab = userChat.getTab();
            tab.setOnCloseRequest((a) -> openChats.remove(userChat.getUsername()));
            openChats.put(selectedUser, userChat);
            Platform.runLater(() -> tabPaneChats.getTabs().add(tab));

        }
        tabPaneChats.getSelectionModel().select(tab);
    }

    @FXML
    void btnConnectAction(ActionEvent event) {
        if (Asserts.isValidPort(txtFieldPort.getText())) {
            ClientFacade.inst().setHostname(txtFieldIP.getText());
            ClientFacade.inst().setPort(Integer.parseInt(txtFieldPort.getText()));
            ClientFacade.inst().connect();
        } else
            logOutput("Puerto invalido");
    }

    @FXML
    void btnDisconnectAction(ActionEvent event) {
        ClientFacade.inst().disconnect();
    }

    @FXML
    void btnExitAction(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }


    @FXML
    void btnGroupsEnterAction(ActionEvent event) {

    }

    @FXML
    void btnGroupsNewAction(ActionEvent event) {

    }

    @FXML
    void btnGroupsRemoveAction(ActionEvent event) {

    }

    @FXML
    void btnLoginAction(ActionEvent event) {
        String username = fieldLoginUsername.getText();
        String password = fieldLoginPassword.getText();
        if (username.length() < 4 || password.length() < 4) {
            FxDialogs.showError("User Pass Error", "User / Pass too short");
            return;
        }
        ClientFacade.inst().login(username, password);
    }

    @FXML
    void btnNavChatsAction(ActionEvent event) {
        chatMenuPane.setCenter(tabPaneChats);
        ClientFacade.inst().requestUserList();
    }

    @FXML
    void btnNavGroupsAction(ActionEvent event) {
        chatMenuPane.setCenter(tabPaneGroups);
        ClientFacade.inst().requestGroupList();
    }

    @FXML
    void btnLogOutAction(ActionEvent actionEvent) {
        ClientFacade.inst().logout();
        cleanUpChatPane();
    }

    private void cleanUpChatPane() {
        Platform.runLater(() -> {
            tabChat.setDisable(true);
            usersObservableSet.clear();
//            listViewGroups.getItems().clear();
            tabPaneChats.getTabs().remove(1, tabPaneChats.getTabs().size());
            tabPaneGroups.getTabs().remove(1, tabPaneGroups.getTabs().size());
        });
    }

    @Subscribe
    void logOutput(String log) {
        Platform.runLater(() -> txtAreaClientLog.appendText(log + "\n"));
    }

    @Subscribe
    public void loginResponseReceived(ClientLoginResponseEvent authResponseEvent) {
        if (authResponseEvent.getAuth() == -1) {
            Platform.runLater(() -> {
                FxDialogs.showError("Auth Failed", "Auth Failed", "Auth Failed");
                tabChat.setDisable(true);
            });
        } else {
            logOutput("Login Success");
            Platform.runLater(() -> {
//                FxDialogs.showError("Welcome", "Auth Success", "Login Correct");
                tabChat.setDisable(false);
                tabPane.getSelectionModel().select(tabChat);
            });
        }
    }

    @Subscribe
    public void socketStatusChanged(SocketStatusEvent socketStatusEvent) {
        if (socketStatusEvent.isActive()) {
            Platform.runLater(() -> {
                circleClientStatus.setFill(Color.LIMEGREEN);
                btnConnect.setDisable(true);
                tabLogin.setDisable(false);

                tabPane.getSelectionModel().select(tabLogin);
            });
        } else {
            Platform.runLater(() -> {
                circleClientStatus.setFill(Color.ORANGERED);
                btnConnect.setDisable(false);
                tabLogin.setDisable(true);
                cleanUpChatPane();
                tabPane.getSelectionModel().select(tabConnect);
            });
        }
    }

    @Subscribe
    public void userListReceived(ClientUserListEvent userListEvent) {
        Platform.runLater(() -> {
            List<String> list = Arrays.asList(userListEvent.getUserList());
            usersObservableSet.addAll(list);
            usersObservableSet.retainAll(list);
        });
    }

    @Subscribe
    public void pmReceived(ClientPmEvent pmEvent) {
        String tabKey;
        if (pmEvent.isAck()) {
            tabKey = pmEvent.getDestiny();
        } else {
            tabKey = pmEvent.getOrigin();
        }
        activateTab(tabKey);
        ChatControl chatControl = openChats.get(tabKey);
        chatControl.newMessage(pmEvent.getOrigin(), pmEvent.getMessage());
    }
}
