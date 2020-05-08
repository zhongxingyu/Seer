 package ru.allgage.geofriend.server;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * User data access object.
  */
 public class UserDAO {
 	private Connection connection;
 
 	/**
 	 * Create data access object instance.
 	 *
 	 * @param connection server connection.
 	 */
 	public UserDAO(Connection connection) {
 		this.connection = connection;
 	}
 
 	/**
 	 * Check whether a user with specified login and password exist in the database.
 	 *
 	 * @param login    user name.
 	 * @param password user password.
 	 * @return true if user exist.
 	 * @throws SQLException thrown on query fail.
 	 */
 	public boolean exist(String login, String password) throws SQLException {
 		try (PreparedStatement statement = connection.prepareStatement(
 				"SELECT * FROM users WHERE login = ? AND password = MD5(?)")) {
 			statement.setString(1, login);
 			statement.setString(2, password);
 
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next();
			}
 		}
 	}
 
 	/**
 	 * Creates the user.
 	 *
 	 * @param login    user name.
 	 * @param password user password.
 	 * @param email    user e-mail.
 	 * @return true if the user was created; false otherwise.
 	 */
 	public boolean create(String login, String password, String email) {
 		try (PreparedStatement statement = connection.prepareStatement(
 				"INSERT INTO users (login, password, email) VALUES(?, MD5(?), ?)")) {
 			statement.setString(1, login);
 			statement.setString(2, password);
 			statement.setString(3, email);
 
 			return statement.executeUpdate() > 0;
 		} catch (SQLException exception) {
 			exception.printStackTrace();
 			return false;
 		}
 	}
 }
