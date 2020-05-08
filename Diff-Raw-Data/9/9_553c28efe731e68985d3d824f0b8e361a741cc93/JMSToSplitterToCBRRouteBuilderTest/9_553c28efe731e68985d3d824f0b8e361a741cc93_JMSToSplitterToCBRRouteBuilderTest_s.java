 package com.vpn.integration.route;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.jms.ConnectionFactory;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.apache.activemq.RedeliveryPolicy;
 import org.apache.activemq.broker.BrokerService;
 import org.apache.activemq.broker.region.policy.RedeliveryPolicyMap;
 import org.apache.activemq.broker.util.RedeliveryPlugin;
 import org.apache.activemq.camel.component.ActiveMQComponent;
 import org.apache.activemq.command.ActiveMQQueue;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.component.jms.JmsComponent;
 import org.apache.camel.impl.JndiRegistry;
 import org.apache.camel.spring.spi.SpringTransactionPolicy;
 import org.apache.camel.test.junit4.CamelTestSupport;
 import org.apache.commons.io.FileUtils;
 import org.custommonkey.xmlunit.DetailedDiff;
 import org.custommonkey.xmlunit.Diff;
 import org.custommonkey.xmlunit.Difference;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jms.connection.JmsTransactionManager;
 import org.xml.sax.SAXException;
 
 public class JMSToSplitterToCBRRouteBuilderTest extends CamelTestSupport {
 
 	@Rule
 	public TemporaryFolder folder = new TemporaryFolder();
 
 	private File incoming;
 	private File outgoing;
 	
 	private static ConnectionFactory connectionFactory = null;
 	
 	private BrokerService broker = null;
 	
 	public static void initialize() {
 		
 		
 	}
 //			"vm://test-broker?broker.persistent=false");
 
 	private static Logger log = LoggerFactory.getLogger(JMSToSplitterToCBRRouteBuilderTest.class);
 
 	@Before
 	@Override
 	public void setUp() throws Exception {
 		
 		broker = new BrokerService();
 		broker.setBrokerName("test-broker");
 		broker.setPersistent(false);
 		broker.setUseJmx(false);
 		broker.setSchedulerSupport(true);
 
 		
 		
 		RedeliveryPlugin redeliveryPlugin = new RedeliveryPlugin();
 		RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
 		redeliveryPolicy.setInitialRedeliveryDelay(2000);
 		redeliveryPolicy.setRedeliveryDelay(10000);
 		redeliveryPolicy.setUseExponentialBackOff(false);
 //		redeliveryPolicy.setBackOffMultiplier(2);
 		redeliveryPolicy.setMaximumRedeliveries(-1);
 //		redeliveryPolicy.setMaximumRedeliveryDelay(60000);
 		redeliveryPolicy.setQueue("*");
 		RedeliveryPolicyMap redeliveryPolicyMap = new RedeliveryPolicyMap();
 		redeliveryPolicyMap.put(new ActiveMQQueue("rfq"), redeliveryPolicy);
 		redeliveryPlugin.setRedeliveryPolicyMap(redeliveryPolicyMap);
 		redeliveryPlugin.installPlugin(broker.getBroker());
 		broker.start();
 
 		incoming = folder.newFolder("Incoming");
 		outgoing = folder.newFolder("Outgoing");
 		
 		FileUtils.copyFileToDirectory(new File(
 				"./src/test/resources/testRFQ.xml"), incoming.getAbsoluteFile());
 
//		connectionFactory =  new ActiveMQConnectionFactory(
//		"vm://test-broker?create=false&broker.persistent=false");
 		connectionFactory =  new ActiveMQConnectionFactory(
				"tcp://localhost:61636");
 		RedeliveryPolicy cfRedeliveryPolicy = new RedeliveryPolicy();
 		cfRedeliveryPolicy.setInitialRedeliveryDelay(0);
 		cfRedeliveryPolicy.setRedeliveryDelay(10000);
 		cfRedeliveryPolicy.setUseExponentialBackOff(false);
 		cfRedeliveryPolicy.setMaximumRedeliveries(-1);
 		((ActiveMQConnectionFactory)connectionFactory).getRedeliveryPolicyMap().put(new ActiveMQQueue(">"), cfRedeliveryPolicy);
 		
 		
 //		((ActiveMQConnectionFactory) connectionFactory)
 //				.setRedeliveryPolicy(cfRedeliveryPolicy);
 
 		super.setUp();
 
 	
 	}
 
 	@Test
 	public void test() throws InterruptedException, IOException, SAXException {
 		Thread.sleep(2000);
 		String expected = FileUtils.readFileToString(new File(
 				"./src/test/resources/123456-output.xml"), "UTF-8");
 
 		String actual = FileUtils.readFileToString(new File(outgoing.getAbsolutePath() + File.separator + "123456-output.xml"), "UTF-8");
 		Diff diff = new Diff(expected, actual);
 		if(!diff.identical()) {
 			log.error("The expected RFQ does not match actual. Listing the differences...");
 			DetailedDiff detailedDiff = new DetailedDiff(diff);
 			for(Object difference : detailedDiff.getAllDifferences()) {
 				log.info(((Difference)difference).getDescription());
 			}
 			fail("The expected resultant RFQ does not match actual");
 		}
 		//Thread.sleep(3000000);
 	}
 	
 /*	@Test
 	public void testFile2JMSRoute() throws InterruptedException, JMSException,
 			IOException, SAXException {
 
 		// Create a Connection
 		Connection connection = connectionFactory.createConnection();
 		connection.start();
 
 		// Create a Session
 		Session session = connection.createSession(false,
 				Session.AUTO_ACKNOWLEDGE);
 
 		// Create the destination (Topic or Queue)
 		Destination destinationFiction = session.createQueue("FICTION");
 
 		// Create a MessageConsumer from the Session to the Topic or Queue
 		MessageConsumer consumerFiction = session
 				.createConsumer(destinationFiction);
 
 		// Wait for a message
 		Message messageFiction = consumerFiction.receive(2000);
 
 		if (messageFiction instanceof TextMessage) {
 			TextMessage textMessage = (TextMessage) messageFiction;
 			String text = textMessage.getText();
 
 			XMLUnit.setIgnoreWhitespace(true);
 			Diff diff = new Diff(FileUtils.readFileToString(new File(
 					"./src/test/resources/item-fiction.xml"), "UTF-8"), text);
 
 			System.out.println(FileUtils.readFileToString(new File(
 					"./src/test/resources/item-fiction.xml"), "UTF-8"));
 			System.out.println(text);
 
 			Assert.assertTrue(
 					"The content from src/test/resources/item-fiction.xml should have the same as the content retrieved from the jms message",
 					diff.similar());
 		} else {
 			fail("message should have been a TextMessage");
 		}
 
 		// Create the destination (Topic or Queue)
 		Destination destination = session.createQueue("DRAMA");
 
 		// Create a MessageConsumer from the Session to the Topic or Queue
 		MessageConsumer consumer = session.createConsumer(destination);
 
 		// Wait for a message
 		Message message = consumer.receive(2000);
 
 		if (message instanceof TextMessage) {
 			TextMessage textMessage = (TextMessage) message;
 			String text = textMessage.getText();
 			XMLUnit.setIgnoreWhitespace(true);
 			Diff diff = new Diff(FileUtils.readFileToString(new File(
 					"./src/test/resources/item-drama.xml"), "UTF-8"), text);
 			System.out.println(FileUtils.readFileToString(new File(
 					"./src/test/resources/item-drama.xml"), "UTF-8"));
 			System.out.println(text);
 			Assert.assertTrue(
 					"The content from src/test/resources/item-drama.xml should have the same as the content retrieved from the jms message",
 					diff.similar());
 
 		} else {
 			fail("message should have been a TextMessage");
 		}
 
 		consumerFiction.close();
 		consumer.close();
 		session.close();
 		connection.close();
 	}
 */
 	@Override
 	protected RouteBuilder[] createRouteBuilders() throws Exception {
 		addTestJmsComponent();
 		
 		FileToJMSRouteBuilder file2JmsRouteBuilder = new FileToJMSRouteBuilder();
 		file2JmsRouteBuilder.setIncomingFileDirectory(incoming.getAbsolutePath());
 
 		JMSToSplitterToCBRRouteBuilder jmsToSplitterToCBRRouteBuilder = new JMSToSplitterToCBRRouteBuilder();
 		jmsToSplitterToCBRRouteBuilder.setOutputFileDirectory(outgoing.getAbsolutePath());
 		return new RouteBuilder[] { file2JmsRouteBuilder,
 				jmsToSplitterToCBRRouteBuilder };
 	}
 
 	@Override
     protected JndiRegistry createRegistry() throws Exception {
         JndiRegistry reg = super.createRegistry();
                 
         
         org.springframework.jms.connection.JmsTransactionManager transactionManager = new org.springframework.jms.connection.JmsTransactionManager();
 		transactionManager.setConnectionFactory(connectionFactory);
 		reg.bind("txManager", transactionManager);
         
         SpringTransactionPolicy txPolicy = new SpringTransactionPolicy();
         txPolicy.setTransactionManager(transactionManager);
         txPolicy.setPropagationBehaviorName("PROPAGATION_REQUIRED");
         reg.bind("required", txPolicy);
         
         return reg;
     }
 	
 	private void addTestJmsComponent() {
 		
 		org.springframework.jms.connection.JmsTransactionManager transactionManager = new org.springframework.jms.connection.JmsTransactionManager();
 		transactionManager.setConnectionFactory(connectionFactory);
 		
 		ActiveMQComponent c = new ActiveMQComponent();
 		c.setConnectionFactory(connectionFactory);
 		c.setTransacted(true);
 		
 	    c.setTransactionManager((JmsTransactionManager)context.getRegistry().lookup("txManager"));
 		context.addComponent("jms", c);
 
 //		JmsComponent component = JmsComponent.jmsComponentTransacted(
 //				connectionFactory, transactionManager);
 
 //		context.addComponent("jms", component);
 
 	}
 	
 	@Override
 	@After
 	public void tearDown() throws Exception {
 		super.tearDown();
 		broker.stop();
 	}
 
 }
