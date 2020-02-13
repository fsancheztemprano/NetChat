package chat;

import chat.core.Server;
import java.util.Scanner;

public class ConsoleServer {

    public static void main(String[] args) throws InterruptedException {
        Server.getInstance().startServer();
        Thread.sleep(1000);
        Scanner scanner = new Scanner(System.in);

        String userin = "";
        while (!userin.equalsIgnoreCase("exit") && Server.getInstance().isServerAlive()) {
            userin = scanner.nextLine();
        }
        Server.getInstance().stopServer();
        System.out.println("END");
        System.exit(0);
    }

}
