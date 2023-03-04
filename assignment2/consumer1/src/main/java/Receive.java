import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class Receive implements Runnable {

  private final Channel channel;
  private final DeliverCallback deliverCallback;
  private static final String QUEUE_NAME_1 = "swipe1";
  private static final String EXCHANGE_NAME = "swipes";
  private ConcurrentMap<Integer, int[]> cnt;

  public Receive(Connection connection, ConcurrentMap<Integer, int[]> cnt) throws IOException {
    channel = connection.createChannel();
    this.cnt = cnt;
    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
    channel.queueDeclare(QUEUE_NAME_1, false, false, false, null);
    channel.queueBind(QUEUE_NAME_1, EXCHANGE_NAME, "");
    deliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      Gson gson = new Gson();
      Map map = gson.fromJson(message, Map.class);
      int leftorright = map.get("leftorright").toString().equals("left") ? 0 : 1;
      cnt.computeIfAbsent(Integer.parseInt(map.get("swiper").toString()), k -> new int[]{0, 0})[leftorright]++;
//      System.out.println(Thread.currentThread().getId() + " Received '" + message + "'");
    };
  }

  @Override
  public void run() {
    try {
      channel.basicConsume(QUEUE_NAME_1, true, deliverCallback, consumerTag -> { });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
