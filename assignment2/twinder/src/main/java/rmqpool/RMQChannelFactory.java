package rmqpool;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import java.io.IOException;

/**
 * A simple RabbitMQ channel factory based on the APche pooling libraries
 */
public class RMQChannelFactory extends BasePooledObjectFactory<Channel> {

  // Valid RMQ connection
  private final Connection connection;
  // used to count created channels for debugging
  private int count;
  private static final String QUEUE_NAME_1 = "swipe1";
  private static final String QUEUE_NAME_2 = "swipe2";
  private static final String EXCHANGE_NAME = "swipes";

  public RMQChannelFactory(Connection connection) {
    this.connection = connection;
    count = 0;
  }

  @Override
  synchronized public Channel create() throws IOException {
    count ++;
    Channel channel = connection.createChannel();
    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
    channel.queueDeclare(QUEUE_NAME_1, false, false, false, null);
    channel.queueDeclare(QUEUE_NAME_2, false, false, false, null);
    channel.queueBind(QUEUE_NAME_1, EXCHANGE_NAME, "");
    channel.queueBind(QUEUE_NAME_2, EXCHANGE_NAME, "");
    return channel;

  }

  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    //System.out.println("Wrapping channel");
    return new DefaultPooledObject<>(channel);
  }

  @Override
  synchronized public void destroyObject(PooledObject<Channel> p) throws Exception {
    count --;
    p.getObject().close();
  }

  public int getChannelCount() {
    return count;
  }
}
