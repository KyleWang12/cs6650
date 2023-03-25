package service;

import redis.clients.jedis.Jedis;

public class UserService {

  public void swipe(String id, String targetId, int attitude, String comment) {
    try (Jedis jedis = RedisPoolFactory.getInstance().getResource()) {
      jedis.sadd(String.format("user:%s:%d", id, attitude), targetId);
      jedis.hset(String.format("user:%s:comment:%s", id, targetId), "comment", comment);
    }
  }
}
