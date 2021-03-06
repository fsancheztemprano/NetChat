package app.ui;

import app.core.ClientFacade;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import tools.log.Flogger;

public class ChatControl extends AbstractChatControl {


    {
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ChatPane.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (final IOException e) {
            Flogger.atSevere().withCause(e).log("ER-UI-CC-0001");
        }
    }

    public ChatControl(String title) {
        super(title);
    }

    @FXML
    @Override
    protected void initialize() {
        super.initialize();
    }

    @FXML
    @Override
    public void sendMessageAction(ActionEvent event) {
        String draft = fieldMessageDraft.getText().trim();
        if (draft.length() > 0)
            ClientFacade.inst().sendPrivateMessage(title, draft);
    }

}
