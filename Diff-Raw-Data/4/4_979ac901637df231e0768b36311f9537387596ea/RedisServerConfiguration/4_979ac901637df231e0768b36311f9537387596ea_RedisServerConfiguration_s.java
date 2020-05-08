 package ru.pilin.redis.runner.core;
 
 import java.io.File;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public final class RedisServerConfiguration {
 
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private final String redisServerCmd;
 
     private final File redisConf;
 
     private final String hostname;
 
     private final int port;
 
     public RedisServerConfiguration() {
         this("/usr/local/bin/redis-server", "localhost", 6379);
     }
 
     public RedisServerConfiguration(String redisServerCmd, String hostname,
         int port) {
         this(redisServerCmd,
             new File(RedisServerConfiguration.class
            .getResource("/redis-test.conf").getFile()),
             hostname, port);
     }
 
     public RedisServerConfiguration(String redisServerCmd, File redisConf,
             String hostname, int port) {
         super();
         this.redisServerCmd = redisServerCmd;
         this.redisConf = redisConf;
         this.hostname = hostname;
         this.port = port;
        log.debug("Starting Redis with config {}", this.redisConf);
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result
             + ((hostname == null) ? 0 : hostname.hashCode());
         result = prime * result + port;
         result = prime * result
             + ((redisServerCmd == null) ? 0 : redisServerCmd.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         RedisServerConfiguration other = (RedisServerConfiguration) obj;
         if (hostname == null) {
             if (other.hostname != null)
                 return false;
         } else if (!hostname.equals(other.hostname))
             return false;
         if (port != other.port)
             return false;
         if (redisServerCmd == null) {
             if (other.redisServerCmd != null)
                 return false;
         } else if (!redisServerCmd.equals(other.redisServerCmd))
             return false;
         return true;
     }
 
     @Override
     public String toString() {
         return "RedisServerConfiguration [redisServerCmd=" + redisServerCmd
             + ", hostname=" + hostname + ", port=" + port + "]";
     }
 
     public String getRedisServerCmd() {
         return redisServerCmd;
     }
 
     public File getRedisConf() {
         return redisConf;
     }
 
     public String getHostname() {
         return hostname;
     }
 
     public int getPort() {
         return port;
     }
 }
