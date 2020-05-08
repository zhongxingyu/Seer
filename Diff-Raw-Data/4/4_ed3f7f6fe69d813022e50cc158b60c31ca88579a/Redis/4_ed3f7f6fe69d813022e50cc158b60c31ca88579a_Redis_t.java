 /*
  * Copyright 2012 Janrain, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.janrain.backplane.server.redis;
 
 import org.apache.log4j.Logger;
 import redis.clients.jedis.*;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @author Tom Raney
  */
 public class Redis {
 
     JedisPool pool;
     private static Redis instance;
 
     private Redis() {
         JedisPoolConfig config = new JedisPoolConfig();
         config.setMaxActive(100);
         config.setMaxIdle(100);
         config.setMinIdle(100);
 
         pool = new JedisPool(config, "localhost");
 
     }
 
     public synchronized static Redis getInstance() {
 
         if (instance == null) {
             instance = new Redis();
         }
         return instance;
     }
 
     /**
      *
      * @param lockName
      * @param identifier
      * @param waitInMilliSeconds if -1 loop forever
      * @param lockTimeSeconds
      * @return
      */
 
     public String getLock(String lockName, String identifier, int waitInMilliSeconds, int lockTimeSeconds) {
         Jedis jedis = pool.getResource();
         boolean loop = true;
 
         try {
             long end = System.currentTimeMillis() + waitInMilliSeconds;
 
             while (loop) {
                 if (jedis.setnx(lockName, identifier) == 1) {
                     jedis.expire(lockName, lockTimeSeconds);
                     return identifier;
                 } else if (jedis.ttl(lockName) == -1 ) {
                     jedis.expire(lockName, lockTimeSeconds);
                 }
                 Thread.sleep((int)(Math.random()*20));
                 if (waitInMilliSeconds < 0 ) {
                     loop = true;
                 } else {
                     loop = end > System.currentTimeMillis() ? true: false;
                 }
             }
         } catch (InterruptedException e) {
             logger.warn(e);
         } finally {
             pool.returnResource(jedis);
         }
         logger.warn("couldn't get lock '" + lockName + " with id " + identifier);
         return null;
     }
 
     /**
      * Be sure to return to pool!
      * @return
      */
 
     public Jedis getJedis() {
         return pool.getResource();
     }
 
     public void releaseToPool(Jedis jedis) {
         if (jedis != null) {
             pool.returnResource(jedis);
         }
     }
 
     public boolean releaseLock(String lockName, String identifier) {
         Jedis jedis = pool.getResource();
 
         try {
             while (true) {
                 jedis.watch(lockName);
                 if (identifier.equals(jedis.get(lockName))) {
                     Transaction t = jedis.multi();
                     t.del(lockName);
 
                     if (t.exec() == null) {
                         jedis.unwatch();
                         continue;
                     } else {
                         return true;
                     }
                 } else {
                     // lost lock
                     return false;
                 }
             }
         } finally {
             pool.returnResource(jedis);
         }
     }
 
     public boolean refreshLock(String lockName, String identifier, int lockTimeSeconds) {
         Jedis jedis = pool.getResource();
 
         try {
             // refresh lock
             byte[] currentIdentifier = jedis.get(lockName.getBytes());
 
            if (currentIdentifier != null && new String(currentIdentifier).equals(identifier)) {
                jedis.expire(lockName, lockTimeSeconds);
                 return true;
             }
         } finally {
             pool.returnResource(jedis);
         }
         logger.warn("lock " + lockName + " with identifier " + identifier + " is no longer current");
         return false;
     }
 
     public void set(byte[] key, byte[] value) {
         Jedis jedis = pool.getResource();
 
         try {
             jedis.set(key,value);
         } finally {
             pool.returnResource(jedis);
         }
     }
 
     public void set(byte[] key, byte[] value, int seconds) {
         Jedis jedis = pool.getResource();
         try {
             jedis.set(key,value);
             jedis.expire(key, seconds);
         } finally {
             pool.returnResource(jedis);
         }
     }
 
     public void append(byte[] key, byte[] value) {
         Jedis jedis = pool.getResource();
         try {
             jedis.append(key, value);
         } finally {
             pool.returnResource(jedis);
         }
     }
 
     public Long rpush(final byte[] key, final byte[] string) {
         Jedis jedis = pool.getResource();
         try {
             return jedis.rpush(key, string);
         } finally {
             pool.returnResource(jedis);
         }
     }
 
     public long llen(byte[] key) {
         Jedis jedis = pool.getResource();
         try {
             return jedis.llen(key);
         } finally {
             pool.returnResource(jedis);
         }
     }
 
     public byte[] get(byte[] key) {
         Jedis jedis = pool.getResource();
         try {
             return jedis.get(key);
         } finally {
             pool.returnResource(jedis);
         }
     }
 
     public List<byte[]> mget(byte[]... keys) {
         Jedis jedis = pool.getResource();
         try {
             return jedis.mget(keys);
         } finally {
             pool.returnResource(jedis);
         }
     }
 
 
 
     public byte[] lpop(byte[] key) {
         Jedis jedis = pool.getResource();
         try {
             return jedis.lpop(key);
         } finally {
             pool.returnResource(jedis);
         }
     }
 
     public List<byte[]> lrange(final byte[] key, final int start, final int end) {
         Jedis jedis = pool.getResource();
         try {
             return jedis.lrange(key, start, end);
         } finally {
             pool.returnResource(jedis);
         }
     }
 
     public Set<byte[]> zrangebyscore(final byte[] key, double min, double max) {
         Jedis jedis = pool.getResource();
         try {
             return jedis.zrangeByScore(key, min, max);
         } finally {
             pool.returnResource(jedis);
         }
     }
 
     // PRIVATE
 
     private static final Logger logger = Logger.getLogger(Redis.class);
 
 
 }
