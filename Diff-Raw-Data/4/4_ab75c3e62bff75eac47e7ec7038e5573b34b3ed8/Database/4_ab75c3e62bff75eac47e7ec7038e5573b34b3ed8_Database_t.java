 package edu.ucla.loni.server;
 
 import edu.ucla.loni.shared.*;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 public class Database {
 	////////////////////////////////////////////////////////////
 	// Private Variables
 	////////////////////////////////////////////////////////////
 	private static Connection db_connection;
 	private static String db_name = "jdbc:hsqldb:hsql://localhost:9002/xdb1";
 	private static String db_username = "SA";
 	private static String db_password = "";
 	
 	/**
 	 *  Returns a connection to the database
 	 */
 	private static Connection getDatabaseConnection() throws Exception {
 		if (db_connection == null){
 			Class.forName("org.hsqldb.jdbcDriver");
 			db_connection = DriverManager.getConnection(db_name, db_username, db_password);
 		}
 		
 		return db_connection;
 	}
 	
 	////////////////////////////////////////////////////////////
 	// Directory
 	////////////////////////////////////////////////////////////
 	
 	public static Directory selectDirectory(String absolutePath) throws Exception{
 		Connection con = getDatabaseConnection();
 		
 		PreparedStatement stmt = con.prepareStatement(
 			"SELECT * " +
 			"FROM directories " +		
 			"WHERE absolutePath = ?"		
 		);
 		stmt.setString(1, absolutePath);
 		ResultSet rs = stmt.executeQuery();
 		
 		if (rs.next()){
 			Directory dir = new Directory();
 			
 			dir.dirId = rs.getInt(1);
 			dir.absolutePath = rs.getString(2);
 			dir.monitorModified = rs.getTimestamp(3);
 			dir.accessModified = rs.getTimestamp(4);
 			
 			return dir;
 		} else {
 			return null;
 		}
 	}
 	
 	public static void insertDirectory(String absolutePath, Timestamp monitorModified, Timestamp accessModified) throws Exception{
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"INSERT INTO directories (absolutePath, monitorModified, accessModified) " +
 			"VALUES (?, ?, ?)" 		
 		);
 		stmt.setString(1, absolutePath);
 		stmt.setTimestamp(2, monitorModified);
 		stmt.setTimestamp(3, accessModified);
 		int updated = stmt.executeUpdate();
 		
 		if (updated != 1){
 			throw new Exception("Failed to insert row into 'directory'");
 		}
 	}
 	
 	public static void updateDirectory(Directory dir) throws Exception{
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"UPDATE directories " +
 			"SET monitorModified = ?, accessModified = ? " +
 			"WHERE directoryId = ?" 		
 		);
 		stmt.setTimestamp(1, dir.monitorModified);
 		stmt.setTimestamp(2, dir.accessModified);
 		stmt.setInt(3, dir.dirId);
 		int updated = stmt.executeUpdate();
 		
 		if (updated != 1){
 			throw new Exception("Failed to update row in 'directory'");
 		}
 	}
 	
 	////////////////////////////////////////////////////////////
 	// Pipefile
 	////////////////////////////////////////////////////////////
 	
 	/**
 	 * ResultSet is from a query with the following form 
 	 *   SELECT * 
 	 *   FROM pipefile ...
 	 */
 	private static Pipefile[] resultSetToPipefileArray(ResultSet rs) throws Exception{
 		ArrayList<Pipefile> list = new ArrayList<Pipefile>();
 		
 		while (rs.next()) {
 			Pipefile p = new Pipefile();
 			
 			p.fileId = rs.getInt(1);
 			p.directoryId = rs.getInt(2);
 			p.absolutePath = rs.getString(3);
 			p.lastModified = rs.getTimestamp(4);
 			
 			p.name = rs.getString(5);
 			p.type = rs.getString(6);
 			p.packageName = rs.getString(7);
 			p.description = rs.getString(8);
 			p.tags = rs.getString(9);
 			p.access = Database.selectAgents(true,  p.fileId);
 			
 			p.values = rs.getString(10);
 			p.formatType = rs.getString(11);
 			p.location = rs.getString(12);
 			p.uri = rs.getString(13);
 			
 			list.add(p);
 		}
 		
 		if (list.size() > 0){
 			Pipefile[] ret = new Pipefile[list.size()];
 			return list.toArray(ret);
 		} else {
 			return null;
 		}
 	}
 	
 	public static Pipefile[] selectPipefiles(int dirId) throws Exception {
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 	    	"SELECT * " +
 			"FROM pipefiles " +
 			"WHERE directoryID = ? " +
 			"ORDER BY absolutePath"
 		);
 	    stmt.setInt(1, dirId);
 		ResultSet rs = stmt.executeQuery();
 		
 		return resultSetToPipefileArray(rs);
 	}
 	
 	public static Pipefile[] selectPipefilesSearch(int dirId, String query) throws Exception {
 		query = "%" + query.toLowerCase() + "%";
 		
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"SELECT * " +
 			"FROM pipefiles " +
 			"WHERE directoryID = ? " +
 				"AND (LCASE(name) LIKE ? " +
 				     "OR LCASE(packageName) LIKE ? " +
 				     "OR LCASE(description) LIKE ? " +
 				     "OR LCASE(tags) LIKE ? )" 
 		);
 		stmt.setInt(1, dirId);
 		stmt.setString(2, query);
 		stmt.setString(3, query);
 		stmt.setString(4, query);
 		stmt.setString(5, query);
 		ResultSet rs = stmt.executeQuery();
 		
 		return resultSetToPipefileArray(rs);
 	}
 	
 	public static Pipefile selectPipefileByHierarchy(int dirId, String packageName, String type, String name) throws Exception {
 		
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"SELECT * " +
 			"FROM pipefiles " +
 			"WHERE directoryID = ? " +
 				"AND packageName =  ? " +
 				"AND type = ? " +
 				"AND name = ? " 
 		);
 		stmt.setInt(1, dirId);
 		stmt.setString(2, packageName);
 		stmt.setString(3, type);
 		stmt.setString(4, name);
 		ResultSet rs = stmt.executeQuery();
 		
 		Pipefile [] rsPipes = resultSetToPipefileArray(rs);
 		if (rsPipes.length == 1) //should always be 1
 			return rsPipes[0];
 		else
 			return null;
 	}
 	
 	public static int selectPipefileId(String absolutePath) throws Exception {
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 	    	"SELECT fileId " +
 			"FROM pipefiles " +
 			"WHERE absolutePath = ? "
 		);
 	    stmt.setString(1, absolutePath);
 		ResultSet rs = stmt.executeQuery();
 		
 		if (rs.next()){
 			return rs.getInt(1);
 		} else {
 			return -1;
 		}
 	}
 	
 	public static Timestamp selectPipefileLastModified(String absolutePath) throws Exception {
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 	    	"SELECT lastModified " +
 			"FROM pipefiles " +
 			"WHERE absolutePath = ? "
 		);
 	    stmt.setString(1, absolutePath);
 		ResultSet rs = stmt.executeQuery();
 		
 		if (rs.next()){
 			return rs.getTimestamp(1);
 		} else {
 			return null;
 		}
 	}
 	
 	public static void insertPipefile(int dirId, Pipefile pipe) throws Exception{
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"INSERT INTO pipefiles (" +
 				"directoryID, absolutePath, lastModified, " +
 				"name, type, packageName, description, tags, " +
 				"dataValues, formatType, location, uri) " +
 			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
 		);
 		stmt.setInt(1, dirId);
 		stmt.setString(2, pipe.absolutePath);
 		stmt.setTimestamp(3, pipe.lastModified);
 		
 		stmt.setString(4, pipe.name);
 		stmt.setString(5, pipe.type);
 		stmt.setString(6, pipe.packageName);
 		stmt.setString(7, pipe.description);
 		stmt.setString(8, pipe.tags);
 		
 		stmt.setString(9, pipe.values);
 		stmt.setString(10, pipe.formatType);
 		stmt.setString(11, pipe.location);
 		stmt.setString(12, pipe.uri);
 		
 		int inserted = stmt.executeUpdate();
 		
 		if (inserted != 1){
 			throw new Exception("Failed to insert row into database");
 		}
 	}
 	
 	public static void updatePipefile(Pipefile pipe) throws Exception{
 		Database.updateAgentConnections(pipe.directoryId, true, pipe.fileId, pipe.access);
 		
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"UPDATE pipefiles " +
 		    "SET name = ?, type = ?, packageName = ?, description = ?, tags = ?, " +
 		    "dataValues = ?, formatType = ?, location = ?, uri = ?, " +
 		    "absolutePath = ?, lastModified = ? " +
 			"WHERE fileId = ?"
 		);
 		
 		stmt.setString(1, pipe.name);
 		stmt.setString(2, pipe.type);
 		stmt.setString(3, pipe.packageName);
 		stmt.setString(4, pipe.description);
 		stmt.setString(5, pipe.tags);
 		
 		stmt.setString(6, pipe.values);
 		stmt.setString(7, pipe.formatType);
 		stmt.setString(8, pipe.location);
 		stmt.setString(9, pipe.uri);
 		
 		stmt.setString(10, pipe.absolutePath);
 		stmt.setTimestamp(11, pipe.lastModified);
 		
 		stmt.setInt(12, pipe.fileId);
 		
 		int updated = stmt.executeUpdate();
 		
 		if (updated != 1){
 			throw new Exception("Failed to insert row into database");
 		}
 	}
 	
 	public static void deletePipefile(Pipefile pipe) throws Exception{
 		Database.deleteAgentConnections(true, pipe.fileId);
 		
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"DELETE FROM pipefiles " +
 			"WHERE fileId = ?" 		
 		);
 		stmt.setInt(1, pipe.fileId);
 		
 		int deleted = stmt.executeUpdate();
 		
 		if (deleted != 1){
 			throw new Exception("Failed to delete row from 'pipefile' table");
 		}
 	}
 	
 	////////////////////////////////////////////////////////////
 	// Groups
 	////////////////////////////////////////////////////////////
 	
 	/**
 	 * ResultSet is from a query with the following form <br>
 	 *   SELECT agentId, dirId, name <br>
 	 *   FROM agents ... <br>
 	 */
 	private static Group[] resultSetToGroupArray(ResultSet rs) throws Exception{
 		ArrayList<Group> list = new ArrayList<Group>();
 		
 		while (rs.next()) {
 			Group g = new Group();
 			
 			g.groupId = rs.getInt(1);
 			g.directoryId = rs.getInt(2);
 			g.name = rs.getString(3);
 			
 			g.users = Database.selectAgents(false, g.groupId);
 			
 			list.add(g);
 		}
 		
 		if (list.size() > 0){
 			Group[] ret = new Group[list.size()];
 			return list.toArray(ret);
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 *  Select all groups
 	 */
 	public static Group[] selectGroups(int dirId) throws Exception{
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"SELECT agentId, directoryId, name " +
 			"FROM agents " +
 			"WHERE directoryId = ? AND isGroup = 1"
 		);
 		stmt.setInt(1, dirId);
 		ResultSet rs = stmt.executeQuery();
 		
 		return resultSetToGroupArray(rs);
 	}
 	
 	public static Group selectGroupByName(int dirId, String name) throws Exception {
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"SELECT agentId, directoryId, name " +
 			"FROM agents " +
 			"WHERE directoryId = ? AND name = ? AND isGroup = 1"
 		);
 		stmt.setInt(1, dirId);
 		stmt.setString(2, name);
 		ResultSet rs = stmt.executeQuery();
 		Group[] groups = resultSetToGroupArray(rs);
 		
 		if (groups != null && groups.length == 1){
 			return groups[0];
 		}
 		else {
 			return null;
 		}
 	}
 	
 	/**
 	 *  Insert a group
 	 */
 	public static void insertGroup(int dirId, Group group) throws Exception{
 		Database.insertAgent(dirId, group.name, true);
 		int groupId = Database.selectAgentId(dirId, group.name, true);
 		
 		Database.insertAgentConnections(dirId, false, groupId, group.users);
 	}
 	
 	/**
 	 *  Update a group
 	 */
 	public static void updateGroup(Group group) throws Exception{
 		Database.updateAgent(group.groupId, group.name);
 		Database.updateAgentConnections(group.directoryId, false, group.groupId, group.users);
 	}
 	
 	/**
 	 *  Delete a group
 	 */
 	public static void deleteGroup(Group group) throws Exception{
 		// Delete group
 		Database.deleteAgent(group.groupId);
 		
 		// Delete connections used to define the group
 		Database.deleteAgentConnections(false, group.groupId);
 		
 		// Delete connections where this group defined something else
 		Database.deleteGroupConnections(group.groupId);
 	}
 	
 	////////////////////////////////////////////////////////////
 	// FileAgents, GroupAgents
 	///////////////////////////////////////////////////////////
 	
 	/**
 	 * ResultSet is from a query with the following form <br>
 	 *   SELECT agents.name, agents.isGroup <br>
 	 *   FROM ... agents ... <br>
 	 * <p> 
 	 * Returns the agents as a comma separated String
 	 */
 	private static String resultSetToAgentString(ResultSet rs) throws Exception{
 		String ret = "";
 		
 		while (rs.next()){
 			String name = rs.getString(1);
 			boolean isGroup = rs.getBoolean(2);
 			
 			if (isGroup){
 				ret += GroupSyntax.groupnameToAgent(name);
 			} else {
 				ret += name;
 			}
 			
 			ret += ",";
 		}
 		
 		// Remove last ,
 		if (ret.length() > 0){
 			ret = ret.substring(0, ret.length() - 1); 
 		}
 		
 		return ret;
 	}
 	
 	private static String selectAgents(boolean file, int id) throws Exception{
 		String table, tableId;
 		if (file){
 			table = "fileAgents";
 			tableId = "fileId";
 		} else {
 			table = "groupAgents";
 			tableId = "groupId";
 		}
 		
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"SELECT agents.name, agents.isGroup " +
 			"FROM " + table + " JOIN agents ON "+ table + ".agentId = agents.agentId " +
 			"WHERE "+ tableId + " = ?"
 		);
 		stmt.setInt(1, id);
 		ResultSet rs = stmt.executeQuery();
 		
 		return resultSetToAgentString(rs);
 	}
 	
 	private static void insertAgentConnections(int dirId, boolean file, int id, String agentList) throws Exception {
		if (agentList == null){
			return;
		}
		
 		// Convert agentList into array of agentIds
 		String[] agentNames = agentList.split(",");
 		int[] agentIds = new int[agentNames.length];
 		
 		for (int i = 0; i < agentNames.length; i++){
 			String agent = agentNames[i];
 			
 			// Trim whitespace
 			agent = agent.trim();
 			
 			// Determine if its a group
 			boolean isGroup = GroupSyntax.isGroup(agent);
 			if (isGroup){
 				agent = GroupSyntax.agentToGroupname(agent);
 			}
 			
 			if (!agent.equals("")){
 				agentIds[i] = getAgentId(dirId, agent, isGroup);
 			} else {
 				agentIds[i] = -1; // Invalid
 			}
 		}
 		
 		// Set up query to insert rows in fileAgents or groupAgents
 		String table, tableId;
 		if (file){
 			table = "fileAgents";
 			tableId = "fileId";
 		} else {
 			table = "groupAgents";
 			tableId = "groupId";
 		}
 		
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"INSERT INTO " + table + " (" + tableId + ", agentId) " +
 			"VALUES (?, ?) "
 		);
 		stmt.setInt(1, id);
 		
 		// For each agent add a row
 		for (int agentId : agentIds){
 			if (agentId != -1){
 				stmt.setInt(2, agentId);
 				int inserted = stmt.executeUpdate();
 				
 				if (inserted != 1){
 					throw new Exception("Failed to insert row into '" + table + "' table");
 				}
 			}
 		}
 	}
 	
 	private static void updateAgentConnections(int dirId, boolean file, int id, String agentList) throws Exception{
 		deleteAgentConnections(file, id);
 		insertAgentConnections(dirId, file, id, agentList);
 	}
 	
 	private static void deleteAgentConnections(boolean file, int id) throws Exception{
 		String table, tableId;
 		if (file){
 			table = "fileAgents";
 			tableId = "fileId";
 		} else {
 			table = "groupAgents";
 			tableId = "groupId";
 		}
 		
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"DELETE FROM " + table + " " +
 			"WHERE " + tableId + " = ?"	
 		);
 		stmt.setInt(1, id);
 		stmt.executeUpdate();
 		
 		// In case we created some agents that are no longer used, go delete them
 		Database.deleteUnusedAgents();
 	}
 	
 	private static void deleteGroupConnections(int groupId) throws Exception {
 		Connection con = getDatabaseConnection();
 		
 		// Delete connections to files
 		PreparedStatement stmt = con.prepareStatement(
 			"DELETE FROM fileAgents "+
 			"WHERE agentId = ?"	
 		);
 		stmt.setInt(1, groupId);
 		stmt.executeUpdate();
 		
 		// Delete connections to groups
 		stmt = con.prepareStatement(
 			"DELETE FROM groupAgents "+
 			"WHERE agentId = ?"	
 		);
 		stmt.setInt(1, groupId);
 		stmt.executeUpdate();
 	}
 	
 	////////////////////////////////////////////////////////////
 	// Agents
 	///////////////////////////////////////////////////////////
 	
 	private static int getAgentId(int dirId, String name, boolean isGroup)throws Exception{
 		int id = selectAgentId(dirId, name, isGroup);
 		
 		if (id == -1){
 			insertAgent(dirId, name, isGroup);
 			id = selectAgentId(dirId, name, isGroup);
 		}
 		
 		return id;
 	}
 	
 	private static int selectAgentId(int dirId, String name, boolean isGroup) throws Exception{
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"SELECT agentId " +
 			"FROM agents " +
 			"WHERE directoryId = ? AND name = ? AND isGroup = ?"
 		);
 		stmt.setInt(1, dirId);
 		stmt.setString(2, name);
 		stmt.setBoolean(3, isGroup);
 		ResultSet rs = stmt.executeQuery();
 		
 		if (rs.next()){
 			return rs.getInt(1);
 		} else {
 			return -1;
 		}
 	}
 	
 	private static void insertAgent(int dirId, String name, boolean isGroup) throws Exception{
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"INSERT INTO agents (directoryId, name, isGroup) " +
 			"VALUES (?, ?, ?)"
 		);
 		stmt.setInt(1, dirId);
 		stmt.setString(2, name);
 		stmt.setBoolean(3, isGroup);
 		int inserted = stmt.executeUpdate();
 		
 		if (inserted != 1){
 			throw new Exception("Failed to insert row into 'agents' table");
 		}
 	}
 	
 	private static void updateAgent(int agentId, String name) throws Exception {
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"UPDATE agents " +
 			"SET name = ? " +
 			"WHERE agentId = ?"
 		);
 		stmt.setString(1, name);
 		stmt.setInt(2, agentId);
 		int updated = stmt.executeUpdate();
 		
 		if (updated != 1){
 			throw new Exception("Failed to update row in 'agents' table");
 		}
 	}
 	
 	private static void deleteAgent(int agentId) throws Exception {
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"DELETE FROM agents " +
 			"WHERE agentId = ?"
 		);
 		stmt.setInt(1, agentId);
 		stmt.executeUpdate();
 	}
 	
 	private static void deleteUnusedAgents() throws Exception {
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"SELECT agents.agentId " +
 			"FROM agents " +
 				"LEFT JOIN fileAgents ON agents.agentId = fileAgents.agentId " +
 				"LEFT JOIN groupAgents ON agents.agentId = groupAgents.agentId " +
 			"WHERE agents.isGroup = 0 " +
 				"AND fileAgents.fileId IS NULL " +
 				"AND groupAgents.groupId IS NULL " +
 			"GROUP BY agents.agentId "
 		);
 		ResultSet rs = stmt.executeQuery();
 		
 		while (rs.next()){
 			int agentId = rs.getInt(1);
 			Database.deleteAgent(agentId);
 		}
 	}
 }
