import bean.Swipe;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    ScheduledExecutorService ses = Executors.newScheduledThreadPool(2);
    ArrayBlockingQueue<Swipe> queue = new ArrayBlockingQueue<>(65536);
    System.out.println("Start listening");
    for (int i = 0; i < NUM_TASK; i++) {
      executorService.submit(new Producer(connection, queue));
    }
    for (int i = 0; i < 2; i++) {
      ses.scheduleAtFixedRate(new Consumer(queue), 0, 2, TimeUnit.SECONDS);
    }

  }
}
