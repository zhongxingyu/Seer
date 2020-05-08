 package com.git.broker.impl.util.jms;
 
 import com.git.broker.api.service.consumer.IConsumerService;
 import org.springframework.jms.listener.DefaultMessageListenerContainer;
 
 /**
 * Class description.
  * <p/>
  * User: dmgcodevil
  * Date: 12/6/12
  * Time: 4:22 PM
  */
 public class CustomMessageListenerContainer extends DefaultMessageListenerContainer {
 
     public String getMessageSelector() {
         IConsumerService consumerService = (IConsumerService) getMessageListener();
         return consumerService.getMessageSelector();
     }
 
 }
