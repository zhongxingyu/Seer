 package ru.pilin.redis.runner.core;
 
 import java.io.IOException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.exceptions.JedisConnectionException;
 
 public class RedisServerRunner {
 
     private Logger log = LoggerFactory.getLogger(RedisServerRunner.class);
 
     private Runtime runtime = Runtime.getRuntime();
 
     private final Jedis jedis;
 
     private Process process;
 
     private String redisServerCmd = "/usr/local/bin/redis-server";
 
     public RedisServerRunner(String hostname, int port) {
         super();
         jedis = new Jedis(hostname, port);
     }
 
     public RedisServerRunner() {
         this("localhost", 6379);
     }
 
     public void start() {
         log.trace(".start()");
         try {
            String configFile = getClass().getResource("/redis-test.conf").getFile();
             log.debug("Starting Redis with config {}", configFile);
             process = runtime.exec(new String[] { redisServerCmd, configFile });
 //            TODO better way to wait for redis to start
             int tries = 0;
             while (true) {
                 tries ++;
                 try {
                     jedis.connect();
                     break;
                 } catch (JedisConnectionException e) {
                     if (tries < 10) {
                         try {
                             Thread.sleep(10);
                         } catch (InterruptedException e1) {
                             e1.printStackTrace();
                         }
                     } else {
                         throw new RedisServerExcpetion(e.getMessage(), e);
                     }
                 }
             }
             // TODO bind output stream to log
         } catch (IOException e) {
             throw new RedisServerExcpetion(e.getMessage(), e);
         }
     }
 
     // TODO: rename to shutdown
     public void stop() {
         log.trace(".stop()");
         try {
             String result = jedis.shutdown();
             log.debug("Shutdown returned: {}", result);
             process.waitFor();
         } catch (InterruptedException e) {
             throw new RedisServerExcpetion(e.getMessage(), e);
         }
     }
 
     public void setRedisServerCmd(String redisServerCmd) {
         this.redisServerCmd = redisServerCmd;
     }
 }
