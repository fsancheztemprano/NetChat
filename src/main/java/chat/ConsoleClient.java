package chat;

import chat.core.Client;
import java.util.Scanner;

public class ConsoleClient {

    public static void main(String[] args) {
        Client.getInstance().connect();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            Client.getInstance().sendMessage(scanner.nextLine());

        }
    }

}
