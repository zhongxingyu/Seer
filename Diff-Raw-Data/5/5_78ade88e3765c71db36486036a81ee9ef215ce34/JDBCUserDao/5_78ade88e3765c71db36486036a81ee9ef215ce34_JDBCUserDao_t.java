 package ar.edu.itba.paw.grupo1.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.apache.log4j.Logger;
 
 import ar.edu.itba.paw.grupo1.model.User;
 
 
 public class JDBCUserDao extends AbstractDao implements UserDao {
 	
 	private static final String UNIQUE_VIOLATION_STATE = "23505";
 	private static Logger logger = Logger.getLogger(JDBCUserDao.class);
 
 	public JDBCUserDao(Connection conn) {
 		super(conn);
 	}
 
 	@Override
 	public void register(String name, String surname, String email,
 			String phone, String username, String password)
 			throws UserAlreadyExistsException {
 		
 		
 		try {
 			PreparedStatement stmt = conn.prepareStatement(
 				"INSERT INTO users (name, surname, email, phone, username, password)" 
 					+ " VALUES(?, ?, ?, ?, ?, ?)",
 				Statement.RETURN_GENERATED_KEYS
 			);
 			
 			stmt.setString(1, name);
 			stmt.setString(2, surname);
 			stmt.setString(3, email);
 			stmt.setString(4, phone);
 			stmt.setString(5, username);
 			stmt.setString(6, password);
 			
 			stmt.execute();
 			
 		} catch (SQLException e) {
 			
 			if (UNIQUE_VIOLATION_STATE.equals(e.getSQLState())) {
 				throw new UserAlreadyExistsException();
 			} else {
 				logger.warn("Caught SQLException while trying to register user.", e);
				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	@Override
 	public User login(String username, String hash) {
 		
 		try {
 			PreparedStatement stmt = conn.prepareStatement(
 				"SELECT * FROM users WHERE username = ? AND password = ?"
 			);
 			
 			stmt.setString(1, username);
 			stmt.setString(2, hash);
 			
 			if (stmt.execute()) {
 				
 				ResultSet result = stmt.getResultSet();
 				if (result.next()) {
 					return userFromResult(result);
 				}
 			}
 			
 		} catch (SQLException e) {
 			logger.warn("Caught SQLException when trying login with user " + username + " pass " + hash, e);
			throw new RuntimeException(e);
 		}
 		
 		return null;
 	}
 
 	private User userFromResult(ResultSet result) throws SQLException {
 		return new User(
 			result.getInt("id"), 
 			result.getString("name"), 
 			result.getString("surname"), 
 			result.getString("email"), 
 			result.getString("phone"), 
 			result.getString("username"),
 			result.getString("password")
 		);
 	}
 
 	@Override
 	public User get(int userId) {
 		try {
 			PreparedStatement stmt = conn.prepareStatement(
 				"SELECT * FROM users WHERE id = ?"
 			);
 			
 			stmt.setInt(1, userId);
 			
 			if (stmt.execute()) {
 				
 				ResultSet result = stmt.getResultSet();
 				if (result.next()) {
 					return userFromResult(result);
 				}
 			}
 			
 		} catch (SQLException e) {
 			logger.warn("Caught SQLException when trying to get user with id " + userId, e);
			throw new RuntimeException(e);
 		}
 		
 		return null;
 	}
 }
