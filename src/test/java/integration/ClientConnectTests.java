package integration;

import app.core.ClientNodeManager;
import app.core.ServerFacade;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientConnectTests {

    @BeforeClass
    public static void serverStartUp() {
        ServerFacade.inst().startServer();
    }

    @Test
    public void whenClientConnect_socketShouldConnect() {
        ClientNodeManager client = new ClientNodeManager();
        client.startSocketManager();
        Assert.assertTrue(client.isSocketOpen());
    }

    @AfterClass
    public static void serverShutDown() {
        ServerFacade.inst().stopServer();
    }

}
