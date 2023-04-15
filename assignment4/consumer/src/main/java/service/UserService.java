package service;

import bean.Swipe;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class UserService {

  public void swipe(String id, String targetId, int attitude, String comment) {
    try (Jedis jedis = RedisPoolFactory.getWriteInstance().getResource()) {
      jedis.sadd(String.format("user:%s:%d", id, attitude), targetId);
      jedis.hset(String.format("user:%s:comment:%s", id, targetId), "comment", comment);
    }
  }

  public void swipeBatch(ArrayBlockingQueue<Swipe> swipeQueue) {
    try (Jedis jedis = RedisPoolFactory.getWriteInstance().getResource()) {
      Pipeline pipeline = jedis.pipelined();
      Map<Swipe, Response<Boolean>> swipeResponses = new HashMap<>();

      while (!swipeQueue.isEmpty()) {
        Swipe swipe = swipeQueue.poll();
        if(swipe == null) {
          continue;
        }
        String swiperKey = String.format("user:%s:%d", swipe.getSwiper(), swipe.getAttitude());
        String swipeeKey = String.format("user:%s:1", swipe.getSwipee());

        pipeline.sadd(swiperKey, swipe.getSwipee());
        pipeline.hset(String.format("user:%s:comment:%s", swipe.getSwiper(), swipe.getSwipee()),
            "comment", swipe.getComment());
        Response<Boolean> swipeeHasSwipedSwiper = pipeline.sismember(swipeeKey, swipe.getSwiper());
        swipeResponses.put(swipe, swipeeHasSwipedSwiper);
      }

      pipeline.sync();

      for (Map.Entry<Swipe, Response<Boolean>> entry : swipeResponses.entrySet()) {
        Swipe swipe = entry.getKey();
        Response<Boolean> swipeeHasSwipedSwiper = entry.getValue();

        if (swipeeHasSwipedSwiper.get()) {
          pipeline.sadd(String.format("user:%s:match", swipe.getSwiper()), swipe.getSwipee());
          pipeline.sadd(String.format("user:%s:match", swipe.getSwipee()), swipe.getSwiper());
        }
      }

      pipeline.sync();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
