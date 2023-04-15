import bean.Swipe;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import service.UserService;


public class Consumer implements Runnable {

  private ArrayBlockingQueue<Swipe> queue;
  private final Gson gson = new Gson();
  private final UserService userService = new UserService();
  public Consumer(ArrayBlockingQueue<Swipe> q) throws IOException {
    this.queue = q;
  }

  @Override
  public void run() {
    try {
      userService.swipeBatch(queue);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
