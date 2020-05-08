 package ru.allgage.geofriend.server;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Status data access object.
  */
 public class StatusDAO {
 	private Connection connection;
 
 	/**
 	 * Create data access object instance.
 	 *
 	 * @param connection server connection.
 	 */
 	public StatusDAO(Connection connection) {
 		this.connection = connection;
 	}
 
 	/**
 	 * Creates the status in the database.
 	 *
 	 * @param user      user.
 	 * @param latitude  latitude.
 	 * @param longitude longitude.
 	 * @param text      message text.
 	 * @throws SQLException thrown on query fail.
 	 */
 	public boolean create(User user, double latitude, double longitude, String text) throws SQLException {
 		try (PreparedStatement statement = connection.prepareStatement(
 				"INSERT INTO statuses (user_id, time, status, lat, lng) VALUES (?, (?), ?, ?, ?)")) {
 			statement.setInt(1, user.getId());
 			statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
 			statement.setString(3, text);
 			statement.setDouble(4, latitude);
 			statement.setDouble(5, longitude);
 
 			return statement.executeUpdate() > 0;
 		}
 	}
 
 	/**
 	 * Returns actual statuses of all users.
 	 *
 	 * @return list with statuses.
 	 * @throws SQLException thrown on query fail.
 	 */
 	public List<Status> getActualStatuses(long timestamp) throws SQLException {
 		try (PreparedStatement statement = connection.prepareStatement(
 				"SELECT " +
 						"users.id AS user_id, " +
 						"users.login AS login, " +
 						"users.email AS email, " +
 						"users.isonline AS isonline, " +
 						"statuses.id AS status_id, " +
 						"statuses.time AS time, " +
 						"statuses.lat AS lat, " +
 						"statuses.lng AS lng, " +
 						"statuses.status AS text " +
 						"FROM statuses " +
 						"JOIN users ON (statuses.user_id = users.id) " +
 						"WHERE statuses.time = (SELECT " +
 						"MAX(s2.time) " +
 						"FROM statuses AS s2 " +
 						"WHERE s2.user_id = statuses.user_id) AND statuses.time > (?) " +
 						"GROUP BY statuses.user_id")) {
 			statement.setLong(1, timestamp);
 
 			return selectStatuses(statement);
 		}
 	}
 
 	public List<Status> getOnlineStatuses() throws SQLException {
 		try (PreparedStatement statement = connection.prepareStatement(
 				"SELECT " +
 						"users.id AS user_id, " +
 						"users.login AS login, " +
 						"users.email AS email, " +
 						"1 AS isonline, " +
 						"statuses.id AS status_id, " +
 						"statuses.time AS time, " +
 						"statuses.lat AS lat, " +
 						"statuses.lng AS lng, " +
 						"statuses.status AS text " +
 						"FROM statuses " +
 						"JOIN users ON (statuses.user_id = users.id) " +
 						"WHERE statuses.time = (SELECT " +
 						"MAX(s2.time) " +
 						"FROM statuses AS s2 " +
 						"WHERE s2.user_id = statuses.user_id) AND users.isonline = 1 " +
 						"GROUP BY statuses.user_id")) {
 			return selectStatuses(statement);
 		}
 	}
 
 	/**
 	 * Returns last 10 statuses of a specified user.
 	 *
 	 * @param userLogin user login.
 	 * @return list with statuses.
 	 * @throws SQLException thrown on query fail.
 	 */
 	public List<Status> getHistoricalStatuses(String userLogin) throws SQLException {
 		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT " +
 						"users.id AS user_id, " +
 						"users.login AS login, " +
 						"users.email AS email, " +
 						"users.isonline AS isonline, " +
 						"statuses.id AS status_id, " +
 						"statuses.time AS time, " +
 						"statuses.lat AS lat, " +
 						"statuses.lng AS lng, " +
 						"statuses.status AS text " +
 						"FROM statuses " +
 						"JOIN users ON (statuses.user_id = users.id) " +
 						"WHERE users.login = ? " +
						"ORDER BY statuses.time DESC " +
						"LIMIT 10")) {
 			statement.setString(1, userLogin);
 
 			return selectStatuses(statement);
 		}
 	}
 
 	/**
 	 * Selects the statuses from the database.
 	 *
 	 * @param statement query statement.
 	 * @return list of selected objects.
 	 * @throws SQLException thrown on query fail.
 	 */
 	private List<Status> selectStatuses(PreparedStatement statement) throws SQLException {
 		try (ResultSet resultSet = statement.executeQuery()) {
 			List<Status> result = new ArrayList<>();
 			while (resultSet.next()) {
 				int userId = resultSet.getInt("user_id");
 				String login = resultSet.getString("login");
 				String email = resultSet.getString("email");
 				boolean isOnline = resultSet.getBoolean("isonline");
 				int statusId = resultSet.getInt("status_id");
 				Timestamp dateTime = resultSet.getTimestamp("time");
 				double latitude = resultSet.getDouble("lat");
 				double longitude = resultSet.getDouble("lng");
 				String text = resultSet.getString("text");
 
 				User user = new User(userId, login, email, isOnline);
 				Status status = new Status(statusId, user, dateTime, latitude, longitude, text);
 
 				result.add(status);
 			}
 
 			return result;
 		}
 	}
 }
