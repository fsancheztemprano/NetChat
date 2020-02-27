package app;

import app.core.ServerFacade;
import java.util.Scanner;

public class ServerCli {

    public static void main(String[] args) throws InterruptedException {
        ServerFacade.inst().startServer();
        Thread.sleep(1000);
        Scanner scanner = new Scanner(System.in);

        String userin = "";
        while (!userin.equalsIgnoreCase("exit") && ServerFacade.inst().isServerAlive()) {
            userin = scanner.nextLine();
            ServerFacade.inst().getServerManager().queueServerBroadcast(userin);
        }
        new Thread(() -> ServerFacade.inst().stopServer()).start();

        System.out.println("END");
        System.exit(0);
    }

}
