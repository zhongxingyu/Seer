 package springbook.user.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.sql.DataSource;
 
 import org.springframework.dao.EmptyResultDataAccessException;
 
 import springbook.user.domain.User;
 
public class UserDao {
 	private DataSource dataSource;
 	
 	/*
 	 * DataSource
 	 * 	자바에서 DB 커넥션을 가져오는 오브젝트의 기능을 추상화해서 비슷한 용도로
 	 *  사용할 수 있게 만들어진 인터페이스
 	 */
 	public void setDataSource(DataSource dataSource) {
 		this.dataSource = dataSource;
 	}

 	public void add(User user) throws ClassNotFoundException, SQLException {
 		Connection c = this.dataSource.getConnection();
 
 		PreparedStatement ps = c.prepareStatement(
 			"insert into users(id, name, password) values(?,?,?)");
 		ps.setString(1, user.getId());
 		ps.setString(2, user.getName());
 		ps.setString(3, user.getPassword());
 
 		ps.executeUpdate();
 
 		ps.close();
 		c.close();
 	}
 
 	public User get(String id) throws ClassNotFoundException, SQLException {
 		Connection c = this.dataSource.getConnection();
 		PreparedStatement ps = c
 				.prepareStatement("select * from users where id = ?");
 		ps.setString(1, id);
 
 		ResultSet rs = ps.executeQuery();
 		
 		User user = null;
 		if (rs.next()) {
 			user = new User();
 			user.setId(rs.getString("id"));
 			user.setName(rs.getString("name"));
 			user.setPassword(rs.getString("password"));
 		}
 
 		rs.close();
 		ps.close();
 		c.close();
 		
 		if (user == null) throw new EmptyResultDataAccessException(1);
 		
 		return user;
 	}
 
 	public void deleteAll() throws SQLException {
 		Connection c = null;
 		PreparedStatement ps = null;
 		
 		try {
 			c = this.dataSource.getConnection();
			ps = c.prepareStatement("delete from users");
 			ps.executeUpdate();
 		} catch (SQLException e) {
 			throw e;
 		} finally {
 			if (ps != null) {
 				try {
 					ps.close();
 				} catch (SQLException e) {
 				}
 			}
 			if (c != null) {
 				try {
 					c.close();
 				} catch (SQLException e) {
 				}
 			}
 		}
 	}
 	
 	public int getCount() throws SQLException {
 		Connection c = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		
 		try {
 			c = dataSource.getConnection();
 			
 			ps = c.prepareStatement("select count(*) from users");
 			
 			rs = ps.executeQuery();
 			rs.next();
 			return rs.getInt(1);
 		} catch (SQLException e) {
 			throw e;
 		} finally {
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 				}
 			}
 			if (ps != null) {
 				try {
 					ps.close();
 				} catch (SQLException e) {
 				}
 			}
 			if (c != null) {
 				try {
 					c.close();
 				} catch (SQLException e) {
 				}
 			}
 		}
 	}
 
 }
