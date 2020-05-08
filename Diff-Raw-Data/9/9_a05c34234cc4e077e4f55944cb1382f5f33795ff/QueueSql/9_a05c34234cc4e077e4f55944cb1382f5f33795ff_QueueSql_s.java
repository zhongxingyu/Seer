 /*
  * Dec 1, 2005
  */
 package com.thinkparity.model.io.sql;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Vector;
 
 import org.jivesoftware.database.JiveID;
 
 import com.thinkparity.codebase.DateUtil;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.jabber.JabberId;
 
 import com.thinkparity.model.queue.QueueItem;
 
 
 /**
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 @JiveID(1002)
 public class QueueSql extends AbstractSql {
 
 	private static final String DELETE =
 		"delete from parityQueue where queueId = ?";
 
 	private static final String INSERT =
 		new StringBuffer("insert into parityQueue ")
 		.append("(queueId,username,queueMessageSize,queueMessage,createdBy,")
 		.append("updatedBy,updatedOn) ")
 		.append("values (?,?,?,?,?,?,CURRENT_TIMESTAMP)")
 		.toString();
 
 	private static final String SELECT = new StringBuffer()
 		.append("select queueId,username,queueMessageSize,queueMessage,")
 		.append("createdOn,updatedOn ")
 		.append("from parityQueue ")
 		.append("where queueId = ?").toString();
 
 	private static final String SELECT_USERNAME = new StringBuffer()
 		.append("select queueId,username,queueMessageSize,queueMessage,")
 		.append("createdOn,updatedOn ")
 		.append("from parityQueue ")
		.append("where username = ? order by createdOn desc").toString();
 
 	/**
 	 * Create a QueueSql.
 	 */
 	public QueueSql() { super(); }
 
 	public void delete(final Integer queueId) throws SQLException {
         logApiId();
 		logger.debug(queueId);
 		Connection cx = null;
 		PreparedStatement ps = null;
 		try {
 			cx = getCx();
 			ps = cx.prepareStatement(DELETE);
 			ps.setInt(1, queueId);
 			Assert.assertTrue("delete(Integer)", 1 == ps.executeUpdate());
 		}
 		finally { close(cx, ps); }
 	}
 
 	public Integer insert(final String username, final String message,
 			final JabberId createdBy) throws SQLException {
         logApiId();
 		logger.debug(username);
 		logger.debug(message);
 		Connection cx = null;
 		PreparedStatement ps = null;
 		try {
 			cx = getCx();
 			ps = prepare(cx, INSERT);
 
 			final Integer queueId = nextId(this);
 			set(ps, 1, queueId);
 			set(ps, 2, username);
 			set(ps, 3, message.length());
 			set(ps, 4, message);
 			set(ps, 5, createdBy);
 			set(ps, 6, createdBy);
 			if(1 != ps.executeUpdate())
 				throw new SQLException("Could not create queue entry.");
 
 			return queueId;
 		}
 		finally { close(cx, ps); }
 	}
 
 	public QueueItem select(final Integer queueId) throws SQLException {
         logApiId();
 		logger.debug(queueId);
 		Connection cx = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			cx = getCx();
 			ps = cx.prepareStatement(SELECT);
 			ps.setInt(1, queueId);
 			rs = ps.executeQuery();
 			if(rs.next()) { return extract(rs); }
 			else { return null; }
 		}
 		finally { close(cx, ps, rs); }
 	}
 
 	public Collection<QueueItem> select(final String username) throws SQLException {
         logApiId();
 		logger.debug(username);
 		Connection cx = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			cx = getCx();
 			ps = cx.prepareStatement(SELECT_USERNAME);
 			ps.setString(1, username);
 			rs = ps.executeQuery();
 			final Collection<QueueItem> items = new Vector<QueueItem>(7);
 			while(rs.next()) { items.add(extract(rs)); }
 			return items;
 		}
 		finally { close(cx, ps, rs); }
 	}
 
 	private QueueItem extract(final ResultSet rs) throws SQLException {
 		final Integer queueId = rs.getInt(1);
 		final String username = rs.getString(2);
 		final Integer queueMessageSize = rs.getInt(3);
 		final String queueMessage = rs.getString(4);
 		final Calendar createdOn = DateUtil.getInstance(rs.getTimestamp(5));		
 		final Calendar updatedOn =  DateUtil.getInstance(rs.getTimestamp(6));
 		return new QueueItem(createdOn, queueId, queueMessage, queueMessageSize,
 				updatedOn, username);
 	}
 }
