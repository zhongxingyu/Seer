 package Beans;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import Exceptions.InsertException;
 import Exceptions.SelectException;
 import Exceptions.UpdateException;
 
 /**********************************************
  * Race class that contains all attributes * a race may contain and is mapped to
  * the DB *
  **********************************************/
 
 public class CheckIn {
 	private Connection c;
 
 	/**
 	 * Default constructor. Sets up the connection.
 	 */
 	public CheckIn() {
 		c = Conn.getInstance().getConnection();
 	}
 
 	/**
 	 * Add a check-in to the database.
 	 * 
 	 * Given a user id and race id (a check-in's primary keys) as well as a
 	 * comment, location and picture url, this method will add a check-in with
 	 * this information to the database.
 	 * 
 	 * @param userId		ID of the user creating this check-in
 	 * @param raceId		ID of the race this check-in was created in
 	 * @param picture		Url of an image the user wants to add to this check-in
 	 * @param comment		A comment the user wants to associate with this check-in
 	 * @param geoLocation	The location of this check-in
 	 * @throws InsertException
 	 */
 	public void addCheckIn(int userId, int raceId, String picture,
 			String comment, String geoLocation) throws InsertException {
 		PreparedStatement ps;
 		int rows = -1;
 
 		try {
 			ps = c.prepareStatement("INSERT INTO CheckIn(userid, raceid, picture, comment, geolocation) VALUES (?,?,?,?,?)");
 
 			ps.setInt(1, userId);
 			ps.setInt(2, raceId);
 			ps.setString(3, picture);
 			ps.setString(4, comment);
 			ps.setString(5, geoLocation);
 
 		} catch (SQLException e) {
 			throw new InsertException("Failed to insert check-in");
 		}
 		try {
 			if ((rows = ps.executeUpdate()) != 1)
 				throw new InsertException(
 						"CheckIn wasn't updated in the database " + rows
 								+ " rows updated.");
 		} catch (SQLException e) {
 			throw new InsertException("Database error");
 		}
 		try {
 			c.commit();
 			ps.close();
 		} catch (SQLException e) {
 			throw new InsertException("Database error");
 		}
 
 	}
 
 	/**
 	 * Adds a check-in to the database.
 	 * 
 	 * Given a CheckInObj, this adds a check-in to the database based on the
 	 * object.
 	 * 
 	 * @param checkInObj
 	 *            The check-in to add to the database.
 	 * @throws InsertException
 	 */
 	public void addCheckIn(CheckInObj checkInObj) throws InsertException {
 		addCheckIn(checkInObj.getUserID(), checkInObj.getRaceID(),
 				checkInObj.getPic(), checkInObj.getComment(),
 				checkInObj.getLocation());
 	}
 
 	/**
 	 * Gets all the check-ins for a race.
 	 * 
 	 * Given a race id, this gets all the check-ins for that race. Returns all
 	 * the check-in IDs.
 	 * 
 	 * @param raceId
 	 *            The race id for all the check-ins
 	 * @return A list of all the check-in IDs
 	 * @throws SelectException
 	 */
 	public List<Integer> getCheckInIDs(int raceId) throws SelectException {
 		PreparedStatement ps;
 		List<Integer> results = new ArrayList<Integer>();
 		ResultSet rs = null;
 
 		try {
 			ps = c.prepareStatement("SELECT ID FROM CheckIn WHERE raceId = ?");
 			ps.setInt(1, raceId);
 
 			rs = ps.executeQuery();
 			ps.close();
 			c.commit();
 		} catch (SQLException e) {
 			throw new SelectException("Unable to get check-ins");
 		}
 
 		try {
 			while (rs.next()) {
 				Integer temp = rs.getInt(1);
 				results.add(temp);
 			}
 		} catch (SQLException e) {
 			throw new SelectException("Error on returned check-ins");
 		}
 
 		return results;
 	}
 
 	/**
 	 * Gets all the check-ins for a race.
 	 * 
 	 * Given a race id, this gets all the check-ins for that race. Returns a
 	 * list of CheckInObj.
 	 * 
 	 * @param raceId
 	 *            The race id for all the check-ins
 	 * @return A list of CheckInObj
 	 * @throws SelectException
 	 */
 	public List<CheckInObj> getCheckInObjects(int raceId)
 			throws SelectException {
 		List<CheckInObj> results = new ArrayList<CheckInObj>();
 		PreparedStatement ps;
 		ResultSet rs = null;
 
 		try {
 			ps = c.prepareStatement("SELECT id, userid, picture, comment, geolocation FROM CheckIn WHERE raceid = ?");
 			ps.setInt(1, raceId);
 
 			rs = ps.executeQuery();
 			ps.close();
 		} catch (SQLException e) {
 			throw new SelectException("Unable to get check-ins");
 		}
 
 		try {
 			while (rs.next()) {
 				CheckInObj checkInObj = new CheckInObj();
 				checkInObj.setID(rs.getInt(1));
 				checkInObj.setUserID(rs.getInt(2));
 				checkInObj.setPic(rs.getString(3));
 				checkInObj.setComment(rs.getString(4));
 				checkInObj.setLocation(rs.getString(5));
 				checkInObj.setRaceID(raceId);
 
 				results.add(checkInObj);
 			}
 			c.commit();
 			ps.close();
 		} catch (SQLException e) {
 			throw new SelectException("Error on returned check-ins");
 		}
 
 		return results;
 	}
 
 	/**
 	 * Gets user id
 	 * 
 	 * Given a check-in id, this method obtains the user id.
 	 * 
 	 * @param checkInId
 	 *            The check-in's id
 	 * @return The user id of the check-in
 	 * @throws SelectException
 	 */
 	public int getUserId(int checkInId) throws SelectException {
 		PreparedStatement ps;
 		ResultSet rs = null;
 		int result = -1;
 
 		try {
 			ps = c.prepareStatement("SELECT userId FROM CheckIn WHERE ID = ?");
 			ps.setInt(1, checkInId);
 
 			rs = ps.executeQuery();
 			ps.close();
 		} catch (SQLException e) {
 			throw new SelectException("Unable to select user id");
 		}
 
 		try {
 			while (rs.next()) {
 				result = rs.getInt(1);
 			}
 			c.commit();
 			ps.close();
 		} catch (SQLException e) {
 			throw new SelectException("Error on returned user id");
 		}
 
 		return result;
 	}
 
 	/**
 	 * Gets race id
 	 * 
 	 * Given a check-in's id, this returns the race id.
 	 * 
 	 * @param checkInId
 	 *            The check-in's id
 	 * @return The race id
 	 * @throws SelectException
 	 */
 	public int getRaceId(int checkInId) throws SelectException {
 		PreparedStatement ps;
 		ResultSet rs = null;
 		int result = -1;
 
 		try {
 			ps = c.prepareStatement("SELECT raceId FROM CheckIn WHERE ID = ?");
 			ps.setInt(1, checkInId);
 
 			rs = ps.executeQuery();
 			ps.close();
 		} catch (SQLException e) {
 			throw new SelectException("Unable to select race id");
 		}
 
 		try {
 			while (rs.next()) {
 				result = rs.getInt(1);
 			}
 			c.commit();
 			ps.close();
 		} catch (SQLException e) {
 			throw new SelectException("Error on returned race id");
 		}
 
 		return result;
 	}
 
 	/**
 	 * Gets pic url
 	 * 
 	 * Given a check-in's id, this returns the pic url.
 	 * 
 	 * @param checkInId
 	 *            The check-in's id
 	 * @return The pic url
 	 * @throws SelectException
 	 */
 	public String getPic(int checkInId) throws SelectException {
 		PreparedStatement ps;
 		ResultSet rs;
 		String result = "";
 
 		try {
 			ps = c.prepareStatement("SELECT picture FROM CheckIn WHERE ID = ?");
 			ps.setInt(1, checkInId);
 
 			rs = ps.executeQuery();
 			ps.close();
 		} catch (SQLException e) {
 			throw new SelectException("Unable to select pic url");
 		}
 
 		try {
 			while (rs.next()) {
 				result = rs.getString(1);
 			}
 			c.commit();
 			ps.close();
 		} catch (SQLException e) {
 			throw new SelectException("Error on returned pic url");
 		}
 
 		return result;
 	}
 
 	/**
 	 * Sets pic url
 	 * 
 	 * Given a check-in's id, this sets the pic url to the input string.
 	 * 
 	 * @param checkInId
 	 *            The check-in's id
 	 * @param pic
 	 *            The new pic url
 	 * @throws UpdateException
 	 */
 	public void setPic(int checkInId, String pic) throws UpdateException {
 		PreparedStatement ps = null;
 		int rows = -1;
 
 		try {
 			ps = c.prepareStatement("UPDATE Checkin SET picture = ? WHERE ID = ?");
 			ps.setString(1, pic);
 			ps.setInt(2, checkInId);
 		} catch (SQLException e) {
 			throw new UpdateException("Unable to update the pic url");
 		}
 
 		try {
 			if (ps.executeUpdate() == -1)
 				throw new UpdateException(
 						"Pic url wasn't updated in the database. " + rows
 								+ " rows updated.");
 		} catch (SQLException e) {
 			throw new UpdateException("Failed to update database");
 		}
 
 		try {
 			c.commit();
 			ps.close();
 		} catch (SQLException e) {
 			throw new UpdateException("Database error");
 		}
 
 	}
 
 	/**
 	 * Gets comment
 	 * 
 	 * Given a check-in's id, this returns the comment.
 	 * 
 	 * @param checkInId
 	 *            The check-in's id
 	 * @return The comment
 	 * @throws SelectException
 	 */
 	public String getComment(int checkInId) throws SelectException {
 		PreparedStatement ps;
 		ResultSet rs = null;
 		String result = "";
 
 		try {
 			ps = c.prepareStatement("SELECT comment FROM CheckIn WHERE ID = ?");
 			ps.setInt(1, checkInId);
 
 			rs = ps.executeQuery();
 			ps.close();
 		} catch (SQLException e) {
 			throw new SelectException("Unable to select comment");
 		}
 
 		try {
 			while (rs.next()) {
 				result = rs.getString(1);
 			}
 			c.commit();
 			ps.close();
 		} catch (SQLException e) {
 			throw new SelectException("Error on returned comment");
 		}
 
 		return result;
 	}
 
 	/**
 	 * Sets comment
 	 * 
 	 * Given a check-in's id, this sets the comment to the input string.
 	 * 
 	 * @param checkInId
 	 *            The check-in's id
 	 * @param comment
 	 *            The new comment
 	 * @throws UpdateException
 	 */
 	public void setComment(int checkInId, String comment)
 			throws UpdateException {
 		PreparedStatement ps = null;
 		int rows = -1;
 
 		try {
 			ps = c.prepareStatement("UPDATE CheckIn SET comment = ? WHERE ID = ?");
 			ps.setString(1, comment);
 			ps.setInt(2, checkInId);
 		} catch (SQLException e) {
 			throw new UpdateException("Unable to update the comment");
 		}
 
 		// throw error if fail
 		try {
 			if (ps.executeUpdate() == -1)
 				throw new UpdateException(
 						"Comment wasn't updated in the database " + rows
 								+ " rows updated.");
 		} catch (SQLException e) {
 			throw new UpdateException("Failed to update database");
 		}
 
 		try {
 			c.commit();
 			ps.close();
 		} catch (SQLException e) {
 			throw new UpdateException("Database error");
 		}
 
 	}
 
 	/**
 	 * Gets location
 	 * 
 	 * Given a check-in's id, this returns the location.
 	 * 
 	 * @param checkInId
 	 *            The check-in's id
 	 * @return The location
 	 * @throws SelectException
 	 */
 	public String getLocation(int checkInId) throws SelectException {
 		PreparedStatement ps;
 		ResultSet rs = null;
 		String result = "";
 
 		try {
 			ps = c.prepareStatement("SELECT geolocation FROM CheckIn WHERE ID = ?");
 			ps.setInt(1, checkInId);
 			rs = ps.executeQuery();
 			ps.close();
 		} catch (SQLException e) {
 			throw new SelectException("Unable to select location");
 		}
 
 		try {
 			while (rs.next()) {
 				result = rs.getString(1);
 			}
 			c.commit();
 			ps.close();
 		} catch (SQLException e) {
 			throw new SelectException("Error on returned location");
 		}
 
 		return result;
 	}
 
 	/**
 	 * Sets location
 	 * 
 	 * Given a check-in's id, this sets the comment to the input string.
 	 * 
 	 * @param checkInId
 	 *            The check-in's id
 	 * @param comment
 	 *            The new location
 	 * @throws UpdateException
 	 */
 	public void setLocation(int checkInId, String location)
 			throws UpdateException {
 		PreparedStatement ps = null;
 		int rows = -1;
 		
 		try {
 			ps = c.prepareStatement("UPDATE CheckIn SET location = ? WHERE ID = ?");
 			ps.setString(1, location);
 			ps.setInt(2, checkInId);
 		} catch (SQLException e) {
 			throw new UpdateException("Unable to update the location");
 		}
 
 		// throw error if fail
 		try {
 			if (ps.executeUpdate() == -1)
 				throw new UpdateException(
 						"location wasn't updated in the database " + rows
 								+ " rows updated.");
 		} catch (SQLException e) {
 			throw new UpdateException("Failed to update database");
 		}
 
 		try {
 			c.commit();
 			ps.close();
 		} catch (SQLException e) {
 			throw new UpdateException("Database error");
 		}
 	}
 }
