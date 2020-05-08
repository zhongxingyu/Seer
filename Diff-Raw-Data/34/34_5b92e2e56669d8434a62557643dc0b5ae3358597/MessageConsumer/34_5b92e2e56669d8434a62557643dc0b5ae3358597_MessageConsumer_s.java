 package com.vreco.boomerang;
 
 import com.vreco.boomerang.conf.Conf;
 import com.vreco.boomerang.datastore.DataStore;
 import com.vreco.boomerang.datastore.RedisStore;
 import com.vreco.boomerang.message.Message;
 import com.vreco.util.mq.Consumer;
 import com.vreco.util.mq.Producer;
 import com.vreco.util.shutdownhooks.SimpleShutdown;
 import java.io.IOException;
 import java.util.HashMap;
 import javax.jms.JMSException;
 import javax.jms.TextMessage;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Ben Aldrich
  */
 public class MessageConsumer implements Runnable {
 
   final SimpleShutdown shutdown = SimpleShutdown.getInstance();
   final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
   final ObjectMapper mapper = new ObjectMapper();
   final Conf conf;
   DataStore store;
 
   public MessageConsumer(Conf conf) {
     this.conf = conf;
   }
 
   @Override
   public void run() {
     try (Consumer consumer = new Consumer(conf.getValue("mq.connection.url"));
             Producer producer = new Producer(conf.getValue("mq.connection.url"))) {
       store = new RedisStore(conf);
       consumer.setTimeout(conf.getLongValue("mq.connection.timeout", Long.parseLong("2000")));
       consumer.connect("queue", conf.getValue("mq.processing.queue"));
       while (!shutdown.isShutdown()) {
         TextMessage mqMsg = consumer.getTextMessage();
         if (mqMsg != null) {
           try {
             Message msg = getMessage(mqMsg.getText());
             store.set(msg);
             producer.connect("queue", msg.getDestination());
             producer.sendMessage(mapper.writeValueAsString(msg.getMsg()));
             mqMsg.acknowledge();
           } catch (JMSException | IOException e) {
             System.out.print(e.getCause().toString());
           }
         } else {
           logger.debug("No messages on queue...");
         }
       }
     } catch (JMSException e) {
       logger.error("Failed to process message", e);
     }
 
   }
 
   /**
    *
    * @param mqMsg
    * @return
    */
   protected Message getMessage(final String mqMsg) throws JsonParseException, JsonMappingException, IOException {
     HashMap<String, Object> hMsg = mapper.readValue(mqMsg, HashMap.class);
     return new Message(hMsg, conf);
   }
 }
