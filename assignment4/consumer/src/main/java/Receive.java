import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;import java.util.Map;
import service.UserService;


public class Receive implements Runnable {

  private final Connection connection;

  private final UserService userService = new UserService();
  private final Gson gson = new Gson();
  private static final String QUEUE_NAME = "swipe";

  public Receive(Connection connection) throws IOException {
    this.connection = connection;
  }

  @Override
  public void run() {
    try {
      Channel channel = connection.createChannel();
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
          Map map = gson.fromJson(message, Map.class);
          int leftorright = map.get("leftorright").toString().equals("left") ? 0 : 1;
          try {
            userService.swipe(map.get("swiper").toString(), map.get("swipee").toString(), leftorright, map.get("comment").toString());
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          } catch (Exception e) {
            e.printStackTrace();
          }
      };
      channel.basicQos(1);
      channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
