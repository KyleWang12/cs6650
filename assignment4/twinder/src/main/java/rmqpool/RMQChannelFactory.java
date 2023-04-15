package rmqpool;

import static constants.RmqConstants.*;
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

  private final Connection connection;
  private int count;

  public RMQChannelFactory(Connection connection) {
    this.connection = connection;
    count = 0;
  }

  @Override
  synchronized public Channel create() throws IOException {
    count ++;
    Channel channel = connection.createChannel();
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    return channel;

  }

  @Override
  public PooledObject<Channel> wrap(Channel channel) {
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
