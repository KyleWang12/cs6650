import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class Receive implements Runnable {

  private final Channel channel;
  private final DeliverCallback deliverCallback;
  private static final String QUEUE_NAME_1 = "swipe1";
  private static final String QUEUE_NAME_2 = "swipe2";
  private static final String EXCHANGE_NAME = "swipes";
  private ConcurrentMap<Integer, List<Integer>> right;

  public Receive(Connection connection, ConcurrentMap<Integer, List<Integer>> right) throws IOException {
    channel = connection.createChannel();
    this.right = right;
    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
    channel.queueDeclare(QUEUE_NAME_2, false, false, false, null);
    channel.queueBind(QUEUE_NAME_2, EXCHANGE_NAME, "");
    deliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      Gson gson = new Gson();
      Map map = gson.fromJson(message, Map.class);
      if(map.get("leftorright").toString().equals("right")){
        right.computeIfAbsent(Integer.parseInt(map.get("swiper").toString()), k -> new ArrayList<>()).add(Integer.parseInt(map.get("swipee").toString()));
      }
//      System.out.println(Thread.currentThread().getId() + " Received '" + message + "'");
    };
  }

  @Override
  public void run() {
    try {
      channel.basicConsume(QUEUE_NAME_2, true, deliverCallback, consumerTag -> { });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
