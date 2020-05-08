 package feedreader.persist;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import common.persist.DbUtils;
 import common.persist.EntityManager;
 import feedreader.User;
 
 public class UserEntityHandler implements EntityManager.EntityHandler {
 	
 	@Override
 	public void persist(EntityManager.QueryContext queryContext, Object entity) throws SQLException {
 		User user = (User) entity;
 		Connection cnn = null;
 		try {
 			cnn = queryContext.getConnection();
 			
 			PreparedStatement stmt = null;
 			try {
 				stmt = cnn.prepareStatement("insert into Users (email) values (?) returning id");
 				stmt.setString(1, user.getEmail());
 				
 				boolean hasResult = stmt.execute();
 				if(hasResult) {
 					ResultSet rst = null;
 					try {
 						rst = stmt.getResultSet();
 						if(rst.next()) {
 							user.setId(rst.getInt(1));
 						}
 					} finally {
 						DbUtils.close(rst);
 					}
 				}
 			} finally {
 				DbUtils.close(stmt);
 			}
 			
 		} finally {
 			queryContext.releaseConnection(cnn);
 		}
 	}
 	
 	@Override
 	public Object get(EntityManager.QueryContext queryContext, Object id) throws SQLException {
 		return getUserById(queryContext, (Integer)id);
 	}
 	
 	@Override
 	public List<Object> executeNamedQuery(EntityManager.QueryContext queryContext, String query, Object... parameters) throws SQLException {
 		if("getUserByEmail".equals(query)) {
 			return asList(getUserByEmail(queryContext, (String)parameters[0]));
 		}
 		
 		throw new IllegalArgumentException("Unknown query specified: " + query);
 	}
 	
 	private User getUserById(EntityManager.QueryContext queryContext, int id) throws SQLException {
 		User user = null;
 		Connection cnn = null;
 		try {
 			cnn = queryContext.getConnection();
 			
 			PreparedStatement stmt = null;
 			try {
 				stmt = cnn.prepareStatement("select id, email from Users where id = ? limit 1");
 				stmt.setInt(1, id);
 				
 				ResultSet rst = null;
 				try {
 					rst = stmt.executeQuery();
 					
 					if(rst.next()) {
 						user = createUser(rst);
 					}
 					
 				} finally {
 					DbUtils.close(rst);
 				}
 			} finally {
 				DbUtils.close(stmt);
 			}
 			
 		} finally {
 			queryContext.releaseConnection(cnn);
 		}
 		
 		return user;
 	}
 	
 	private User getUserByEmail(EntityManager.QueryContext queryContext, String email) throws SQLException {
 		User user = null;
 		Connection cnn = null;
 		try {
 			cnn = queryContext.getConnection();
 			
 			PreparedStatement stmt = null;
 			try {
 				stmt = cnn.prepareStatement("select id, email from Users where email = ? limit 1");
 				stmt.setString(1, email);
 				
 				ResultSet rst = null;
 				try {
 					rst = stmt.executeQuery();
 					
 					if(rst.next()) {
 						user = createUser(rst);
 					}
 					
 				} finally {
 					DbUtils.close(rst);
 				}
 			} finally {
 				DbUtils.close(stmt);
 			}
 			
 		} finally {
 			queryContext.releaseConnection(cnn);
 		}
 		
 		return user;
 	}
 	
 	private User createUser(ResultSet rst) throws SQLException {
 		User user = new User();
 		user.setId(rst.getInt("id"));
 		user.setEmail(rst.getString("email"));
 		return user;
 	}
 	
 	private List<Object> asList(User user) {
 		if(user == null) {
			return null;
 		}
 		ArrayList<Object> users = new ArrayList<Object>(1);
 		users.add(user);
 		return users;
 	}
 }
