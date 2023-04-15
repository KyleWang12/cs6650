package service;

import static constants.DatabaseConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPoolFactory {
  private static JedisPool jedisPool;
  private static List<JedisPool> jedisReadPools;
  private static AtomicInteger replicaIndex = new AtomicInteger(0);

  private RedisPoolFactory() {
  }

  public static JedisPool getWriteInstance() {
    if (jedisPool == null) {
      synchronized (RedisPoolFactory.class) {
        if (jedisPool == null) {
          JedisPoolConfig poolConfig = new JedisPoolConfig();
          poolConfig.setMaxTotal(1024);
          jedisPool = new JedisPool(poolConfig, host, port, timeout, password);
        }
      }
    }
    return jedisPool;
  }

  public static JedisPool getReadInstance() {
    if (jedisReadPools == null) {
      synchronized (RedisPoolFactory.class) {
        if (jedisReadPools == null) {
          JedisPoolConfig poolConfig = new JedisPoolConfig();
          poolConfig.setMaxTotal(1024);
          jedisReadPools = new ArrayList<>();
          for (int i = 0; i < replicaHosts.length; i++) {
            JedisPool replicaPool = new JedisPool(poolConfig, replicaHosts[i], port, timeout, replicaPass);
            jedisReadPools.add(replicaPool);
          }
        }
      }
    }
    return getReadReplicaPool();
  }

  private static JedisPool getReadReplicaPool() {
    int nextIndex = replicaIndex.getAndUpdate(currentIndex -> (currentIndex + 1) % jedisReadPools.size());
    return jedisReadPools.get(nextIndex);
  }
}