 package Beans;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import Exceptions.DeleteException;
 import Exceptions.InsertException;
 import Exceptions.SelectException;
 import Exceptions.UpdateException;
 
 /******************************************************
  * This is a model class that will set and get any * properties that have to do
  * with the race objects * that will be stored in the database. *
  ******************************************************/
 
 public class Race {
 	private Connection c;
 
 	/**
 	 * Default Constructor
 	 * 
 	 * This function is the constructor which will set up the connection
 	 * required to communicate with the DB.
 	 * 
 	 * @throws ClassNotFoundException
 	 * @throws SQLException
 	 */
 	public Race() throws ClassNotFoundException, SQLException {
 		c = Conn.getInstance().getConnection();
 	}
 
 	/**
 	 * Creates a race and adds it to the database. Returns the race id.
 	 * 
 	 * This function is the one that will initialize the race and store it in
 	 * the database. It will also get the race's ID.
 	 * 
 	 * @param name
 	 *            The name of the race.
 	 * @param dateTime
 	 *            The start date/time.
 	 * @param creator
 	 *            The creator's user ID.
 	 * @return The race ID.
 	 * @throws InsertException
 	 * @throws SelectException
 	 */
 	public int createRace(String name, Calendar dateTime, int creator)
 			throws InsertException, SelectException {
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		int rows = -1;
 
 		// Adds the new race to the database
 		try {
 			ps = c.prepareStatement("INSERT INTO Race(Name, Start, CreatorID,FINISHED) VALUES (?,?,?,0)");
 			ps.setString(1, name);
			java.sql.Timestamp temp = new java.sql.Timestamp(dateTime.getTimeInMillis());
 			ps.setTimestamp(2, temp);
 			ps.setInt(3, creator);
			
			// rows should be 1. Throw error otherwise
 			if ((rows = ps.executeUpdate()) != 1)
				throw new InsertException("Race wasn't updated in the database " + rows + " rows updated.");
			ps.close();
 		} catch (SQLException e) {
			throw new InsertException("Database Error/Failed to insert race");
 		}
 
 		// Retrieves the race id of the created race.
 		// TODO what if a race is deleted from the middle of the database. Is
 		// the new one's ID still the max?
 		try {
 			ps = c.prepareStatement("SELECT MAX(ID) FROM Race");
 			rs = ps.executeQuery();
 			ps.close();
 		} catch (SQLException e) {
 			throw new SelectException("Unable to get ID for created race");
 		}
 		
 		int raceID = -1;
 		try {
 			while (rs.next()) {
 				raceID = rs.getInt(1);
 			}
 			
 		} catch (SQLException e) {
 			throw new SelectException("Error on returned race ID");
 		}
 		
 		return raceID;
 	}
 
 	/**
 	 * Gets a RaceObj for a given race.
 	 * 
 	 * Given a race id and a creator id, this function returns a RaceObj that
 	 * corresponds to the race
 	 * 
 	 * @param raceId
 	 *            Race id of the race
 	 * @param userId
 	 *            User id of the creator of the race
 	 * @return A RaceObj corresponding to the given race.
 	 * @throws SelectException
 	 */
 	public RaceObj getRace(int raceId, int creatorId) throws SelectException {
 		PreparedStatement ps;
 		ResultSet rs = null;
 		RaceObj raceObj = new RaceObj();
 		
 		try {
 			ps = c.prepareStatement("SELECT name, start FROM race WHERE ID = ? AND creator = ?");
 			ps.setInt(1, raceId);
 			ps.setInt(2, creatorId);
 
 			rs = ps.executeQuery();
 		} catch (SQLException e) {
 			throw new SelectException("Unable to get RaceObj");
 		}
 
 		try {
 			while (rs.next()) {
 				raceObj.setName(rs.getString(1));
 				raceObj.setStart(rs.getTimestamp(2));
 			}
 			c.commit();
 			ps.close();
 		} catch (SQLException e) {
 			throw new SelectException("Error on returned RaceObj");
 		}
 
 		
 		raceObj.setCreatorId(creatorId);
 		raceObj.setId(raceId);
 		return raceObj;
 	}
 
 	/**
 	 * Gets a list of RaceObjs that the given user has/has not finished.
 	 * 
 	 * Given a user id, this function returns a list of RaceObjs that the user
 	 * has/has not already finished.
 	 * 
 	 * @param userId
 	 *            The user id that corresponds to the participant
 	 * @param isFinished
 	 *            Chooses between finished and unfinished races
 	 * @return A list of RaceObjs
 	 * @throws SelectException
 	 */
 	public List<RaceObj> getRacesObj(int userId, boolean isFinished)
 			throws SelectException {
 		List<RaceObj> results = new ArrayList<RaceObj>();
 		PreparedStatement ps;
 		ResultSet rs = null;
 
 		try {
 			ps = c.prepareStatement("SELECT r.id, r.name, r.start, r.creatorid, rs.score FROM Race r LEFT JOIN Racers rs ON (r.id = rs.raceID) WHERE userID = ? AND rs.attend = 1 AND r.finished = ? ORDER BY r.start DESC");
 			ps.setInt(1, userId);
 			ps.setBoolean(2, isFinished);
 
 			
 			rs = ps.executeQuery();
 		} catch (SQLException e) {
 			throw new SelectException("Unable to get races");
 		}
 
 		try {
 			while (rs.next()) {
 				RaceObj raceObj = new RaceObj();
 
 				raceObj.setId(rs.getInt(1));
 				raceObj.setName(rs.getString(2));
 				raceObj.setStart(rs.getTimestamp(3));
 				raceObj.setCreatorId(rs.getInt(4));
 				raceObj.setScore(rs.getInt(5));
 				results.add(raceObj);
 			}
 			c.commit();
 			ps.close();
 		} catch (SQLException e) {
 			throw new SelectException("Error on returned races");
 		}
 
 		return results;
 	}
 
 	/**
 	 * Deletes a race from the database.
 	 * 
 	 * Given a race id, this functions deletes the entry in the database that
 	 * matches the id.
 	 * 
 	 * @param raceId
 	 * @throws DeleteException
 	 */
 	public void deleteRace(int raceId) throws DeleteException {
 		PreparedStatement ps = null;
 		int rows = -1;
 
 		try {
 			ps = c.prepareStatement("DELETE * FROM Race WHERE id = ?");
 			ps.setInt(1, raceId);
 		} catch (SQLException e) {
 			throw new DeleteException("Failed to delete race");
 		}
 
 		try {
 			if ((rows = ps.executeUpdate()) != 1)
 				throw new DeleteException(
 						"Race wasn't deleted in the database " + rows
 								+ " rows changed.");
 		} catch (SQLException e) {
 			throw new DeleteException("Database Error");
 		}
 
 		try {
 			c.commit();
 			ps.close();
 		} catch (SQLException e) {
 			throw new DeleteException("Database Error");
 		}
 	}
 
 	/**
 	 * Gets race's name.
 	 * 
 	 * Given a race id, this method obtains the name of the race.
 	 * 
 	 * @param raceId
 	 *            The race's id
 	 * @return The race's name
 	 * @throws SelectException
 	 */
 	public String getName(int raceId) throws SelectException {
 		PreparedStatement ps;
 		ResultSet rs = null;
 		String result = "";
 
 		try {
 			ps = c.prepareStatement("SELECT name FROM Race WHERE ID = ?");
 			ps.setInt(1, raceId);
 
 			rs = ps.executeQuery();
 		} catch (SQLException e) {
 			throw new SelectException("Unable to get race name");
 		}
 
 		try {
 			while (rs.next()) {
 				result = rs.getString(1);
 			}
 			c.commit();
 			ps.close();
 		} catch (SQLException e) {
 			throw new SelectException("Error on returned race name");
 		}
 		
 		return result;
 	}
 
 	/**
 	 * Sets finished status.
 	 * 
 	 * Given a race's id, this sets the finished status to finished/not
 	 * finished.
 	 * 
 	 * @param raceId
 	 *            The race's id
 	 * @param status
 	 *            Whether the race is finished or not
 	 * @throws UpdateException
 	 */
 	public void setFinished(int raceId, boolean status) throws UpdateException {
 		PreparedStatement ps = null;
 		int rows = -1;
 
 		try {
 			ps = c.prepareStatement("UPDATE Race SET finished = ? WHERE ID = ?");
 			ps.setBoolean(1, status);
 			ps.setInt(2, raceId);
 		} catch (SQLException e) {
 			throw new UpdateException("Unable to update the status");
 		}
 
 		// throw error if fail
 		try {
 			if ((rows = ps.executeUpdate()) != 1)
 				throw new UpdateException(
 						"Status wasn't updated in the database " + rows
 								+ " rows updated.");
 		} catch (SQLException e) {
 			throw new UpdateException("Failed to update the database");
 		}
 
 		try {
 			c.commit();
 			ps.close();
 		} catch (SQLException e) {
 			throw new UpdateException("Database Error");
 		}
 	}
 }
