 package de.blogspot.wrongtracks.prost.msg.impl;
 
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.annotation.Resource;
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageProducer;
 import javax.jms.Session;
 import javax.jms.Topic;
 import javax.naming.InitialContext;
 
 import de.blogspot.wrongtracks.prost.msg.api.MessageToClientService;
 
 public class MessageToClientServiceImpl implements MessageToClientService {
 
 	@Resource(name = "ConnectionFactory")
 	private ConnectionFactory factory;
 	@Resource(mappedName = "java:/prost/topic/gui/update")
 	private Topic topic;
 	private static final Logger logger = Logger
 			.getLogger(MessageToClientServiceImpl.class.getName());
 	private Properties props = new Properties();
 	private static final String CONNECTION_USER = "connectionUser";
 	private static final String CONNECTION_PWD = "connectionPwd";
 	private static Connection connection;
 
 	public void init() {
 		try {
 			InitialContext context = new InitialContext();
 			factory = (ConnectionFactory) context.lookup("java:/ConnectionFactory");
			props.load(this.getClass()
					.getResourceAsStream("/connection.properties"));
 			connection = factory.createConnection("prostMessaging",
 					"messaging0!");
 			connection.start();
 			topic = (Topic) context.lookup("java:/prost/topic/gui/update");
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void destroy() {
 		try {
 			connection.stop();
 			connection.close();
 		} catch (JMSException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void sendGuiUpdateMessage() {
 		try {
 			Session session = connection.createSession(false,
 					Session.AUTO_ACKNOWLEDGE);
 			MessageProducer messageProducer = session.createProducer(topic);
 			Message message = session.createMessage();
 			logger.log(Level.FINE, "Sending message: " + message.toString()
 					+ " with ID: " + message.getJMSMessageID() + " and topic: "
 					+ topic.getTopicName());
 			messageProducer.send(message);
 			messageProducer.close();
 			session.close();
 		} catch (JMSException e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 }
