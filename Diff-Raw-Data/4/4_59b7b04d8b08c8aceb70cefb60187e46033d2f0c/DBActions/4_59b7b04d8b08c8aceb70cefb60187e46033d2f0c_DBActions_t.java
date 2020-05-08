 /**
  * 
  */
 package net.skyebook;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import edu.poly.bxmc.betaville.osm.Node;
 import edu.poly.bxmc.betaville.osm.Relation;
 import edu.poly.bxmc.betaville.osm.RelationMemeber;
 import edu.poly.bxmc.betaville.osm.tag.AbstractTag;
 
 /**
  * @author Skye Book
  *
  */
 public class DBActions {
 
 	private AtomicBoolean busy = new AtomicBoolean(false);
 
 	private Connection con = null;
 	private Statement statement = null;
 
 	private PreparedStatement insertNode;
 	private PreparedStatement insertNodeNullTags;
 	private PreparedStatement insertWay;
 	private PreparedStatement insertWayNullTags;
 	private PreparedStatement insertWayMember;
 	private PreparedStatement insertRelation;
 	private PreparedStatement insertRelationNullTags;
 	private PreparedStatement insertRelationMember;
 
 
 
 	/**
 	 * Constructor - Creates (opens) the SQL connection
 	 */
 	public DBActions(String user, String pass, String dbName) {
 		try {
 			try {
 				Class.forName("com.mysql.jdbc.Driver").newInstance();
 				con = DriverManager.getConnection(
 						"jdbc:mysql://localhost:3306/"+dbName,
 						pass,
 						pass);
 
 				statement = con.createStatement();
 
 				System.out.println("Preparing statements");
 
 				insertNode = con.prepareStatement("INSERT INTO nodes (id, latitude, longitude, tags) VALUES (?, ?, ?, ?)");
 				insertNodeNullTags = con.prepareStatement("INSERT INTO nodes (id, latitude, longitude) VALUES (?, ?, ?)");
 
 				insertWay = con.prepareStatement("INSERT INTO ways (id, tags) VALUES (?, ?)");
 				insertWayNullTags = con.prepareStatement("INSERT INTO ways (id) VALUES (?)");
 				insertWayMember = con.prepareStatement("INSERT INTO way_members (way, node) VALUES (?, ?)");
 
				insertRelation = con.prepareStatement("INSERT INTO relations (id, tags) VALUES (?, ?)");
				insertRelationNullTags = con.prepareStatement("INSERT INTO relations (id) VALUES (?)");
 				insertRelationMember = con.prepareStatement("INSERT INTO relation_members (relation, way, type) VALUES (?, ?, ?)");
 
 				System.out.println("Statements Prepared");
 
 			} catch (ClassNotFoundException e) {
 				System.err.println("ClassNotFoundException: " + e.getMessage());
 			} catch (InstantiationException e) {
 				System.err.println("InstantiationException: " + e.getMessage());
 			} catch (IllegalAccessException e) {
 				System.err.println("IllegalAccessException: " + e.getMessage());
 			}
 		} catch (SQLException e) {
 			System.err.println("SQLException: " + e.getMessage());
 			System.err.println("SQLState: " + e.getSQLState());
 			System.err.println("VendorError: " + e.getErrorCode());
 		}
 	}
 
 	public void addNode(Node node) throws SQLException{
 		busy.set(true);
 		if(node.getTags().size()==0){
 			insertNodeNullTags.setLong(1, node.getId());
 			insertNodeNullTags.setDouble(2, node.getLocation().getLatitude());
 			insertNodeNullTags.setDouble(3, node.getLocation().getLongitude());
 			insertNodeNullTags.execute();
 		}
 		else{
 			insertNode.setLong(1, node.getId());
 			insertNode.setDouble(2, node.getLocation().getLatitude());
 			insertNode.setDouble(3, node.getLocation().getLongitude());
 			insertNode.setString(4, createTagString(node.getTags()));
 			insertNode.execute();
 		}
 
 		busy.set(false);
 	}
 
 	public void addWay(ShallowWay way) throws SQLException{
 		busy.set(true);
 		if(way.getTags().size()==0){
 			insertWayNullTags.setLong(1, way.getId());
 			insertWayNullTags.execute();
 		}
 		else{
 			insertWay.setLong(1, way.getId());
 			insertWay.setString(2, createTagString(way.getTags()));
 			insertWay.execute();
 		}
 
 		for(long reference : way.getNodeReferences()){
 			addWayMember(way.getId(), reference);
 		}
 		busy.set(false);
 	}
 
 	public void addRelation(Relation relation) throws SQLException{
 		busy.set(true);
 		if(relation.getTags().size()==0){
 			insertRelationNullTags.setLong(1, relation.getId());
 			insertRelationNullTags.execute();
 		}
 		else{
 			insertRelation.setLong(1, relation.getId());
 			insertRelation.setString(2, createTagString(relation.getTags()));
 			insertRelation.execute();
 		}
 
 		for(RelationMemeber rm : relation.getMemebers()){
 			addRelationMember(relation.getId(), rm);
 		}
 		busy.set(false);
 	}
 
 	private void addRelationMember(long relationID, RelationMemeber rm) throws SQLException{
 		insertRelationMember.setLong(1, relationID);
 		insertRelationMember.setLong(2, rm.getObjectReference().getId());
 		insertRelationMember.setString(3, rm.getRole());
 		insertRelationMember.execute();
 	}
 
 	private void addWayMember(long wayID, long nodeID) throws SQLException{
 		insertWayMember.setLong(1, wayID);
 		insertWayMember.setLong(2, nodeID);
 		insertWayMember.execute();
 	}
 
 	private String createTagString(List<AbstractTag> tags){
 		// build the tag string
 		StringBuilder tagString = new StringBuilder();
 		//System.out.println("there are "+tags.size()+" tags");
 		for(AbstractTag tag : tags){
 			// if this is not the first tag, add a comma to separate it from the last pair
 			if(tagString.length()>0) tagString.append(",");
 
 			tagString.append(tag.getKey()+","+tag.getValue());
 		}
 
 		return tagString.toString();
 	}
 
 	public synchronized boolean isBusy(){
 		return busy.get();
 	}
 
 	/**
 	 * Sends the SQL query to the database
 	 * 
 	 * @param query
 	 *            Query to send to the database
 	 * @return The set of results obtained after the execution of the query
 	 * @throws SQLException
 	 */
 	public ResultSet sendQuery(String query) throws SQLException {
 		return con.createStatement().executeQuery(query);
 	}
 
 	public int sendUpdate(String update) throws SQLException{
 		return statement.executeUpdate(update, Statement.RETURN_GENERATED_KEYS);
 	}
 
 	public int getLastKey(){
 		try {
 			ResultSet rs = statement.getGeneratedKeys();
 			if(rs.next()){
 				int last = rs.getInt(1);
 				return last;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	/**
 	 * Closes the connection with the database
 	 */
 	public void closeConnection() {
 		try {
 			if (con != null) {
 				con.close();
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public Connection getConnection(){
 		return con;
 	}
 
 }
