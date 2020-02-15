package chat;

import chat.core.Client;
import java.util.Scanner;

public class ConsoleClient {

    public static void main(String[] args) {
        Client.getInstance().connect();
        Scanner scanner = new Scanner(System.in);

        String userin = "";
        while (!((userin = scanner.nextLine()).equalsIgnoreCase("exit")) && Client.getInstance().isConnected()) {
            Client.getInstance().sendMessage(userin);
        }
        new Thread(() -> Client.getInstance().disconnect()).start();

        System.out.println("END");
        System.exit(0);
    }

}
