package app.core.events;

public class ClientAlertEvent extends ClientEvent {

    public final String alertMessage;

    public ClientAlertEvent(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public String getAlertMessage() {
        return alertMessage;
    }
}
