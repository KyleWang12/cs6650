import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
  private static final String userName = "wyh";
  private static final String password = "wyhadmin";
  private static final String virtualHost = "/";
  private static final String hostName = "172.31.30.30";
  private static final int portNumber = 5672;
  private static final int MAX_NUM_THREADS = 150;
  private static final int NUM_TASK = 150;

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setUsername(userName);
    factory.setPassword(password);
    factory.setVirtualHost(virtualHost);
    factory.setHost(hostName);
    factory.setPort(portNumber);
    Connection connection = factory.newConnection();

    // <id, [num of left, num of right]>
    ConcurrentMap<Integer, List<Integer>> right = new ConcurrentHashMap<>();

    ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUM_THREADS);
    for (int i = 0; i < NUM_TASK; i++) {
      executorService.submit(new Receive(connection, right));
    }
  }
}
