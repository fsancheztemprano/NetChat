import chat.Server;
import java.net.UnknownHostException;

public class App {

    public static void main(String[] args) throws UnknownHostException {
        System.out.println("Hello World");
        Server.getInstance().startServer();

    }
}
