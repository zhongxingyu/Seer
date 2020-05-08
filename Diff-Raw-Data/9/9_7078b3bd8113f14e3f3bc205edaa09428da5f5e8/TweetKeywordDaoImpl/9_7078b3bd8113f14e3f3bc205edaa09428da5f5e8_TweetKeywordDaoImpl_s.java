 package toctep.skynet.backend.dal.dao.impl.mysql;
 
 import java.sql.SQLException;
 
 import toctep.skynet.backend.dal.dao.TweetKeywordDao;
 import toctep.skynet.backend.dal.domain.Domain;
 import toctep.skynet.backend.dal.domain.DomainLongPk;
 import toctep.skynet.backend.dal.domain.TweetKeyword;
 
 import com.mysql.jdbc.Connection;
 import com.mysql.jdbc.Statement;
 
 public class TweetKeywordDaoImpl extends TweetKeywordDao {
 
 	@Override
 	public void insert(Domain domain) {
		Connection conn = (Connection) this.getConnection();
 		
 		TweetKeyword tweetKeyword = (TweetKeyword) domain;
 		
 		Statement stmt = null;
 		
 		try {
 			stmt = (Statement) conn.createStatement();
 			int id = stmt.executeUpdate(
 					"INSERT INTO " + tableName + " (tweet_id, value, keyword_id) " +
 					"VALUES (" + tweetKeyword.getTweetId() + ", " + 
 							tweetKeyword.getTweetKeywordValue()+ ", "+
 							tweetKeyword.getKeywordId()+")",
 					Statement.RETURN_GENERATED_KEYS
 				);
 			tweetKeyword.setId(id);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}	
 	}
 
 	@Override
 	public DomainLongPk select(long id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void update(Domain domain) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void delete(Domain domain) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean exists(Domain domain) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public int count() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 }
