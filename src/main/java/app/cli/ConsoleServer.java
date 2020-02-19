package app.cli;

import app.core.Server;
import java.util.Scanner;

public class ConsoleServer {

    public static void main(String[] args) throws InterruptedException {
        Server.inst().startServer();
        Thread.sleep(1000);
        Scanner scanner = new Scanner(System.in);

        String userin = "";
        while (!userin.equalsIgnoreCase("exit") && Server.inst().isServerAlive()) {
            userin = scanner.nextLine();
            Server.inst().getServerManager().queueTransmission(userin);
        }
        new Thread(() -> Server.inst().stopServer()).start();

        System.out.println("END");
        System.exit(0);
    }

}
