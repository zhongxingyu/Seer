 package com.webquiz.data;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import com.webquiz.model.User;
 
 public class UserDB {
 
 	/**
 	 * Returns matching User object from the database
 	 * 
 	 * @param username
 	 * @param password
 	 * @return User or null if not found
 	 */
 	public static User getUser(String username, String password) {
 		ConnectionPool pool = ConnectionPool.getInstance();
 		Connection connection = pool.getConnection();
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 
 		String query = "SELECT id FROM user WHERE username = ? AND password = ?";
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setString(1, username);
 			ps.setString(2, password);
 			rs = ps.executeQuery();
 			if (rs.next()) {
 				User user = new User(username, password, rs.getInt("id"));
 				return user;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			DBUtil.closeResultSet(rs);
 			DBUtil.closePreparedStatement(ps);
 			pool.freeConnection(connection);
 		}
 		return null;
 	}
 
 	public static boolean addUser(String username, String password) {
 		ConnectionPool pool = ConnectionPool.getInstance();
 		Connection connection = pool.getConnection();
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 
		String query = "SELECT id FROM user WHERE username = ?";
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setString(1, username);
 			rs = ps.executeQuery();
 			if (rs.next()) {
 				System.out.println("place 1");
 				return false;
 			} else {
 				String insert = "INSERT INTO user (username, password) VALUES ('"
 						+ username + "', '" + password + "')";
 				java.sql.Statement stmt = connection.createStatement();
 				int c = stmt.executeUpdate(insert);
 				System.out.println("place 2 " + c);
 				return true;
 
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			DBUtil.closeResultSet(rs);
 			DBUtil.closePreparedStatement(ps);
 			pool.freeConnection(connection);
 		}
 		System.out.println("place 3");
 		return false;
 	}
 }
