import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static constants.RmqConstants.*;

public class Client {

  private static final int NUM_TASK = 256;

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setUsername(userName);
    factory.setPassword(password);
    factory.setVirtualHost(virtualHost);
    factory.setHost(hostName);
    factory.setPort(portNumber);

    Connection connection = factory.newConnection();

    ExecutorService executorService = Executors.newCachedThreadPool();
    System.out.println("Start listening");
    for (int i = 0; i < NUM_TASK; i++) {
      executorService.submit(new Receive(connection));
    }
  }
}
