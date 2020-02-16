package chat;

import chat.core.Client;
import java.util.Scanner;

public class ConsoleClient {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username: ");
        String username = scanner.nextLine();

        String message = "";

        Client.getInstance().connect();
        while (!((message = scanner.nextLine()).equalsIgnoreCase("exit")) && Client.getInstance().isConnected()) {
            Client.getInstance().sendMessage(username, message);
        }
        new Thread(() -> Client.getInstance().disconnect()).start();

        System.out.println("END");
        System.exit(0);
    }

}
