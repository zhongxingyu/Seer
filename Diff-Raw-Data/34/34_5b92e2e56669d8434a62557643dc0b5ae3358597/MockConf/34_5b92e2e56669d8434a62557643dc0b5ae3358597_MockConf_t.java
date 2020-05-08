 package com.vreco.boomerang.conf;
 
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  *
  * @author Ben Aldrich
  */
 public class MockConf {
   public Conf conf;
   
   public MockConf() throws IOException {
     Properties prop = new Properties();
     prop.setProperty("mq.connection.url", "vm://localhost?broker.persistent=false");
     prop.setProperty("app.name", "boomerang");
 //    prop.setProperty("mq.connection.url", "tcp://localhost:61616?jms.prefetchPolicy.all=5");
     prop.setProperty("mq.connection.timeout","2000");
     prop.setProperty("mq.processing.queue", "boomerangQ");
     prop.setProperty("data.redis.url", "localhost");
     prop.setProperty("mq.response.queue", "boomerangResponseQ");
     prop.setProperty("boomerang.date.label", "boomDate");
     prop.setProperty("boomerang.uuid.label", "boomUuid");
     prop.setProperty("boomerang.retry.label", "boomRetry");
     prop.setProperty("boomerang.producer.label", "boomQueues");
     prop.setProperty("boomerang.resend.seconds", "60");
    prop.setProperty("boomerang.resend.default", "60000");
    prop.setProperty("boomerang.producer.ttl.default", "60000");
     conf = new Conf(prop);
   }
 
 }
