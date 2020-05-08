 package toctep.skynet.backend.dal.dao.impl.mysql;
 
 import java.sql.SQLException;
 
 import toctep.skynet.backend.dal.dao.KeywordDao;
 import toctep.skynet.backend.dal.domain.Domain;
 import toctep.skynet.backend.dal.domain.DomainLongPk;
 import toctep.skynet.backend.dal.domain.Keyword;
 
 import com.mysql.jdbc.Connection;
 import com.mysql.jdbc.Statement;
 
 public class KeywordDaoImpl extends KeywordDao {
 
 	@Override
 	public void insert(Domain domain) {
		Connection conn = MySqlUtil.getInstance().getConnection();
 		
 		Keyword keyword = (Keyword) domain;
 		
 		Statement stmt = null;
 		
 		try {
 			stmt = (Statement) conn.createStatement();
 			int id = stmt.executeUpdate(
 					"INSERT INTO " + tableName + " (keyword) " +
 					"VALUES (" + keyword.getKeyword() + ")", 
 					Statement.RETURN_GENERATED_KEYS
 				);
 			keyword.setId(id);
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
