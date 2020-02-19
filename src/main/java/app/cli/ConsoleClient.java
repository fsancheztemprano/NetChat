package app.cli;

import app.core.Client;
import java.util.Scanner;

public class ConsoleClient {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username: ");
        String username = scanner.nextLine();

        String message = "";

        Client.inst().connect();
        while (!((message = scanner.nextLine()).equalsIgnoreCase("exit")) && Client.inst().isConnected()) {
            Client.inst().sendMessage(username, message);
        }
        new Thread(() -> Client.inst().disconnect()).start();

        System.out.println("END");
        System.exit(0);
    }

}
