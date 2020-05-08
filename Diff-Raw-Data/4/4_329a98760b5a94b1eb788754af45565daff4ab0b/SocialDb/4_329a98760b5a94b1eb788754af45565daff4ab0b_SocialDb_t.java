 package db;
 
 import java.io.InputStreamReader;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import models.Attachment;
 import models.Dependency;
 import models.Issue;
 import models.Item;
 import models.Link;
 import models.Person;
 import models.Silent;
 import db.Resources.CommType;
 import db.util.ISetter;
 import db.util.ISetter.FloatSetter;
 import db.util.ISetter.IntSetter;
 import db.util.ISetter.StringSetter;
 import db.util.PreparedStatementExecutionItem;
 
 public class SocialDb extends DbConnection
 {
 	public boolean connect(String dbName)
 	{
 		return super.connect(dbName);
 	}
 	/**
 	 * Creates a db on the current connection.
 	 * @param dbName
 	 * @return true for success
 	 */
 	public boolean createDb(String dbName) {
 		try {
 			// Drop the DB if it already exists
 			String query = "DROP DATABASE IF EXISTS " + dbName;
 			ISetter[] parms = {};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, parms);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			
 			// First create the DB.
 			query = "CREATE DATABASE " + dbName;
 			ei = new PreparedStatementExecutionItem(query, parms);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			
 			// Reconnect to our new database.
 			connect(dbName.toLowerCase());
 			
 			// Now load our default schema in.
 			runScript(new InputStreamReader(this.getClass().getResourceAsStream("createSocialDb.sql")));
 			return true;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	/**
 	 * Insert a Social Item 
 	 * @param item
 	 * @return the sequence id of the inserted item
 	 */
 	public int insertItem(Item item) {
 		String query = "INSERT INTO items (p_id, item_date, item_id, body, title, type) VALUES " +
 				"(" + item.getPId() + ", ?::timestamp, default, ?, ?, ?)";
 		ISetter[] params = {
 				new StringSetter(1,item.getItemDate().toString()),
 				new StringSetter(2,item.getBody()),
 				new StringSetter(3,item.getTitle()),
 				new StringSetter(4,item.getCommunicationType().toString())
 		};
 		PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, params);
 		addExecutionItem(ei);
 		ei.waitUntilExecuted();
 		return getSequenceValue("items_id_seq"); 
 	}
 	
 	/**
 	 * Insert a person to people table and return the newly created sequence id
 	 * If the person already exists, return the sequence id
 	 * @param person
 	 * @return sequence id as p_id. -1 if there is exception
 	 */
 	public int insertPerson(Person person) {
 		try 
 		{
 			String sql = "SELECT * FROM people WHERE " +
 					"name=? AND email=?"; 
 			
 			ISetter[] prms = {new StringSetter(1,person.getName()),
 							  new StringSetter(2,person.getEmail())};
 			PreparedStatementExecutionItem eifirst = new PreparedStatementExecutionItem(sql, prms);
 			addExecutionItem(eifirst);
 			eifirst.waitUntilExecuted();
 			ResultSet rs = eifirst.getResult();
 
 			if(!rs.next()) {
 				// Insert
 				String query = "INSERT INTO people (p_id, name, email) VALUES " +
 						"(default, ?, ?)";
 				ISetter[] params = {
 						new StringSetter(1, person.getName()),
 						new StringSetter(2, person.getEmail())
 				};
 				PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, params);
 				addExecutionItem(ei);
 				ei.waitUntilExecuted();
 
 				return getSequenceValue("people_id_seq");
 			}
 			else {
 				return rs.getInt("p_id");
 			}
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return -1;
 		}
 	}
 	
 	/**
 	 * Insert a thread into Threads table
 	 * @param thread
 	 * @return true if succeed. Exception throw if not
 	 */
 	public boolean insertThread(models.Thread thread) {
 		String query = "INSERT INTO threads (item_id, thread_id) VALUES " +
 				"(" + thread.getItemID() + ", " + thread.getThreadID() + ")";
 		PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, null);
 		addExecutionItem(ei);
 		ei.waitUntilExecuted();
 		return true;
 	}
 	
 	/**
 	 * Insert Link into Links table
 	 * If Link already exists, update its confidence
 	 * @param link
 	 * @return true if succeed, false ow
 	 */
 	public synchronized boolean insertLink(Link link) {
 		try {
 			// First check if a link exists. 
 			String query = "SELECT * from links where item_id=? and commit_id=?";
 			ISetter[] parms = {new IntSetter(1, link.getItemID()), new StringSetter(2, link.getCommitID())};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, parms);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			if (ei.getResult().next())
 			{
 				// If we have a existing record that has less confidence, then take the higher one.
 				// Otherwise ignore this link and keep the higher confidence one.
 				if (ei.getResult().getFloat("confidence") < link.getConfidence())
 				{
 					query = "UPDATE links SET confidence=? where item_id=? and commit_id=?";
 					ISetter[] parms2 = {new FloatSetter(1, link.getConfidence()), new IntSetter(2, link.getItemID()), new StringSetter(3, link.getCommitID())};
 					ei = new PreparedStatementExecutionItem(query, parms2);
 					addExecutionItem(ei);
 					ei.waitUntilExecuted();
 				}
 			}
 			else 
 			{
 				query = "INSERT INTO links (item_id, commit_id, confidence) VALUES " +
 				"(?, ?, ?)";
 				ISetter[] parms3 = {new IntSetter(1, link.getItemID()), new StringSetter(2,link.getCommitID()), new FloatSetter(3, link.getConfidence())};
 				ei = new PreparedStatementExecutionItem(query, parms3);
 				addExecutionItem(ei);
 				ei.waitUntilExecuted();
 			}
 			return true;
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	/**
 	 * Insert Issue into issues table
 	 * @param issue
 	 * @return true if succeeds, throw exception ow
 	 */
 	public boolean insertIssue(Issue issue) {
 		String query = "INSERT INTO issues (item_id, status, assignee_id, creation_ts, last_modified_ts, " +
 				"title, description, creator_id, keywords, issue_num) VALUES " +
 				"(" + issue.getItemID() + ", ?, " + issue.getAssignedID() + ", ?::timestamp, ?::timestamp, ?, ?, " +
 				issue.getCreatorID() + ", ?, ?)";
 		ISetter[] params = {
 				new StringSetter(1, issue.getStatus()),
 				new StringSetter(2, issue.getCreationTS().toString()),
 				new StringSetter(3, issue.getLastModifiedTS().toString()),
 				new StringSetter(4, issue.getTitle()),
 				new StringSetter(5, issue.getDescription()),
 				new StringSetter(6, issue.getKeywords()),
 				new StringSetter(7, issue.getIssueNum())
 		};
 		PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, params);
 		addExecutionItem(ei);
 		ei.waitUntilExecuted();
 		return true;
 	}
 	
 	/**
 	 * Insert Silent to silents table
 	 * @param silent
 	 * @return true if succeed, exception ow
 	 */
 	public boolean insertSilent(Silent silent) {
 		String query = "INSERT INTO silents (p_id, item_id) VALUES " +
 					"(" + silent.getpID() + ", " + silent.getItemID() + ")";
 		PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, null);
 		addExecutionItem(ei);
 		ei.waitUntilExecuted();
 		return true;
 	}
 	
 	/**
 	 * Insert Attachment to attachments table
 	 * @param attachment
 	 * @return true if succeed, exception ow
 	 */
 	public boolean insertAttachment(Attachment attachment) {
 		String query = "INSERT INTO attachments (item_id, title, body) VALUES " +
 					"(" + attachment.getItemID() + ", ?, ?)";
 		ISetter[] params = {
 				new StringSetter(1,attachment.getTitle()),
 				new StringSetter(2,attachment.getBody())
 		};
 		PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, params);
 		addExecutionItem(ei);
 		ei.waitUntilExecuted();
 		return true;
 	}
 	
 	/** 
 	 * Insert dependency into Dependencies table
 	 * @param dependency
 	 * @return true if succeed, exception ow
 	 */
 	public boolean insertDependency(Dependency dependency) {
 		String query = "INSERT INTO dependencies (item_id, depends_on_id) VALUES (?, ?)";
 		ISetter[] params = {
 			new IntSetter(1, dependency.getItemID()),
 			new IntSetter(2, dependency.getDependsOnID())
 		};
 		PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(query, params);
 		addExecutionItem(ei);
 		ei.waitUntilExecuted();
 		return true;
 	}
 	
 	/**
 	 * Get a list of items related to this thread
 	 * @param ThreadID
 	 * @return List of items, empty if none found, null ow
 	 */
 	public List<Item> getItemsInThread(int ThreadID) {
 		try 
 		{
 			LinkedList<Item> items = new LinkedList<Item>();
 			String sql = "SELECT p_id, item_date, items.item_id, body, title, type" +
 					" FROM threads JOIN items ON (threads.item_id = items.item_id)" +
 					" WHERE thread_id=" + ThreadID; 
 			
 			ISetter[] params = {};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			
 			ResultSet rs = ei.getResult();
 			while(rs.next())
 			{
 				items.add(new Item(rs.getInt("p_id"), rs.getTimestamp("item_date"), rs.getInt("item_id"), 
 						rs.getString("body"), rs.getString("title"), CommType.valueOf(rs.getString("type"))));
 			}
 			return items;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Get Sequence Id for a sequence table
 	 * @param sequence
 	 * @return sequence id, -1 of none found or there is exception
 	 */
 	private int getSequenceValue(String sequence) {
 		try 
 		{
 			// Get the ID
 			String sql = "SELECT currval(?)"; 
 			ISetter[] params = {new StringSetter(1, sequence)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			if(rs.next())
 				return rs.getInt("currval");
 			return -1;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return -1;
 		}
 	}
 
 	/**
 	 * Get item id (Sequence id) for an issue Key
 	 * @param dependsKey - issue_num in issues table
 	 * @return item id. -1 if not found or exception
 	 */
 	public int findItemIDFromJiraKey(String dependsKey)
 	{
 		try 
 		{
 			// Get the ID
 			String sql = "SELECT item_id from issues where issue_num = ?";
 			
 			ISetter[] params = {new StringSetter(1, dependsKey)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			if(rs.next())
 				return rs.getInt("item_id");
 			return -1;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return -1;
 		}		
 	}
 
 	/**
 	 * Get a list of item id which has the same issue number (bugNumber)
 	 * @param bugNumber - issue_num
 	 * @return List of item id. Null if there is exception
 	 */
 	public Set<Integer> getIssuesMatchingBugNumber(String bugNumber)
 	{
 		Set<Integer> issues = new HashSet<Integer>();
 		try {
 			String sql = "SELECT item_id FROM issues WHERE issue_num=?";
 			
 			ISetter[] params = {new StringSetter(1, bugNumber)};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			
 			while (rs.next())
 			{
 				issues.add(rs.getInt(1));
 			}
 			return issues;
 		}
 		catch(SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Get the first Item id found that has this bug number
 	 * @param bugNum - issue_num
 	 * @return first item id found, -1 if not found or exception
 	 */
 	public int getItemIDFromBugNumber(int bugNum) {
 		try {
 			String sql = "SELECT item_id FROM issues WHERE issue_num=?";
 			
 			ISetter[] params = {new StringSetter(1, Integer.toString(bugNum))};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			
 			while (rs.next())
 				return rs.getInt("item_id");
 			
 			return -1;
 		}
 		catch(SQLException e)
 		{
 			e.printStackTrace();
 			return -1;
 		}
 	}
 	
 	/**
 	 * Get a list of Issue within a limit number and offset (index to start)
 	 * @param iLIMIT
 	 * @param iOFFSET
 	 * @return List of issues, Null if there is exception
 	 */
 	public List<Issue> getIssues(int iLIMIT, int iOFFSET) {
 		try 
 		{
 			LinkedList<Issue> issues = new LinkedList<Issue>();
 			String sql = "SELECT * FROM issues " +
 					"ORDER BY item_id " +
 					"LIMIT " + iLIMIT + " OFFSET " + iOFFSET; 
 			ISetter[] params = {};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			while(rs.next())
 			{
 				issues.add(new Issue(rs.getInt("item_id"), rs.getString("status"), rs.getInt("assignee_id"), 
 						rs.getTimestamp("creation_ts"), rs.getTimestamp("last_modified_ts"), rs.getString("title"),
 						rs.getString("description"), rs.getInt("creator_id"), rs.getString("keywords"), 
 						rs.getString("issue_num")));
 			}
 			return issues;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Gets a list of all Non-Issue items
 	 * @return List of items, Null if there is exception
	 * TODO @braden, make this paginate if we run into memory issues
 	 */
 	public List<Item> getAllItems()
 	{
 		List<Item> items = new ArrayList<Item>();
 		try
 		{
			String sql = "SELECT p_id, item_date, item_id, body, title, type from items;";
 			ISetter[] params = {};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			while(rs.next())
 			{
 				Item i = new Item(
 						rs.getInt("p_id"),
 						rs.getTimestamp("item_date"),
 						rs.getInt("item_id"),
 						rs.getString("body"),
 						rs.getString("title"),
 						Resources.CommType.valueOf(rs.getString("type"))
 				);
 				items.add(i);
 			}
 			return items; 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Get a list of commit id that are associated with this issue
 	 * @param issue
 	 * @return
 	 */
 	public List<String> getCommitIDForIssue(Issue issue) {
 		try 
 		{
 			LinkedList<String> commitIDs = new LinkedList<String>();
 			String sql = "SELECT * FROM links " +
 					"WHERE item_id=" + issue.getItemID();
 			
 			ISetter[] params = {};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			while(rs.next())
 			{
 				commitIDs.add(rs.getString("commit_id"));
 			}
 			return commitIDs;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Get a list of Link that are associated with this issue
 	 * @param issue
 	 * @return
 	 */
 	public List<Link> getLinksFromIssue(Issue issue) {
 		try {
 			List<Link> links = new ArrayList<Link>();
 			String sql = "SELECT * FROM links WHERE item_id = " + issue.getItemID();
 			
 			ISetter[] params = {};
 			PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, params);
 			addExecutionItem(ei);
 			ei.waitUntilExecuted();
 			ResultSet rs = ei.getResult();
 			
 			while(rs.next())
 			{
 				links.add(new Link(rs.getInt("item_id"),rs.getString("commit_id"),rs.getFloat("confidence")));
 			}
 			
 			return links;
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Delete All Links in links table
 	 */
 	public void deleteLinks() {
 		String sql = "DELETE FROM links;";
 		PreparedStatementExecutionItem ei = new PreparedStatementExecutionItem(sql, null);
 		addExecutionItem(ei);
 		ei.waitUntilExecuted();
 	}
 }
