 package db;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 
 import models.Change;
 
 
 public class CallGraphDb extends DbConnection
 {
 	public CallGraphDb()
 	{
 		super();
 	}
 	
 	/**
 	 * Gets all of the change objects associated including and before a given commit.
 	 * @param CommitId
 	 * @return
 	 */
 	public List<Change> getAllOwnerChangesBefore(String CommitId)
 	{
 		try 
 		{
 			LinkedList<Change> changes = new LinkedList<Change>();
 			String sql = "SELECT commit_id, file_id, owner_id, char_start, char_end, change_type FROM owners natural join commits where commit_date <= (select commit_date from commits where commit_id=?)" +
 					"and (branch_id is NULL OR branch_id=?) order by commit_date, commit_id, char_start;"; 
 			String[] parms = {CommitId, branchID};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			while(rs.next())
 			{
 				changes.add(new Change(rs.getString("owner_id"), rs.getString("commit_id"), Resources.ChangeType.valueOf(rs.getString("change_type")), rs.getString("file_id"), rs.getInt("char_start"), 
 						rs.getInt("char_end")));
 			}
 			return changes;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public List<Change> getAllFileOwnerChangesBefore(String FileId, String CommitId)
 	{
 		try 
 		{
 			LinkedList<Change> changes = new LinkedList<Change>();
 			String sql = "SELECT commit_id, file_id, owner_id, char_start, char_end, change_type FROM owners natural join commits where commit_date <= (select commit_date from commits where commit_id=?)" +
 					"and (branch_id is NULL OR branch_id=?) and file_id=? order by commit_date, commit_id, line_start;"; 
 			String[] parms = {CommitId, branchID, FileId};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			while(rs.next())
 			{
 				changes.add(new Change(rs.getString("owner_id"), rs.getString("commit_id"), 
 						Resources.ChangeType.valueOf(rs.getString("change_type")), rs.getString("file_id"),
 						rs.getInt("line_start"), rs.getInt("line_end")));
 			}
 			return changes;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public List<Change> getAllOwnersForFileAtCommit(String FileId, String CommitId)
 	{
 		try 
 		{
 			LinkedList<Change> changes = new LinkedList<Change>();
			String sql = "SELECT source_commit_id file_id, owner_id, char_start, char_end, change_type FROM owners natural join commits where commit_id=?" +
					"and (branch_id is NULL OR branch_id=?) and file_id=? order by commit_date, commit_id, line_start;"; 
 			String[] parms = {CommitId, branchID, FileId};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			while(rs.next())
 			{
 				changes.add(new Change(rs.getString("owner_id"), rs.getString("source_commit_id"), 
 						Resources.ChangeType.valueOf(rs.getString("change_type")), rs.getString("file_id"),
						rs.getInt("line_start"), rs.getInt("line_end")));
 			}
 			return changes;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Adds a new network record into the networks table for a pair of commits.
 	 * @param NewCommitId
 	 * @param OldCommitId
 	 */
 	public int addNetworkRecord(String NewCommitId, String OldCommitId)
 	{
 		try {
 			String sql = "INSERT INTO networks (new_commit_id, old_commit_id, network_id) VALUES (?, ?, default);";
 			String[] parms = {NewCommitId, OldCommitId};
 			execPrepared(sql, parms);
 			// get the id generated;
 			sql = "SELECT network_id from networks where new_commit_id=? and old_commit_id=?;";
 			ResultSet rs = execPreparedQuery(sql, parms);
 			if (!rs.next())
 				return -1;
 			else
 				return rs.getInt(1);
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return -1;
 		}
 	}
 	
 	/**
 	 * Adds a node record into the nodes table for a given User/UserfullName
 	 * @param UserId
 	 * @param UserFullName
 	 */
 	public void addNode(String UserId, int NetworkId)
 	{
 		try {
 			if (nodeExists(UserId))
 				return;
 			String sql = "INSERT INTO nodes (id, label, network_id) VALUES (?, ?, ?);";
 			PreparedStatement s = conn.prepareStatement(sql);
 			s.setString(1, UserId);
 			s.setString(2, UserId);
 			s.setInt(3, NetworkId);
 			s.execute();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean nodeExists(String UserId)
 	{
 		try {
 			String sql = "SELECT * FROM nodes WHERE id=?;";
 			String[] parms = {UserId};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			return rs.next();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public String getUserFullName(String UserId, int NetworkId)
 	{
 		try
 		{
 			String sql = "SELECT author from commits where author_email=?;";
 			String[] parms = {UserId};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			if(!rs.next())
 				return null;
 			else
 				return rs.getString("author");
 		}
 		catch(SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Adds an edge record between two users with the weight given.
 	 * @param UserId
 	 * @param UserSource
 	 * @param UserTarget
 	 * @param weight
 	 */
 	public void addEdge(String UserSource, String UserTarget, float weight, int NetworkId)
 	{
 		try {
 			String sql = "INSERT INTO edges (source, target, weight, network_id) VALUES (?, ?, ?, ?);";
 			PreparedStatement s = conn.prepareStatement(sql);
 			s.setString(1, UserSource);
 			s.setString(2, UserTarget);
 			s.setFloat(3, weight);
 			s.setInt(4, NetworkId);
 			s.execute();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
