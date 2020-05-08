 package ch.ethz.mlmq.server.db.dao;
 
 import java.io.Closeable;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import ch.ethz.mlmq.dto.ClientDto;
 import ch.ethz.mlmq.dto.MessageDto;
 import ch.ethz.mlmq.dto.MessageQueryInfoDto;
 import ch.ethz.mlmq.dto.QueueDto;
 import ch.ethz.mlmq.logging.LoggerUtil;
 import ch.ethz.mlmq.logging.PerformanceLogger;
 import ch.ethz.mlmq.logging.PerformanceLoggerManager;
 import ch.ethz.mlmq.net.request.SendMessageRequest;
 import ch.ethz.mlmq.server.ClientApplicationContext;
 
 public class MessageDao implements Closeable {
 
 	private static final Logger logger = Logger.getLogger(MessageDao.class.getSimpleName());
 
 	private final PerformanceLogger perfLog = PerformanceLoggerManager.getLogger();
 
 	private PreparedStatement beginTransStmt;
 	private PreparedStatement commitTransStmt;
 	private PreparedStatement insertMessageStmt;
 	private PreparedStatement peekMessageStmt;
 	private PreparedStatement peekMessageForUpdateStmt;
 	private PreparedStatement deleteMessageStmt;
 	private PreparedStatement generateNewConversationContextStmt;
 	private PreparedStatement getPublicQueuesContainingMessagesStmt;
 	private PreparedStatement getNumMsgPerQueueStmt;
 
 	public void init(Connection connection) throws SQLException {
 		// prepare statements
 
 		beginTransStmt = connection.prepareStatement("BEGIN TRANSACTION");
 		commitTransStmt = connection.prepareStatement("COMMIT TRANSACTION");
 
 		//@formatter:off
 		String insertSqlStatement = "INSERT INTO message("
 				+ "queue_id, client_sender_id, content, prio, context)"
 				+ "VALUES (?, ?, ?, ?, ?)";
 		//@formatter:on
 		insertMessageStmt = connection.prepareStatement(insertSqlStatement);
 
 		String peekMessageSqlStmt = "SELECT id, queue_id, client_sender_id, content, prio, sent_at, context FROM peekMessage(?, ?, ?, ?)";
 		peekMessageStmt = connection.prepareStatement(peekMessageSqlStmt);
 
 		String peekMessageForUpdateSqlStmt = "SELECT id, queue_id, client_sender_id, content, prio, sent_at, context FROM dequeueMessage(?, ?, ?, ?)";
 		peekMessageForUpdateStmt = connection.prepareStatement(peekMessageForUpdateSqlStmt);
 
 		String deleteMessageSqlStmt = "DELETE FROM message WHERE id = ?";
 		deleteMessageStmt = connection.prepareStatement(deleteMessageSqlStmt);
 
 		generateNewConversationContextStmt = connection.prepareStatement("SELECT nextval('message_context')");
 
 		String getNumMsgPerQueueSqlStmt = "SELECT count(*) FROM message WHERE queue_id = ?";
 		getNumMsgPerQueueStmt = connection.prepareStatement(getNumMsgPerQueueSqlStmt);
 
 		//@formatter:off
 		String getPublicQueuesContainingMessagesSqlStmt = ""
 				+ "SELECT m.queue_id "
 				+ "FROM message m "
 				+ "GROUP BY m.queue_id "
 				+ "LIMIT ?";
 		//@formatter:on
 		getPublicQueuesContainingMessagesStmt = connection.prepareStatement(getPublicQueuesContainingMessagesSqlStmt);
 
 	}
 
 	public void close() {
 		try {
 			beginTransStmt.close();
 		} catch (SQLException e) {
 			logger.severe("Error while closing insertMessageStmt" + LoggerUtil.getStackTraceString(e));
 		}
 
 		try {
 			commitTransStmt.close();
 		} catch (SQLException e) {
 			logger.severe("Error while closing insertMessageStmt" + LoggerUtil.getStackTraceString(e));
 		}
 
 		try {
 			insertMessageStmt.close();
 		} catch (SQLException e) {
 			logger.severe("Error while closing insertMessageStmt" + LoggerUtil.getStackTraceString(e));
 		}
 
 		try {
 			peekMessageStmt.close();
 		} catch (SQLException e) {
 			logger.severe("Error while closing peekMessageStmt" + LoggerUtil.getStackTraceString(e));
 		}
 
 		try {
 			peekMessageForUpdateStmt.close();
 		} catch (SQLException e) {
 			logger.severe("Error while closing peekMessageStmt" + LoggerUtil.getStackTraceString(e));
 		}
 
 		try {
 			deleteMessageStmt.close();
 		} catch (SQLException e) {
 			logger.severe("Error while closing deleteMessageStmt" + LoggerUtil.getStackTraceString(e));
 		}
 
 		try {
 			generateNewConversationContextStmt.close();
 		} catch (SQLException e) {
 			logger.severe("Error while closing generateNewConversationContextStmt" + LoggerUtil.getStackTraceString(e));
 		}
 
 		try {
 			getNumMsgPerQueueStmt.close();
 		} catch (SQLException e) {
 			logger.severe("Error while closing getNumMsgPerQueueStmt" + LoggerUtil.getStackTraceString(e));
 		}
 
 		try {
 			getPublicQueuesContainingMessagesStmt.close();
 		} catch (SQLException e) {
 			logger.severe("Error while closing getPublicQueuesContainingMessagesStmt" + LoggerUtil.getStackTraceString(e));
 		}
 	}
 
 	public void insertMessage(SendMessageRequest request, ClientApplicationContext clientContext) throws SQLException {
 		long startTime = System.currentTimeMillis();
 
 		try {
 			for (long queueId : request.getQueueIds()) {
 				insertMessageStmt.setLong(1, queueId);
 				insertMessageStmt.setLong(2, clientContext.getClient().getId());
 				insertMessageStmt.setBytes(3, request.getContent());
 				insertMessageStmt.setInt(4, request.getPrio());
 				insertMessageStmt.setNull(5, Types.INTEGER);
 
 				insertMessageStmt.execute();
 			}
 		} finally {
 			perfLog.log(System.currentTimeMillis() - startTime, "BDb#insertMessage");
 		}
 	}
 
 	public void insertMessage(long queueId, long clientId, byte[] content, int prio, Long clientContext) throws SQLException {
 		long startTime = System.currentTimeMillis();
 
 		try {
 			insertMessageStmt.setLong(1, queueId);
 			insertMessageStmt.setLong(2, clientId);
 			insertMessageStmt.setBytes(3, content);
 			insertMessageStmt.setInt(4, prio);
 
 			if (clientContext == null) {
 				insertMessageStmt.setNull(5, Types.INTEGER);
 			} else {
 				insertMessageStmt.setLong(5, clientContext);
 			}
 
 			insertMessageStmt.execute();
 
 		} finally {
 			perfLog.log(System.currentTimeMillis() - startTime, "BDb#insertMessage");
 		}
 	}
 
 	public MessageDto dequeueMessage(MessageQueryInfoDto queryInfo) throws SQLException {
 		long startTime = System.currentTimeMillis();
 		int result = -1;
 
 		try {
 			beginTransStmt.execute();
 
 			MessageDto message = peekMessage(peekMessageForUpdateStmt, queryInfo);
 			if (message == null) {
 				// queue is empty or filter does not match
 				return null;
 			}
 
 			deleteMessageStmt.setLong(1, message.getId());
 			result = deleteMessageStmt.executeUpdate();
 
 			if (result != 1) {
 				throw new SQLException("Dequeue did not work - DeleteCount[" + result + "] MessageId[" + message.getId() + "]");
 			}
 
 			return message;
 		} finally {
 			commitTransStmt.execute();
 			long executionTime = System.currentTimeMillis() - startTime;
 			if (result == 1) {
				perfLog.log(executionTime, "BDb#dequeueMessage#Ok#1");
 			} else if (result == -1) {
				perfLog.log(executionTime, "BDb#dequeueMessage#Ok#0");
 			} else {
 				perfLog.log(executionTime, "BDb#dequeueMessage#Error");
 			}
 		}
 	}
 
 	public MessageDto peekMessage(MessageQueryInfoDto queryInfo) throws SQLException {
 		long startTime = System.currentTimeMillis();
 		try {
 			return peekMessage(peekMessageStmt, queryInfo);
 		} finally {
 			perfLog.log(System.currentTimeMillis() - startTime, "BDb#peekMessage");
 		}
 	}
 
 	private MessageDto peekMessage(PreparedStatement prepStmt, MessageQueryInfoDto queryInfo) throws SQLException {
 
 		if (queryInfo.getQueue() == null) {
 			prepStmt.setNull(1, Types.INTEGER);
 		} else {
 			prepStmt.setInt(1, (int) queryInfo.getQueue().getId());
 		}
 
 		if (queryInfo.getSender() == null) {
 			prepStmt.setNull(2, Types.INTEGER);
 		} else {
 			prepStmt.setInt(2, (int) queryInfo.getSender().getId());
 		}
 
 		prepStmt.setBoolean(3, queryInfo.shouldOrderByPriority());
 
 		Integer expectedContext = queryInfo.getConversationContext();
 		if (expectedContext == null) {
 			prepStmt.setNull(4, Types.INTEGER);
 		} else {
 			prepStmt.setInt(4, ((int) (long) queryInfo.getConversationContext()));
 		}
 
 		try (ResultSet rs = prepStmt.executeQuery()) {
 			if (rs.next()) {
 				MessageDto message = new MessageDto();
 
 				message.setId(rs.getLong(1));
 				message.setQueue(new QueueDto(rs.getLong(2)));
 				message.setSender(new ClientDto(rs.getLong(3)));
 				message.setContent(rs.getBytes(4));
 				message.setPrio(rs.getInt(5));
 
 				int context = rs.getInt(7);
 				if (!rs.wasNull()) {
 					message.setConversationContext(context);
 				}
 
 				return message;
 			}
 		}
 		return null;
 	}
 
 	public long generateNewConversationContext() throws SQLException {
 		long startTime = System.currentTimeMillis();
 
 		try (ResultSet rs = generateNewConversationContextStmt.executeQuery()) {
 			if (rs.next()) {
 				return rs.getLong(1);
 			}
 			throw new SQLException("No Value found for generateNewConversationContext");
 		} finally {
 			perfLog.log(System.currentTimeMillis() - startTime, "BDb#genConversationCtx");
 		}
 	}
 
 	/**
 	 * 
 	 * @param maxNumQueues
 	 *            constrains the maximum results returned
 	 * @return
 	 * @throws SQLException
 	 */
 	public List<QueueDto> getPublicQueuesContainingMessages(int maxNumQueues) throws SQLException {
 		long startTime = System.currentTimeMillis();
 
 		List<QueueDto> result = new ArrayList<>();
 
 		getPublicQueuesContainingMessagesStmt.setInt(1, maxNumQueues);
 		try (ResultSet rs = getPublicQueuesContainingMessagesStmt.executeQuery()) {
 			while (rs.next()) {
 				long queueId = rs.getLong(1);
 				QueueDto queue = new QueueDto(queueId);
 				result.add(queue);
 			}
 		} finally {
 			perfLog.log(System.currentTimeMillis() - startTime, "BDb#getPubQueues");
 		}
 
 		return result;
 	}
 
 	/**
 	 * gets the number of messges contained in the queue
 	 * 
 	 * @param queueId
 	 * @return
 	 * @throws SQLException
 	 */
 	public int getNumberOfMessages(long queueId) throws SQLException {
 		long startTime = System.currentTimeMillis();
 
 		getNumMsgPerQueueStmt.setLong(1, queueId);
 		try (ResultSet rs = getNumMsgPerQueueStmt.executeQuery()) {
 			if (rs.next()) {
 				return rs.getInt(1);
 			}
 			throw new SQLException("No Value found for getNumMsgPerQueueStmt");
 		} finally {
 			perfLog.log(System.currentTimeMillis() - startTime, "BDb#getNumMessages");
 		}
 	}
 }
