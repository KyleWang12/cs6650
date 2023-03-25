package service;

import static constants.DatabaseConstants.*;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPoolFactory {
  private static JedisPool jedisPool;

  private RedisPoolFactory() {
  }

  public static JedisPool getInstance() {
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
}