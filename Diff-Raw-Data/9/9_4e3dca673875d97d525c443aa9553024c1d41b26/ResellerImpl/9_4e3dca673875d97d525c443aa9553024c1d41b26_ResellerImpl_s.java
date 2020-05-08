 package edu.integration.patterns.reseller;
 
 import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.integration.Message;
 import org.springframework.integration.MessageChannel;
 import org.springframework.integration.support.MessageBuilder;
 
 public class ResellerImpl implements Reseller, ApplicationContextAware {
 
     private ApplicationContext applicationContext;
 
     private static final Logger logger = Logger.getLogger(ResellerImpl.class);
 
     @Override
     public void buy(long itemId) {
         Message<Long> order = MessageBuilder.withPayload(itemId).build();
         logger.info("Order message created");
         MessageChannel orderChannel = applicationContext.getBean("orderChannel",
                 MessageChannel.class);
         logger.info("Sending order on channel: " + orderChannel);
         orderChannel.send(order);
         logger.info("Order message sent on channel: " + orderChannel);
 
     }
 
     @Override
     public void setApplicationContext(ApplicationContext applicationContext) {
         this.applicationContext = applicationContext;
     }
 }
