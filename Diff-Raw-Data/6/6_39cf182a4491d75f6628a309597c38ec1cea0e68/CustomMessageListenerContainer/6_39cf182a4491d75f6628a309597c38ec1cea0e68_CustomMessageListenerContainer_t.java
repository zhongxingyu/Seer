 package com.git.broker.impl.util.jms;
 
 import com.git.broker.api.service.consumer.IConsumerService;
 import org.springframework.jms.listener.DefaultMessageListenerContainer;
 
 /**
 * CustomMessageListenerContainer.
  * <p/>
  * User: dmgcodevil
  * Date: 12/6/12
  * Time: 4:22 PM
  */
 public class CustomMessageListenerContainer extends DefaultMessageListenerContainer {
 
    /**
     * {@inheritDoc}
     */
     public String getMessageSelector() {
         IConsumerService consumerService = (IConsumerService) getMessageListener();
         return consumerService.getMessageSelector();
     }
 
 }
