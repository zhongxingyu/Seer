 /**
  * 
  */
 package ch.demo.business.service;
 
 import javax.annotation.Resource;
 import javax.ejb.MessageDriven;
 import javax.ejb.MessageDrivenContext;
 import javax.inject.Inject;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.TextMessage;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.slf4j.Logger;
 
 /**
  * @author hostettler
  * 
  */
 @MessageDriven(mappedName = "MyQueue")
 public class StudentRegistrationService implements MessageListener {
 
 	/**
 	 * The logger for the class. The logger is produced by the LoggerProducer
 	 * and then injected here
 	 */
 	@Inject
 	private transient Logger logger;
 
 	@PersistenceContext
 	EntityManager em;
 
 	@Resource
 	private MessageDrivenContext mdbContext;
 
 	public void onMessage(Message inMessage) {
 		TextMessage msg = null;
 		try {
 			if (inMessage instanceof TextMessage) {
 				msg = (TextMessage) inMessage;
				logger.info("MESSAGE BEAN: Message received: " + msg.getText());
 				logger.info(Thread.currentThread().getName());
 			} else {
				logger.warn("Message of wrong type: "
						+ inMessage.getClass().getName());
 			}
 		} catch (JMSException e) {
 			mdbContext.setRollbackOnly();
 			em.getTransaction().rollback();
 			throw new RuntimeException(e);
 		}
 	}
 }
