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
 		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
 
 		try (PreparedStatement statusStatement = connection.prepareStatement(
 				"INSERT INTO statuses (user_id, status) VALUES (?, ?)",
 				Statement.RETURN_GENERATED_KEYS);
 			 PreparedStatement coordinateStatement = connection.prepareStatement(
 					 "INSERT INTO coordinates (user_id, status_id, time, lat, lng) VALUES (?, ?, ?, ?, ?)")) {
 			statusStatement.setInt(1, user.getId());
 			statusStatement.setString(2, text);
 			if (statusStatement.executeUpdate() <= 0) {
 				return false;
 			}
 
 			int statusId;
 			ResultSet resultSet = statusStatement.getGeneratedKeys();
 			if (resultSet.next()) {
 				statusId = resultSet.getInt(1);
 			} else {
 				return false;
 			}
 
 			coordinateStatement.setInt(1, user.getId());
 			coordinateStatement.setInt(2, statusId);
 			coordinateStatement.setTimestamp(3, timestamp);
 			coordinateStatement.setDouble(4, latitude);
 			coordinateStatement.setDouble(5, longitude);
 
 			return coordinateStatement.executeUpdate() > 0;
 		}
 	}
 
 	/**
 	 * Returns actual coordinates and statuses of all users.
 	 *
 	 * @return list with statuses.
 	 * @throws SQLException thrown on query fail.
 	 */
 	public List<Coordinate> getActualStatuses(long timestamp) throws SQLException {
 		try (PreparedStatement statement = connection.prepareStatement(
 				"SELECT " +
 						"users.id AS user_id, " +
 						"users.login AS login, " +
 						"users.email AS email, " +
 						"users.isonline AS isonline, " +
 						"statuses.id AS status_id, " +
 						"statuses.status AS text, " +
 						"coordinates.id AS coordinate_id, " +
 						"coordinates.time AS time, " +
 						"coordinates.lat AS lat, " +
						"coordinates.lng AS lng " +
 						"FROM statuses " +
 						"JOIN coordinates ON (coordinates.status_id = statuses.id) " +
 						"JOIN users ON (statuses.user_id = users.id) " +
 						"WHERE coordinates.time = (SELECT " +
 						"	MAX(c2.time) " +
 						"	FROM coordinates AS c2 " +
 						"	WHERE c2.user_id = statuses.user_id)" +
 						"	AND coordinates.time > ? " +
 						"GROUP BY coordinates.user_id")) {
 			statement.setLong(1, timestamp);
 
 			return selectStatuses(statement);
 		}
 	}
 
 	public List<Coordinate> getOnlineStatuses() throws SQLException {
 		try (PreparedStatement statement = connection.prepareStatement(
 				"SELECT " +
 						"users.id AS user_id, " +
 						"users.login AS login, " +
 						"users.email AS email, " +
 						"1 AS isonline, " +
 						"statuses.id AS status_id, " +
 						"statuses.status AS text, " +
 						"coordinates.id AS coordinate_id, " +
 						"coordinates.time AS time, " +
 						"coordinates.lat AS lat, " +
						"coordinates.lng AS lng " +
 						"FROM statuses " +
 						"JOIN coordinates ON (coordinates.status_id = statuses.id) " +
 						"JOIN users ON (statuses.user_id = users.id) " +
 						"WHERE coordinates.time = (SELECT " +
 						"	MAX(c2.time) " +
 						"	FROM coordinates AS c2 " +
 						"	WHERE c2.user_id = statuses.user_id)" +
 						"	AND users.isonline = 1")) {
 			return selectStatuses(statement);
 		}
 	}
 
 	/**
 	 * Returns last 10 statuses of the specified user.
 	 *
 	 * @param userLogin user login.
 	 * @return list with statuses.
 	 * @throws SQLException thrown on query fail.
 	 */
 	public List<Coordinate> getHistoricalStatuses(String userLogin) throws SQLException {
 		try (PreparedStatement statement = connection.prepareStatement(
 				"SELECT " +
 						"users.id AS user_id, " +
 						"users.login AS login, " +
 						"users.email AS email, " +
 						"users.isonline AS isonline, " +
 						"statuses.id AS status_id, " +
 						"statuses.status AS text " +
 						"coordinates.id AS coordinate_id, " +
 						"coordinates.time AS time, " +
 						"coordinates.lat AS lat, " +
						"coordinates.lng AS lng " +
 						"FROM statuses " +
 						"JOIN users ON (statuses.user_id = users.id) " +
 						"JOIN (SELECT c2.status_id, MAX(c2.time) FROM coordinates c2" +
 						"	GROUP BY c2.status_id) AS max_times ON (max_times.status_id = statuses.id) " +
 						"JOIN coordinates ON (coordinates.status_id = max_times.status_id " +
 						"	AND coordinates.time = max_times.time)" +
 						"WHERE users.login = ? " +
 						"ORDER BY coordinates.time DESC " +
 						"LIMIT 10")) {
 			statement.setString(1, userLogin);
 
 			return selectStatuses(statement);
 		}
 	}
 
 	/**
 	 * Selects the coordinates and statuses from the database.
 	 *
 	 * @param statement query statement.
 	 * @return list of selected objects.
 	 * @throws SQLException thrown on query fail.
 	 */
 	private List<Coordinate> selectStatuses(PreparedStatement statement) throws SQLException {
 		try (ResultSet resultSet = statement.executeQuery()) {
 			List<Coordinate> result = new ArrayList<>();
 			while (resultSet.next()) {
 				Integer userId = resultSet.getInt("user_id");
 				Integer statusId = resultSet.getInt("status_id");
 				Integer coordinateId = resultSet.getInt("coordinate_id");
 				String login = resultSet.getString("login");
 				String email = resultSet.getString("email");
 				boolean isOnline = resultSet.getBoolean("isonline");
 				String text = resultSet.getString("text");
 				Timestamp dateTime = resultSet.getTimestamp("time");
 				double latitude = resultSet.getDouble("lat");
 				double longitude = resultSet.getDouble("lng");
 
 				User user = new User(userId, login, email, isOnline);
 				Status status = new Status(statusId, user, text);
 				Coordinate coordinate = new Coordinate(coordinateId, user, status, dateTime, latitude, longitude);
 
 				result.add(coordinate);
 			}
 
 			return result;
 		}
 	}
 }
