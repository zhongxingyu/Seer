 package swag49.messaging;
 
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.integration.annotation.ServiceActivator;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 import swag49.dao.DataAccessObject;
 import swag49.model.Message;
 import swag49.util.Log;
 
 import java.util.Date;
 
 @Component("emailMessageHandler")
 public class EmailMessageHandler implements MessageReceiver {
     @Log
     private Logger logger;
 
     @Autowired @Qualifier("messageDAO")
     private DataAccessObject<Message> messageDAO;
 
     @ServiceActivator
     @Transactional
     public Message handleMessage(Message message) {
        logger.info("Sending email with content %s", message.getContent());
         message.setReceiveDate(new Date());
 
         messageDAO.update(message);
 
         return message;
     }
 }
