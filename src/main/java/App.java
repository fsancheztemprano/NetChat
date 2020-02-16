import chat.core.Server;
import java.net.UnknownHostException;

public class App {

    public static void main(String[] args) throws UnknownHostException {
        System.out.println("Hello World");
        Server.inst().startServer();

    }
}
