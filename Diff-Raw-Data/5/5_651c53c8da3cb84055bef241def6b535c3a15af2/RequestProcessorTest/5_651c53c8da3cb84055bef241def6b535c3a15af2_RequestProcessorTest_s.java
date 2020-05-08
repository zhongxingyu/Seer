 package ch.ethz.mlmq.test.processing;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import ch.ethz.mlmq.dto.ClientDto;
 import ch.ethz.mlmq.dto.MessageDto;
 import ch.ethz.mlmq.dto.MessageQueryInfoDto;
 import ch.ethz.mlmq.dto.QueueDto;
 import ch.ethz.mlmq.exception.MlmqException;
 import ch.ethz.mlmq.logging.LoggerUtil;
 import ch.ethz.mlmq.logging.PerformanceLoggerManager;
 import ch.ethz.mlmq.net.request.CreateQueueRequest;
 import ch.ethz.mlmq.net.request.DeleteQueueRequest;
 import ch.ethz.mlmq.net.request.DequeueMessageRequest;
 import ch.ethz.mlmq.net.request.PeekMessageRequest;
 import ch.ethz.mlmq.net.request.QueuesWithPendingMessagesRequest;
 import ch.ethz.mlmq.net.request.RegistrationRequest;
 import ch.ethz.mlmq.net.request.Request;
 import ch.ethz.mlmq.net.request.SendClientMessageRequest;
 import ch.ethz.mlmq.net.request.SendMessageRequest;
 import ch.ethz.mlmq.net.response.CreateQueueResponse;
 import ch.ethz.mlmq.net.response.DeleteQueueResponse;
 import ch.ethz.mlmq.net.response.MessageResponse;
 import ch.ethz.mlmq.net.response.QueuesWithPendingMessagesResponse;
 import ch.ethz.mlmq.net.response.RegistrationResponse;
 import ch.ethz.mlmq.net.response.SendClientMessageResponse;
 import ch.ethz.mlmq.net.response.SendMessageResponse;
 import ch.ethz.mlmq.server.BrokerConfiguration;
 import ch.ethz.mlmq.server.ClientApplicationContext;
 import ch.ethz.mlmq.server.db.DbConnectionPool;
 import ch.ethz.mlmq.server.db.util.DatabaseInitializer;
 import ch.ethz.mlmq.server.processing.RequestProcessor;
 import ch.ethz.mlmq.util.ConfigurationUtil;
 
 public class RequestProcessorTest {
 
 	private final Logger logger = Logger.getLogger(RequestProcessorTest.class.getSimpleName());
 
 	private static BrokerConfiguration config;
 
 	private RequestProcessor processor;
 
 	private static DatabaseInitializer dbInitializer;
 	private static String dbName = "mlmqunittest" + System.currentTimeMillis();
 
 	private static DbConnectionPool pool;
 
 	private ClientApplicationContext defaultContext = null;
 
 	@BeforeClass
 	public static void beforeClass() throws IOException, SQLException, MlmqException {
 		LoggerUtil.initConsoleDebug();
 		PerformanceLoggerManager.configureDisabled();
 
 		// load properties
 		Properties props = ConfigurationUtil.loadPropertiesFromJar("brokerconfig.properties");
 		props.put(BrokerConfiguration.DB_NAME, dbName);
 		props.put(BrokerConfiguration.DB_CONNECTIONPOOL_SIZE, 1 + "");
 		config = new BrokerConfiguration(props);
 
 		dbInitializer = new DatabaseInitializer(config.getDbUrl(), config.getDbUserName(), config.getDbPassword(), dbName);
 		dbInitializer.connect();
 		dbInitializer.createDatabase();
 		dbInitializer.createTables();
 
 		pool = new DbConnectionPool(config);
 		pool.init();
 	}
 
 	@AfterClass
 	public static void afterClass() throws SQLException {
 		pool.close();
 		dbInitializer.deleteDatabase();
 	}
 
 	@Before
 	public void before() throws MlmqException {
 		processor = new RequestProcessor();
 
 		defaultContext = registerClient("DefaultRequestProcessorUnitTestClient");
 	}
 
 	private ClientApplicationContext registerClient(String name) throws MlmqException {
 		logger.info("Register UnitTestClient");
 
 		ClientApplicationContext context = new ClientApplicationContext(1);
 		RegistrationRequest registerRequest = new RegistrationRequest(name);
 		processor.process(context, registerRequest, pool);
 
 		return context;
 	}
 
 	@Test
 	public void doTest() throws MlmqException {
 		testRegistrationRequest();
 		testCreateQueueRequest();
 		testSendMessageRequest();
 		testDequeueMessageRequest();
 		testPeekMessageRequest();
		testQueuesWithPendingMessagesRequest();
		testDeleteQueueRequest();
 	}
 
 	/**
 	 * Clients can send messages to multiple queues
 	 * 
 	 * @throws MlmqException
 	 */
 	@Test
 	public void testSendToMultipleQueues() throws MlmqException {
 
 		int numQueues = 10;
 		List<Long> queueIdList = new ArrayList<>();
 
 		byte[] content = "Hallo Welt".getBytes();
 		int prio = 10;
 
 		for (int i = 0; i < numQueues; i++) {
 			String queueName = "testSendToMultipleQueues" + i;
 			logger.info("Creating Queue " + queueName);
 			CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
 			CreateQueueResponse response = (CreateQueueResponse) processor.process(defaultContext, createQueueRequest, pool);
 			queueIdList.add(response.getQueueDto().getId());
 		}
 
 		Request request = new SendMessageRequest(queueIdList, content, prio);
 		SendMessageResponse response = (SendMessageResponse) processor.process(defaultContext, request, pool);
 		Assert.assertNotNull(response);
 
 		for (Long queueId : queueIdList) {
 
 			QueueDto queueFilter = new QueueDto(queueId);
 			ClientDto sender = null;
 			boolean shouldOrderByPriority = true;
 			MessageQueryInfoDto messageQueryInfo = new MessageQueryInfoDto(queueFilter, sender, shouldOrderByPriority);
 			DequeueMessageRequest dequeuRequest = new DequeueMessageRequest(messageQueryInfo);
 
 			MessageResponse messageResponse = (MessageResponse) processor.process(defaultContext, dequeuRequest, pool);
 			Assert.assertNotNull(messageResponse);
 
 			Assert.assertArrayEquals(content, messageResponse.getMessageDto().getContent());
 			Assert.assertEquals(prio, messageResponse.getMessageDto().getPrio());
 		}
 	}
 
 	/**
 	 * Clients can send a message to a queue indicating a particular receiver
 	 * 
 	 * If a message has an explicit receiver, it can only be accessed by that receiver
 	 * 
 	 * @throws MlmqException
 	 */
 	@Test
 	public void testSendToClient() throws MlmqException {
 		ClientApplicationContext clientContext1 = registerClient("SendToClient1");
 		ClientApplicationContext clientContext2 = registerClient("SendToClient2");
 
 		byte[] content = "Blub".getBytes();
 		int prio = 5;
 
 		logger.info("Client 1 sends a message to Client 2");
 		long queueId = clientContext2.getClientQueue().getId();
 		SendMessageRequest sendToClient2 = new SendMessageRequest(queueId, content, prio);
 		processor.process(clientContext1, sendToClient2, pool);
 
 		logger.info("Client 2 wants to read it");
 		QueueDto queueFilter = clientContext2.getClientQueue();
 		ClientDto sender = null;
 		boolean shouldOrderByPriority = true;
 		MessageQueryInfoDto messageQueryInfo = new MessageQueryInfoDto(queueFilter, sender, shouldOrderByPriority);
 		DequeueMessageRequest readMessage = new DequeueMessageRequest(messageQueryInfo);
 		MessageResponse receiveMessageResponse = (MessageResponse) processor.process(clientContext2, readMessage, pool);
 
 		Assert.assertNotNull(receiveMessageResponse);
 		Assert.assertArrayEquals(content, receiveMessageResponse.getMessageDto().getContent());
 	}
 
 	/**
 	 * A client can send a a Request-response like message to a specific other client
 	 * 
 	 * @throws MlmqException
 	 */
 	@Test
 	public void testRequestResponse() throws MlmqException {
 		ClientApplicationContext clientContext1 = registerClient("RequestResponseClient1");
 		ClientApplicationContext clientContext2 = registerClient("RequestResponseClient2");
 
 		byte[] content1 = "Request".getBytes();
 		byte[] content2 = "Response".getBytes();
 		int prio = 5;
 
 		logger.info("Client 1 sends a request-message to Client 2");
 		long clientId1 = clientContext1.getClient().getId();
 		long clientId2 = clientContext2.getClient().getId();
 		SendClientMessageRequest sendToClient2 = new SendClientMessageRequest(clientId2, content1, prio, true);
 		SendClientMessageResponse response1 = (SendClientMessageResponse) processor.process(clientContext1, sendToClient2, pool);
 
 		Assert.assertNotNull("Expect ConversationContext", response1.getConversationContext());
 
 		logger.info("Client 2 wants to read it");
 		QueueDto queue2Filter = clientContext2.getClientQueue();
 		ClientDto sender = null;
 		boolean shouldOrderByPriority = true;
 		MessageQueryInfoDto messageQueryInfo = new MessageQueryInfoDto(queue2Filter, sender, shouldOrderByPriority);
 		DequeueMessageRequest readMessage = new DequeueMessageRequest(messageQueryInfo);
 		MessageResponse receiveMessageResponse = (MessageResponse) processor.process(clientContext2, readMessage, pool);
 
 		Assert.assertNotNull(receiveMessageResponse);
 		MessageDto msg = receiveMessageResponse.getMessageDto();
 		Assert.assertArrayEquals(content1, msg.getContent());
 		Assert.assertNotNull(msg.getConversationContext());
 
 		int conversationContext = msg.getConversationContext();
 
 		logger.info("Client 2 responds on Conversation Context " + conversationContext);
 		SendClientMessageRequest replyToClient = new SendClientMessageRequest(clientId1, content2, prio, conversationContext);
 		SendClientMessageResponse replyResponse = (SendClientMessageResponse) processor.process(clientContext1, replyToClient, pool);
 		Assert.assertNotNull(replyResponse);
 
 		logger.info("Client 1 reads reply");
 		QueueDto queue1Filter = clientContext1.getClientQueue();
 		MessageQueryInfoDto messageQueryInfo2 = new MessageQueryInfoDto(queue1Filter, null, false, conversationContext);
 		DequeueMessageRequest readResponseMessage = new DequeueMessageRequest(messageQueryInfo2);
 		MessageResponse receiveResponseMessageResponse = (MessageResponse) processor.process(clientContext1, readResponseMessage, pool);
 		Assert.assertNotNull(receiveResponseMessageResponse);
 	}
 
 	@Test
 	public void testPublicQueue() throws MlmqException {
 		MessageQueryInfoDto mQI;
 
 		byte[] msg1 = "Message1".getBytes();
 		byte[] msg2 = "Message2".getBytes();
 		byte[] msg3 = "Message3".getBytes();
 
 		// register clients
 		ClientApplicationContext context1 = registerClient("Client1");
 		ClientApplicationContext context2 = registerClient("Client2");
 		ClientApplicationContext context3 = registerClient("Client3");
 
 		// create Queue
 		CreateQueueResponse createQueueResponse = (CreateQueueResponse) processor.process(context1, new CreateQueueRequest("AnyQueue"), pool);
 		long queueId = createQueueResponse.getQueueDto().getId();
 		logger.info("Created public Queue " + queueId);
 
 		// send 3 messages
 		processor.process(context1, new SendMessageRequest(queueId, msg1, 1), pool);
 		processor.process(context1, new SendMessageRequest(queueId, msg2, 10), pool);
 		processor.process(context1, new SendMessageRequest(queueId, msg3, 5), pool);
 
 		// expect to read oldest message msg1
 		mQI = new MessageQueryInfoDto(new QueueDto(queueId), null, false);
 		MessageResponse msgResonse = (MessageResponse) processor.process(context2, new PeekMessageRequest(mQI), pool);
 		Assert.assertArrayEquals(msg1, msgResonse.getMessageDto().getContent());
 
 		// expect and delete hightest prio message msg2
 		mQI = new MessageQueryInfoDto(new QueueDto(queueId), null, true);
 		msgResonse = (MessageResponse) processor.process(context3, new DequeueMessageRequest(mQI), pool);
 		Assert.assertArrayEquals(msg2, msgResonse.getMessageDto().getContent());
 
 		// expect and delete hightest prio message msg3 (msg2 was deleted
 		msgResonse = (MessageResponse) processor.process(context3, new DequeueMessageRequest(mQI), pool);
 		Assert.assertArrayEquals(msg3, msgResonse.getMessageDto().getContent());
 
 	}
 
 	public void testCreateQueueRequest() throws MlmqException {
 
 		Request request = new CreateQueueRequest("SampleQueue");
 		CreateQueueResponse response = (CreateQueueResponse) processor.process(defaultContext, request, pool);
 		Assert.assertNotNull(response);
 		Assert.assertNotNull(response.getQueueDto());
 	}
 
 	public void testQueuesWithPendingMessagesRequest() throws MlmqException {
 		Request request = new QueuesWithPendingMessagesRequest();
 		QueuesWithPendingMessagesResponse response = (QueuesWithPendingMessagesResponse) processor.process(defaultContext, request, pool);
 		Assert.assertNotNull(response);
 		Assert.assertNotNull(response.getQueues());
 	}
 
 	public void testRegistrationRequest() throws MlmqException {
 		Request request = new RegistrationRequest("ClientName");
 		RegistrationResponse response = (RegistrationResponse) processor.process(defaultContext, request, pool);
 		Assert.assertNotNull(response);
 		Assert.assertNotNull(response.getClientDto());
 	}
 
 	public void testDeleteQueueRequest() throws MlmqException {
 		Request request = new DeleteQueueRequest(1);
 		DeleteQueueResponse response = (DeleteQueueResponse) processor.process(defaultContext, request, pool);
 		Assert.assertNotNull(response);
 	}
 
 	public void testDequeueMessageRequest() throws MlmqException {
 		Request request = new DequeueMessageRequest(createTestMessageQueryInfoDto());
 		MessageResponse response = (MessageResponse) processor.process(defaultContext, request, pool);
 		Assert.assertNotNull(response);
 	}
 
 	private MessageQueryInfoDto createTestMessageQueryInfoDto() {
 		QueueDto queueFilter = new QueueDto(123);
 		ClientDto sender = new ClientDto(1);
 		boolean shouldOrderByPriority = true;
 		MessageQueryInfoDto messageQueryInfoDto = new MessageQueryInfoDto(queueFilter, sender, shouldOrderByPriority);
 
 		return messageQueryInfoDto;
 	}
 
 	public void testPeekMessageRequest() throws MlmqException {
 		Request request = new PeekMessageRequest(createTestMessageQueryInfoDto());
 		MessageResponse response = (MessageResponse) processor.process(defaultContext, request, pool);
 		Assert.assertNotNull(response);
 	}
 
 	public void testSendMessageRequest() throws MlmqException {
 		long queueId = 1;
 		byte[] content = "Hallo Welt".getBytes();
 		int prio = 10;
 		Request request = new SendMessageRequest(queueId, content, prio);
 		SendMessageResponse response = (SendMessageResponse) processor.process(defaultContext, request, pool);
 		Assert.assertNotNull(response);
 	}
 }
