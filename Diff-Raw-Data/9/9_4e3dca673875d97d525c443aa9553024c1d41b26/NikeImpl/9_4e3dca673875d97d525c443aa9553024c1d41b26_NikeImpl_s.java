 package edu.integration.patterns.manufacturer;
 
 import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.integration.Message;
 import org.springframework.integration.MessageChannel;
 import org.springframework.integration.core.PollableChannel;
 
 public class NikeImpl implements Nike, ApplicationContextAware {
 
     private static final Logger logger = Logger.getLogger(NikeImpl.class);
 	private ApplicationContext applicationContext;
     
     @Override
     public void receiverOrder() {
     	
 		PollableChannel orderChannel = (PollableChannel) applicationContext
 				.getBean("orderChannel", MessageChannel.class);
        logger.info("Sending order on channel: " + orderChannel);
         Message<?> message = (Message<?>) orderChannel.receive();
         Long itemId = (Long) message.getPayload();
         
         logger.info("Order received: " + message.getPayload());
         
         //Processing order for about 1 second
         try {
             Thread.sleep(1000);
         } catch (InterruptedException e) {
         }
         logger.info("Order processed: " + itemId);
     }
     
     @Override
     public void setApplicationContext(ApplicationContext applicationContext) {
         this.applicationContext = applicationContext;
     }
 
 }
