package app.core;

public class AbstractSocketManager extends ActivableSocketManager {

    protected AbstractCommandProcessor commandProcessor;

    public AbstractCommandProcessor getCommandProcessor() {
        return commandProcessor;
    }

    protected void disableCommandProcessor() {
        if (commandProcessor != null)
            commandProcessor.setActive(false);
    }
}
