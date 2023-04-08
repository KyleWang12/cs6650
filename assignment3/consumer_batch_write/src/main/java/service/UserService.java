package service;

import bean.Swipe;
import java.util.concurrent.ArrayBlockingQueue;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class UserService {

  public void swipe(String id, String targetId, int attitude, String comment) {
    try (Jedis jedis = RedisPoolFactory.getInstance().getResource()) {
      jedis.sadd(String.format("user:%s:%d", id, attitude), targetId);
      jedis.hset(String.format("user:%s:comment:%s", id, targetId), "comment", comment);
    }
  }

  public void swipeBatch(ArrayBlockingQueue<Swipe> swipeQueue) {
//    System.out.println("Processing swipe batch, size: " + swipeQueue.size());
    try (Jedis jedis = RedisPoolFactory.getInstance().getResource()) {
      Pipeline pipeline = jedis.pipelined();
      while (!swipeQueue.isEmpty()) {
        Swipe swipe = swipeQueue.poll();
//        System.out.println("Processing swipe: " + swipe);
        pipeline.sadd(String.format("user:%s:%d", swipe.getSwiper(), swipe.getAttitude()),
            swipe.getSwipee());
        pipeline.hset(String.format("user:%s:comment:%s", swipe.getSwiper(), swipe.getSwipee()),
            "comment", swipe.getComment());
      }
      pipeline.sync();
    }
  }
}
