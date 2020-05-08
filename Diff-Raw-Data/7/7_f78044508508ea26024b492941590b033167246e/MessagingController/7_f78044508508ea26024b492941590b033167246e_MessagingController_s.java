 package com.redhat.salab.messaging;
 
 import java.security.Security;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.UUID;
 
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageProducer;
 import javax.jms.Session;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import org.hornetq.jms.client.HornetQConnectionFactory;
 import org.jboss.sasl.JBossSaslProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class MessagingController {
 	private static final Logger log = LoggerFactory.getLogger(MessagingController.class);
 	
 	static {
 		Security.addProvider(new JBossSaslProvider());
 	}
 	
 	private AppSettings settings;
 	private MessagingContext lastContext;
 	private List<MessagingContext> producerContexts = new ArrayList<MessagingContext>();
 	private List<MessagingContext> consumerContexts = new ArrayList<MessagingContext>();
 	
 	public MessagingController(AppSettings settings) {
 		this.settings = settings;
 	}
 	
 	public List<MessagingContext> getProducerContexts() {
 		return producerContexts;
 	}
 	
 	public List<MessagingContext> getConsumerContexts() {
 		return consumerContexts;
 	}
 	
 	
 	public void execute() {		
 		createMessagingThreads();
 		
 		while(!isAllDone()) {
 			sleep(1000L);
 		}
 		
 		cleanupAll();
 	}
 	
 	private void createMessagingThreads() {
 		try {
 			for(int i=0; i < settings.getProducerCount(); i++) {
 				MessagingContext producer = createProducer(i);
 				producerContexts.add(producer);
 			}
 			
 			for(int i=0; i < settings.getConsumerCount(); i++) {
 				MessagingContext consumer = createConsumer(i);
 				consumerContexts.add(consumer);
 			}
 			
 		} catch (NamingException e) {
 			log.error("Failed to connect to naming context", e);
 			throw new RuntimeException("Failed to connect to naming context");
 		} catch (JMSException e) {
 			log.error("Failed to configure JMS", e);
 			throw new RuntimeException("Failed to connect to naming context");
 		}
 	}
 	
 	private MessagingContext createProducer(int index) throws NamingException, JMSException {
 		MessagingContext context = new MessagingContext();
 		String id = String.format("Producer[%d, %s]", index, UUID.randomUUID().toString());
 		context.setContextID(id);
 		context.setProducer(true); 
 
 		// Create, or reuse, the Naming Context, and JMS ConnectionFactory / Connection / Destination / Session
 		configureJMS(context);				
 		
 		// Create the producer
 		MessageProducer producer = context.getSession().createProducer(context.getDestination());
 		context.setMessageProducer(producer);
 		
 		// Create the producer thread
 		MessagingThread producerThread = new MessagingThread(settings, context);
 		Thread thread = new Thread(producerThread, context.getContextID());
 		thread.start();
 		
 		log.info("Started producer {}", context.getContextID());
 		
 		lastContext = context;
 		return context;
 	}
 	
 	private MessagingContext createConsumer(int index) throws NamingException, JMSException {
 		MessagingContext context = new MessagingContext();
 		String id = String.format("Consumer[%d, %s]", index, UUID.randomUUID().toString());
 		context.setContextID(id);
 		context.setConsumer(true);
 		
 		// Create, or reuse, the Naming Context, and JMS ConnectionFactory / Connection / Destination / Session
 		configureJMS(context);				
 		
 		// Create the consumer
 		MessageConsumer consumer = context.getSession().createConsumer(context.getDestination());
 		context.setMessageConsumer(consumer);
 		
 		// Consumer connections must be started
 		context.getConnection().start();
 		
 		// Create the consumer thread
 		MessagingThread consumerThread = new MessagingThread(settings, context);
 		Thread thread = new Thread(consumerThread, context.getContextID());
 		thread.start();
 		
 		log.info("Started consumer {}", context.getContextID());
 		
 		lastContext = context;
 		return context;
 	}
 		
 	private void configureJMS(MessagingContext context) throws NamingException, JMSException {
 		// Java Naming Context
 		if(settings.isSharingContext() && lastContext != null) {
 			context.setNamingContext(lastContext.getNamingContext());
 		} else {
 			context.setNamingContext(new InitialContext(System.getProperties()));
 		}
 		
 		// Connection Factory
 		if(settings.isSharingConnectionFactory() && lastContext != null) {
 			context.setConnectionFactory(lastContext.getConnectionFactory());
 		}
 		else {
 			String cfname = settings.getConnectionFactoryName();
 			ConnectionFactory cf = (ConnectionFactory)context.getNamingContext().lookup(cfname);
 			context.setConnectionFactory(cf);
 			
			// Workaround for localBindAddress issue
 			HornetQConnectionFactory hqcf = (HornetQConnectionFactory)cf;
			hqcf.getDiscoveryGroupConfiguration().setLocalBindAdress(null);
 		}
 		
 		// Destination
 		if(settings.isSharingDestination() && lastContext != null) {
 			context.setDestination(lastContext.getDestination());
 		} else {
 			String destname = settings.getDestinationName();
 			Destination dest = (Destination)context.getNamingContext().lookup(destname);
 			context.setDestination(dest);
 		}
 		
 		// Connection
 		if(settings.isSharingConnection() && lastContext != null) {
 			context.setConnection(lastContext.getConnection());
 		}
 		else {
 			String user = settings.getUsername();
 			String pass = settings.getPassword();			
 			Connection conn = context.getConnectionFactory().createConnection(user, pass);		
 			context.setConnection(conn);			
 		}
 		
 		// Session
 		if(settings.isSharingSession() && lastContext != null) {
 			context.setSession(lastContext.getSession());
 		}
 		else {
 			Session sess = context.getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
 			context.setSession(sess);
 		}
 	}
 
 	private boolean isAllDone() {
 		for(MessagingContext producer : producerContexts)
 			if(!producer.isDone())
 				return false;
 		
 		if(settings.getConsumerCount() != 0) {
 			long targetCount = settings.getMessageCount() * settings.getProducerCount() + settings.getConsumeAdditionalCount();
 			long consumedCount = 0;
 			for(MessagingContext consumer : consumerContexts) {
 				consumedCount += consumer.getMessageCount();
 			}
 			
 			if(consumedCount < targetCount)
 				return false;
 				
 			for(MessagingContext consumer : consumerContexts) {
 				consumer.setDone(true);
 			}
 		}
 		
 		return true;
 	}
 	
 	private void cleanupAll() {
 		for(MessagingContext context : producerContexts) 
 			cleanup(context);
 		for(MessagingContext context : consumerContexts) 
 			cleanup(context);
 	}
 	
 	private void cleanup(MessagingContext context)  {
 		try {
 			log.debug("Cleaning up JMS context {}", context);
 			
 			if(context.getMessageProducer() != null)
 				context.getMessageProducer().close();
 			
 			if(context.getMessageConsumer() != null) 
 				context.getMessageConsumer().close();
 			
 			if(context.getSession() != null)
 				context.getSession().close();
 			
 			if(context.getConnection() != null)
 				context.getConnection().close();
 			
 			if(context.getNamingContext() != null)
 				context.getNamingContext().close();
 
 			log.debug("Cleaned up JMS context {}", context);
 		} catch (JMSException e) {
 			log.error("Failed to clean up JMS context: " + context, e);
 		} catch (NamingException e) {
 			log.error("Failed to clean up Naming context: " + context, e);
 		}	
 	}
 	
 	private void sleep(Long ms) {
 		try {
 			Thread.sleep(ms);
 		} catch (InterruptedException e) {
 			log.error("Messaging controller interrupted.", e);
 		}
 	}
 	
 }
