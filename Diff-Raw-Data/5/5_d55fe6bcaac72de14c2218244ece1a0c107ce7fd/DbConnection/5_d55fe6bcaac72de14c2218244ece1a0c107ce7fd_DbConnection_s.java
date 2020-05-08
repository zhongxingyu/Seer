 package db;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import models.Commit;
 import db.Resources.ChangeType;
 
 public abstract class DbConnection {
 	protected ScriptRunner sr;
 	public static Connection conn = null;
 	protected String branchName = null;
 	protected String branchID = null;
 	public static Statement currentBatch;
 
 	protected DbConnection() 
 	{
 		try 
 		{
 			Class.forName("org.postgresql.Driver").newInstance();
 		} 
 		catch (InstantiationException e) 
 		{
 			e.printStackTrace();
 		} 
 		catch (IllegalAccessException e) 
 		{
 			e.printStackTrace();
 		} 
 		catch (ClassNotFoundException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public String getBranchID() {
 		return branchID;
 	}
 	
 	public void setBranchID(String branchID) {
 		this.branchID = branchID;
 	}
 
 	public String getBranchName() {
 		return branchName;
 	}
 
 	/**
 	 * Should be called AFTER @see {@link #connect(String)}, as it also does 
 	 * a lookup on the branchID and sets it behind the scenes.
 	 * Also does a lookup in the branches table for 
 	 * @param branchName
 	 */
 	public void setBranchName(String branchName) {
 		this.branchName = branchName;
 		try
 		{
 			String[] params = {branchName};
 			ResultSet rs = this.execPreparedQuery("SELECT branch_id from branches where branch_name ~ ? LIMIT 1", params);
 			rs.next();
 			setBranchID(rs.getString(1));
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Executes a string of SQL on the current database
 	 * NOTE: this assumes your sql is valid.
 	 * @param sql
 	 * @return true if successful
 	 */
 	public boolean exec(String sql)
 	{
 		try {
 			PreparedStatement s = conn.prepareStatement(sql);
 			s.execute();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean execPrepared(String sql, String[] params)
 	{
 		try {
 			PreparedStatement s = conn.prepareStatement(sql);
 			for (int i = 1;i <= params.length;i++)
 			{
 				s.setString(i, params[i-1]);
 			}
 			s.execute();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Executes the given query with escaped values in String[] params in place of
 	 * ? characters in sql.
 	 * @param sql ex. "SELECT * FROM something where my_column=?"
 	 * @param params ex. {"braden's work"}
 	 * @return Query ResultSet on success, null otherwise
 	 */
 	public ResultSet execPreparedQuery(String sql, String[] params)
 	{
 		try {
 			PreparedStatement s = conn.prepareStatement(sql);
 			for (int i = 1;i <= params.length;i++)
 			{
 				s.setString(i, params[i-1]);
 			}
 			return s.executeQuery();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Connects to the given database.  
 	 * @param dbName
 	 * @return true if successful
 	 */
 	public boolean connect(String dbName)
 	{
 		try {
 			conn = DriverManager.getConnection(Resources.dbUrl + dbName.toLowerCase(), Resources.dbUser, Resources.dbPassword);
 			sr = new ScriptRunner(conn, false, true);
 			sr.setLogWriter(null);
 			currentBatch = conn.createStatement();
 		} 
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Close all connection
 	 */
 	public boolean close()
 	{
 		try {
 			conn.close();
 			return true;
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	/**
 	 * Returns a HashMap<(filePath), (fileContents)>
 	 * @param commitID
 	 * @return
 	 */
 	public Set<String> getCommitChangedFiles(String commitID)
 	{
 		Set<String> files = new HashSet<String>();
 		try {
 			String sql = "SELECT file_id FROM changes where commit_id=?;";
 			String[] params = {commitID};
 			ResultSet rs = execPreparedQuery(sql, params);
 			while(rs.next())
 			{
 				files.add(rs.getString("file_id"));
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 		return files;
 	}
 
 	/**
 	 * Returns an ordered map of <CommitId, Set<ChangedFilePaths>> before a given commitID
 	 * @param commitID
 	 * @param ascending
 	 * @return
 	 */
 	public Map<String, Set<String>> getCommitsBeforeChanges(String commitID, boolean ascending, boolean inclusive)
 	{
 		try{
 			Map<String, Set<String>> changes = new LinkedHashMap<String, Set<String>>();
 			String inclusiveStr = " ";
 			if (inclusive)
 				inclusiveStr = "= ";
 			String sql = "SELECT commit_id, file_id from changes natural join commits where " +
 					"(branch_id=? or branch_id is NULL) and commit_date <" + inclusiveStr + 
 					"(select commit_date from commits where commit_id=? and " +
 					"(branch_id=? OR branch_id is NULL)) ORDER BY commit_date";
 			if (!ascending)
 				sql += " desc";
 			String[] params = {this.branchID, commitID, this.branchID};
 			ResultSet rs = execPreparedQuery(sql, params);
 			String currentCommitId;
 			Set<String> currentFileset;
 			if (!rs.next())
 				return changes;
 			currentFileset = new HashSet<String>();
 			currentCommitId = rs.getString("commit_id");
 			currentFileset.add(rs.getString("file_id"));
 			while(rs.next())
 			{
 				if (rs.getString("commit_id").equals(currentCommitId))
 				{
 					// append to the current commit
 					currentFileset.add(rs.getString("file_id"));
 				}
 				else
 				{
 					// start a new one
 					changes.put(currentCommitId, currentFileset);
 					currentFileset = new HashSet<String>();
 					currentCommitId = rs.getString("commit_id");
 					currentFileset.add(rs.getString("file_id"));
 				}
 			}
 			changes.put(currentCommitId, currentFileset);
 			return changes;
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Returns an ordered map of <Commit, Set<ChangedFilePaths>> before a given commitID
 	 * @param commitID
 	 * @param ascending
 	 * @return
 	 */
 	public Map<Commit, Map<String, Resources.ChangeType>> getCommitObjectsBeforeChanges(String commitID, boolean ascending, boolean inclusive)
 	{
 		try{
 			Map<Commit, Map<String, Resources.ChangeType>> changes = new LinkedHashMap<Commit, Map<String, Resources.ChangeType>>();
 			String inclusiveStr = " ";
 			if (inclusive)
 				inclusiveStr = "= ";
 			String sql = "SELECT commit_id, author, author_email, comments, commit_date, branch_id, file_id, change_type from changes natural join commits where " +
 					"(branch_id=? or branch_id is NULL) and commit_date <" + inclusiveStr + 
 					"(select commit_date from commits where commit_id=? and " +
 					"(branch_id=? OR branch_id is NULL)) ORDER BY commit_date";
 			if (!ascending)
 				sql += " desc";
 			String[] params = {this.branchID, commitID, this.branchID};
 			ResultSet rs = execPreparedQuery(sql, params);
 			Commit currentCommit;
 			Map<String, Resources.ChangeType> currentFileset;
 			if (!rs.next())
 				return changes;
 			currentFileset = new HashMap<String, Resources.ChangeType>();
 			currentCommit = new Commit(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getTimestamp(5), rs.getString(6));
 			currentFileset.put(rs.getString("file_id"), Resources.ChangeType.valueOf(rs.getString("change_type")));
 			while(rs.next())
 			{
 				if (rs.getString("commit_id").equals(currentCommit.getCommit_id()))
 				{
 					// append to the current commit
 					currentFileset.put(rs.getString("file_id"), Resources.ChangeType.valueOf(rs.getString("change_type")));
 				}
 				else
 				{
 					// start a new one
 					changes.put(currentCommit, currentFileset);
 					currentFileset = new HashMap<String, Resources.ChangeType>();
 					currentCommit = new Commit(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getTimestamp(5), rs.getString(6));
 					currentFileset.put(rs.getString("file_id"), Resources.ChangeType.valueOf(rs.getString("change_type")));
 				}
 			}
 			changes.put(currentCommit, currentFileset);
 			return changes;
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Return a list of commits and their changed files between any 2 commits, order matters.
 	 * @param beforeCommitID
 	 * @param afterCommitID
 	 * @param ascending
 	 * @return
 	 */
 	public Map<String, Set<String>> getCommitsBeforeAndAfterChanges(String beforeCommitID, String afterCommitID, boolean ascending, boolean inclusive)
 	{
 		try{
 			Map<String, Set<String>> changes = new LinkedHashMap<String, Set<String>>();
 			String inclusiveStr = " ";
 			if (inclusive)
 				inclusiveStr = "= ";
 			String sql = "SELECT commit_id, file_id from changes natural join commits where " +
 					"(branch_id=? or branch_id is NULL) and commit_date <" + inclusiveStr + 
 					"(select commit_date from commits where commit_id=? and " +
 					"(branch_id=? OR branch_id is NULL)) and commit_date >" + inclusiveStr + 
 					"(select commit_date from commits where commit_id=? and " +
 					"(branch_id=? or branch_id is NULL)) ORDER BY commit_date";
 			if (!ascending)
 				sql += " desc";
 			
 			String[] params = {this.branchID, beforeCommitID, this.branchID, afterCommitID, this.branchID};
 			ResultSet rs = execPreparedQuery(sql, params);
 			String currentCommitId;
 			Set<String> currentFileset;
 			if (!rs.next())
 				return changes;
 			currentFileset = new HashSet<String>();
 			currentCommitId = rs.getString("commit_id");
 			currentFileset.add(rs.getString("file_id"));
 			while(rs.next())
 			{
 				if (rs.getString("commit_id").equals(currentCommitId))
 				{
 					// append to the current commit
 					currentFileset.add(rs.getString("file_id"));
 				}
 				else
 				{
 					// start a new one
 					changes.put(currentCommitId, currentFileset);
 					currentFileset = new HashSet<String>();
 					currentCommitId = rs.getString("commit_id");
 					currentFileset.add(rs.getString("file_id"));
 				}
 			}
 			changes.put(currentCommitId, currentFileset);
 			return changes;
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Return a list of commits and their changed files between any 2 commits, order matters.
 	 * @param beforeCommitID
 	 * @param afterCommitID
 	 * @param ascending
 	 * @return
 	 */
 	public Map<Commit, Map<String, Resources.ChangeType>> getCommitObjectsBeforeAndAfterChanges(String beforeCommitID, String afterCommitID, boolean ascending, boolean inclusive)
 	{
 		try{
 			Map<Commit, Map<String, Resources.ChangeType>> changes = new LinkedHashMap<Commit, Map<String, Resources.ChangeType>>();
 			String inclusiveStr = " ";
 			if (inclusive)
 				inclusiveStr = "= ";
 			String sql = "SELECT commit_id, author, author_email, comments, commit_date, branch_id, file_id, change_type from changes natural join commits where " +
 					"(branch_id=? or branch_id is NULL) and commit_date <" + inclusiveStr + 
 					"(select commit_date from commits where commit_id=? and " +
 					"(branch_id=? OR branch_id is NULL)) and commit_date >" + inclusiveStr +
 					"(select commit_date from commits where commit_id=? and " +
 					"(branch_id=? or branch_id is NULL)) ORDER BY commit_date";
 			if (!ascending)
 				sql += " desc";
 			
 			String[] params = {this.branchID, beforeCommitID, this.branchID, afterCommitID, this.branchID};
 			ResultSet rs = execPreparedQuery(sql, params);
 			Commit currentCommit;
 			Map<String, Resources.ChangeType> currentFileset;
 			if (!rs.next())
 				return changes;
 			currentFileset = new HashMap<String, Resources.ChangeType>();
 			currentCommit = new Commit(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getTimestamp(5), rs.getString(6));
 			currentFileset.put(rs.getString("file_id"), Resources.ChangeType.valueOf(rs.getString("change_type")));
 			while(rs.next())
 			{
 				if (rs.getString("commit_id").equals(currentCommit.getCommit_id()))
 				{
 					// append to the current commit
 					currentFileset.put(rs.getString("file_id"), Resources.ChangeType.valueOf(rs.getString("change_type")));
 				}
 				else
 				{
 					// start a new one
 					changes.put(currentCommit, currentFileset);
 					currentFileset = new HashMap<String, Resources.ChangeType>();
 					currentCommit = new Commit(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getTimestamp(5), rs.getString(6));
 					currentFileset.put(rs.getString("file_id"), Resources.ChangeType.valueOf(rs.getString("change_type")));
 				}
 			}
 			changes.put(currentCommit, currentFileset);
 			return changes;
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public String getTimeStamp(String commit_id)
 	{
 		try {
 			String[] params = {commit_id, this.branchID};
 			ResultSet rs = execPreparedQuery("SELECT commit_date from commits where commit_id =? and (branch_id=? or branch_id is NULL);", params);
 			if(rs.next())
 				return rs.getString(1);
 			else
 				return "";
 		}
 		catch(SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public Set<String> getBinaryFiles()
 	{
 		HashSet<String> binaryFiles = new HashSet<String>();
 		try {
 			String[] params = {};
 			ResultSet rs = execPreparedQuery("SELECT file_id from changes EXCEPT (SELECT file_id from files);", params);
 			while(rs.next())
 			{
 				binaryFiles.add(rs.getString(1));
 			}
 			
 			return binaryFiles;
 		}
 		catch(SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public Set<String> getChangedFilesFromCommit(String commitID)
 	{
 		HashSet<String> changed = new HashSet<String>();
 		try {
 			String[] params = {commitID};
 			ResultSet rs = execPreparedQuery("SELECT file_id from changes where commit_id=?", params);
 			while(rs.next())
 			{
 				changed.add(rs.getString(1));
 			}
 			return changed;
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public Set<String> getFileStructureFromCommit(String commitID)
 	{
 		HashSet<String> files = new HashSet<String>();
 		try {
 			String[] params = {commitID};
 			ResultSet rs = execPreparedQuery("SELECT file_id from source_trees where commit_id=?", params);
 			while(rs.next())
 			{
 				files.add(rs.getString(1));
 			}
 			return files;
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public String getRawFile(String fileID, String commitID)
 	{
 		try{
 			String sql = "SELECT raw_file from files where commit_id=? and file_id=?;";
 			String[] params = {commitID, fileID};
 			ResultSet rs = execPreparedQuery(sql, params);
 			if(rs.next())
 				return rs.getString(1);
 			else
 				return "Binary file";
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Checks whether or not a commit is included in the owners table
 	 * @param CommitId
 	 * @return
 	 */
 	public boolean isCommitInOwners(String CommitId) 
 	{
 		try
 		{
 			String sql = "SELECT commit_id from owners where commit_id=?;";
 			String[] params = {CommitId};
 			ResultSet rs = execPreparedQuery(sql, params);
 			if (rs.next())
 				return true;
 			else
 				return false;
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	/**
 	 * Gets the latest commit in the owners table.
 	 * @return CommitID
 	 */
 	public String getLastOwnerCommit() 
 	{
 		try 
 		{
 			String sql = "Select commit_id from owners natural join commits order by commit_date desc;";
 			String[] parms = {};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			if (rs.next())
 				return rs.getString(1);
 			else
 				return null;
 		}
 		catch(SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	/**
 	 * Gets the latest commit in the commits table
 	 * @return CommitID 
 	 */
 	public String getLastCommit() 
 	{
 		try 
 		{
 			String sql = "Select commit_id from commits order by commit_date desc;";
 			String[] parms = {};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			if (rs.next())
 				return rs.getString(1);
 			else
 				return null;
 		}
 		catch(SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Gets the latest revision of a file from a base commitId
 	 * @param CommitId
 	 * @param fileId
 	 * @return
 	 */
 	public String getLatestRevOfFile(String CommitId, String fileId)
 	{
 		try {
 			// get the last commit that changed the file
 			String sql = "SELECT commit_id from changes natural join commits where commit_date < " +
 					"(SELECT commit_date from commits where commit_id=?)" +
 					" and file_id=? order by commit_date desc limit 1;";
 			String[] parms = {CommitId, fileId};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			if (!rs.next())
 				return null;
 			
 			// Get the raw data 
 			String ChangingCommitId = rs.getString("commit_id");
 			sql = "SELECT raw_file from files where commit_id=? and file_id=?;";
 			String[] secondParms = {ChangingCommitId, fileId};
 			rs = execPreparedQuery(sql, secondParms);
 			if(!rs.next())
 				return "Binary file";
 			return rs.getString("raw_file");
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public Commit getCommit(String CommitId)
 	{
 		try
 		{
 			String sql = "SELECT commit_id, author, author_email, comments, commit_date, branch_id FROM Commits where commit_id=? and (branch_id=? or branch_id is NULL);";
			String[] parms = {CommitId};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			if (!rs.next())
 				return null;
 			else
				return new Commit(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getTimestamp(6), rs.getString(7));
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public void insertOwnerRecord(String CommitId, String Author, String FileId, int ChangeStart, int ChangeEnd, ChangeType changeType)
 	{
 		try
 		{
 			PreparedStatement s = conn.prepareStatement(
 					"INSERT INTO owners values (?,?,?,'" + ChangeStart + "','" + ChangeEnd + "', ?)");
 			s.setString(1, CommitId);
 			s.setString(2, Author);
 			s.setString(3, FileId);
 			s.setString(4, changeType.toString());
 			currentBatch.addBatch(s.toString());
 		}
 		catch(SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean execBatch() {
 		try {
 			currentBatch.executeBatch();
 			currentBatch.clearBatch();
 			return true;
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 	}
 }
