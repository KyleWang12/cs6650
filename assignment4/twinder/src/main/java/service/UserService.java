package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import redis.clients.jedis.Jedis;

public class UserService {

  public Map<String, Set<String>> getMatches(String id) {
    Set<String> mutualLikes = new HashSet<>();
    try (Jedis jedis = RedisPoolFactory.getReadInstance().getResource()) {
      mutualLikes = jedis.smembers(String.format("user:%s:match", id));
    }
    Map<String, Set<String>> map = new HashMap<>();
    map.put("matchList", mutualLikes);
    return map;
  }

  public Map<String, Integer> getStats(String id) {
    int likesCount;
    int dislikesCount;
    try (Jedis jedis = RedisPoolFactory.getReadInstance().getResource()) {
      likesCount = jedis.scard("user:" + id + ":1").intValue();
      dislikesCount = jedis.scard("user:" + id + ":0").intValue();
    }
    Map<String, Integer> map = new HashMap<>();
    map.put("numLikes", likesCount);
    map.put("numDislikes", dislikesCount);
    return map;
  }
}
