 package at.fakeroot.sepm.server;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.TreeSet;
 
 import org.apache.log4j.Logger;
 
 import at.fakeroot.sepm.shared.client.serialize.BoundingBox;
 import at.fakeroot.sepm.shared.client.serialize.ClientGeoObject;
 import at.fakeroot.sepm.shared.client.serialize.SearchResult;
 import at.fakeroot.sepm.shared.server.DBGeoObject;
 import at.fakeroot.sepm.shared.server.Property;
 
 /**
  * Class representing the DAO for the GeoObjects, that reads from and writes to the database
  * Based on the Singleton pattern.
  * @author AC
  */
 public class GeoObjectManager implements IGeoObjectManager
 {
 	private static IGeoObjectManager geoObjManager = null;
 	private static final Logger logger = Logger.getRootLogger();
 	IDBConnection dbRead, dbWrite;
 	
 
 	/**
 	 * Constructor that establishes the DB Connection
 	 * @throws IOException if the database configuration could not be read
 	 * @throws SQLException if the database connection could not be made
 	 */
 	private GeoObjectManager() throws SQLException, IOException
 	{
 		dbRead = new DBConnection();
 		// when we're on the testing database, use only one DBConnection
 		if (dbRead.isTesting()) dbWrite = dbRead;
 		else {
 			dbWrite = new DBConnection();
 			dbWrite.setAutoCommit(false);
 		}
 	}
 
 	protected void finalize() throws SQLException {
 		dbRead.disconnect();
 		dbWrite.setAutoCommit(true);
 		dbWrite.disconnect();
 	}
 
 	/**
 	 * Method used for obtaining an instance of this Singleton class
 	 * @throws IOException if the database configuration could not be read
 	 * @throws SQLException if the database connection could not be made
 	 */	
 	public static IGeoObjectManager getInstance() throws SQLException, IOException
 	{
 		if(geoObjManager == null)
 			geoObjManager = new GeoObjectManager();
 		return geoObjManager;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see at.fakeroot.sepm.server.IGeoObjectManager#getObjectId(int, java.lang.String)
 	 */	
 	public long getObjectId(int svc_id, String uid) throws NotFoundException, SQLException {
 		long rc=-1;
 		try{
 			PreparedStatement pstmt = dbRead.prepareStatement("SELECT obj_id FROM geoObject WHERE svc_id=? AND uid=?;");
 			pstmt.setInt(1, svc_id);
 			pstmt.setString(2, uid);
 			ResultSet result= pstmt.executeQuery();
 
 			if(result.next()) {	
 				rc=result.getLong("obj_id");
 			}
 			else {
 				throw new NotFoundException("svc_id: "+svc_id+", uid: "+uid);
 			}
 		} catch(SQLException e) {
 			logger.error("SQLException in GeoObjectManager.getObjectId(int svc_id, String uid)", e);
 			throw e;
 		}
 
 		return rc;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see at.fakeroot.sepm.server.IGeoObjectManager#getObjectbyId(long)
 	 */
 	public DBGeoObject getObjectbyId(long id) throws NotFoundException, SQLException {
 		DBGeoObject rc=null;
 
 		try{
 			//long id, String title, double xPos, double yPos, int serviceID, String uniqueID, String link, Timestamp valid_until, Property[] properties, String[] tags){
 			PreparedStatement pstmt = dbRead.prepareStatement("SELECT title, lng, lat, svc_id, uid, link, valid_until "
 					+ "FROM geoObject LEFT JOIN expiringObject e USING (obj_id) WHERE obj_id = ?");
 			pstmt.setLong(1, id);
 			ResultSet res = pstmt.executeQuery();
 
 			if(res.next()){
 				rc = new DBGeoObject(
 						id,
 						res.getString("title"),
 						res.getDouble("lng"), res.getDouble("lat"),
 						res.getInt("svc_id"),
 						res.getString("uid"),
 						res.getString("link"),
 						res.getTimestamp("valid_until"),
 						getProperties(id),
 						queryTags(id));
 			}
 			else {
 				throw new NotFoundException("obj_id: "+id);
 			}
 		}catch(SQLException e){
 			logger.error("SQLException in GeoObjectManager.getObjectById(long id)", e);
 			throw e;
 		}
 
 		return rc;
 	}
 	
 	/* (non-Javadoc)
 	 * @see at.fakeroot.sepm.server.IGeoObjectManager#delete(long)
 	 */
 	public void delete(long objId) throws NotFoundException, SQLException {
 		_deleteWhereObjId("expiringObject", objId);
 		_deleteWhereObjId("objectProperty", objId);
 		_deleteWhereObjId("objectTag", objId);
 		if (_deleteWhereObjId("geoObject", objId) == 0) {
 			throw new NotFoundException("GeoObjectManager.delete(): objId="+objId);
 		}
 	}
 	
 	/**
 	 * executes a prepared statement that deletes all entries of table having the given obj_id
 	 * 
 	 * Is used by delete() to delete all references to an object in the different database tables
 	 * @param table table to delete from (not escaped!)
 	 * @param objId object ID to delete
 	 * @return number of affected rows
 	 * @throws SQLException if prepareStatement() or executeUpdate() fail 
 	 */
 	private int _deleteWhereObjId(String table, long objId) throws SQLException {
 		PreparedStatement pstmt = dbWrite.prepareStatement("DELETE FROM " + table + " WHERE obj_id = ?");
 		pstmt.setLong(1, objId);
 		return pstmt.executeUpdate();
 	}
 	
 	/* (non-Javadoc)
 	 * @see at.fakeroot.sepm.server.IGeoObjectManager#delete(int, java.lang.String)
 	 */
 	public void delete(int svcId, String uid) throws NotFoundException, SQLException {
		PreparedStatement pstmt = dbWrite.prepareStatement("DELETE FROM geoObject WHERE svc_id = ? AND uid = ?");
		pstmt.setInt(1, svcId);
		pstmt.setString(2, uid);
		
		if (pstmt.executeUpdate() == 0) {
			throw new NotFoundException("GeoObjectManager.delete: svcId="+svcId+", uid='"+uid+"'");
		}
 	}
 
 	/* (non-Javadoc)
 	 * @see at.fakeroot.sepm.server.IGeoObjectManager#select(java.lang.String[], at.fakeroot.sepm.shared.client.serialize.BoundingBox, int, int)
 	 */
 	public SearchResult select(String[] tags, BoundingBox box, int displayLimit, int countLimit)
 	{
 		SearchResult searchResult = new SearchResult(countLimit);
 		PreparedStatement pstmt;
 		ResultSet res;
 
 		long startTime = Calendar.getInstance().getTimeInMillis();
 
 		try {
 			String joinStmt = "LEFT JOIN expiringObject e USING (obj_id) ";
 			
 			//Get the result count
 			String sql = "SELECT COUNT(subquery.obj_id) FROM (SELECT obj_id FROM geoObject " + joinStmt +
 				getWhereClause(box, tags, false, countLimit) + ") AS subquery";
 			pstmt = dbRead.prepareStatement(sql);
 			for (int i = 0; i < tags.length; i++)
 			{
 				pstmt.setString(i + 1, tags[i]);
 			}
 			res = pstmt.executeQuery();
 			res.next();
 			searchResult.setResultCount(res.getInt(1));
 			res.close();
 			pstmt.close();
 	
 			
 			//Get the result set which contains the selected geoObjects.
 			sql = "SELECT obj_id, svc_id, o.title, lng, lat, t.thumbnail FROM geoObject o " +
 				"INNER JOIN service USING (svc_id) INNER JOIN serviceType t USING (stype_id) " +
 				joinStmt + getWhereClause(box, tags, true, displayLimit);
 			pstmt = dbRead.prepareStatement(sql);
 			for (int i = 0; i < tags.length; i++)
 			{
 				pstmt.setString(i + 1, tags[i]);
 			}
 			res = pstmt.executeQuery();
 			
 			//Get the object tags for all those selected objects. Use a single query for that.
 			while (res.next())
 			{
 				//Create the objects with empty tags here. (We can only loop through the result set
 				//once, therefore we have to set the tags later.
 				searchResult.addResultToList(new ClientGeoObject(
 					res.getLong("obj_id"),
 					res.getString("title"),
 					res.getString("thumbnail"),
 					null,
 					res.getDouble("lng"),
 					res.getDouble("lat")));
 			}
 			res.close();
 			pstmt.close();
 			
 			if (searchResult.getResults().size() > 0)
 			{
 				//Fill in the tags from the database into the ClientGeoObjects.
 				queryTags(searchResult);
 			}
 		}
 		catch (SQLException e) {
 			logger.error("SQL error in GeoObjectManager.select()", e);
 			searchResult.setErrorMessage(e.getMessage());
 		}
 
 		searchResult.setDuration(Calendar.getInstance().getTimeInMillis()-startTime);
 		
 		return searchResult;
 	}
 
 	/**
 	 * Gets the Where-Clause for a query. The objects lie in this BoundingBox and have these tags. Random order is optional. The maximum number is set by limit.
 	 * @param box where the objects lie
 	 * @param tags the object tags
 	 * @param randomOrder set true if a random set of objects should be returned, false for the first limit objects
 	 * @param limit the number of returned objects
 	 * */
 	private String getWhereClause(BoundingBox box, String[] tags, boolean randomOrder, int limit) throws SQLException {
 		String sql = "WHERE (e.valid_until IS null OR e.valid_until >= now()) AND ";
 		
 		//Create the WHERE clause for the geographical location.
 		sql += "(lng BETWEEN " + box.getX1() + " AND " + box.getX2() + ") AND (lat BETWEEN " + box.getY1() + " AND " + box.getY2() + ") ";
 		
 		//Create a where clause for each passed tag.
 		for (int i = 0; i < tags.length; i++)
 		{
 			
 			sql += "AND (obj_id IN (SELECT obj_id FROM objecttag WHERE tag = ?) " +
 				getServiceListByTag(tags[i])+ ") ";
 		}
 
 		if (randomOrder)
 			sql += " ORDER BY rndVal DESC";
 		if (limit > 0)
 			sql += " LIMIT " + limit; 
 				
 		return (sql);
 	}
 	
 	/**
 	 * Method used by getWhereClause to get the svc_id of services having this service tag
 	 * @param tag service tag
 	 * */
 	private String getServiceListByTag(String tag) throws SQLException {
 		PreparedStatement stmt = dbRead.prepareStatement("SELECT svc_id from servicetag where tag = ?");
 		String rc = "";
 		
 		stmt.setString(1, tag);
 		
 		ResultSet res = stmt.executeQuery();
 		res.last();
 		int numRows = res.getRow();
 		res.beforeFirst();
 		if (numRows > 0) {
 			rc = "OR svc_id IN (";
 			while (res.next()) {
 				rc += res.getInt(1)+",";
 				
 			}
 			rc = rc.substring(0, rc.length()-1);
 			rc+= ")";
 		}
 		return rc;
 	}
 
 	/**
 	 * Fills the geoObjects with tags.
 	 * @param result The search result, containing the objects to fill.
 	 * @throws SQLException
 	 */
 	private void queryTags(SearchResult result) throws SQLException
 	{
 		ArrayList<ClientGeoObject> geoObjects = result.getResults();
 		ArrayList<String> curObjTags = new ArrayList<String>();
 		Iterator<ClientGeoObject> iter;
 		
 		//Set up the SQL query string.
 		String sql = "SELECT obj_id, tag FROM objectTag WHERE obj_id IN (";
 		iter = geoObjects.iterator(); 
 		while (iter.hasNext())
 			sql += iter.next().getId() + ", ";
 		sql = sql.substring(0, sql.length() - 2) + ") ORDER BY obj_id";
 		
 		//Execute the query.
 		PreparedStatement tagStmt = dbRead.prepareStatement(sql);
 		ResultSet tagRes = tagStmt.executeQuery();
 		long lastID = -1, curID;
 		while (tagRes.next())
 		{
 			curID = tagRes.getLong("obj_id");
 			if (curID != lastID)
 			{
 				if (lastID != -1)
 				{
 					//Get the index of the object with the lastID within the array.
 					iter = geoObjects.iterator();
 					while (iter.hasNext())
 					{
 						ClientGeoObject loopObj = iter.next();
 						if (loopObj.getId() == lastID)
 						{
 							loopObj.setTags(curObjTags.toArray(new String[curObjTags.size()]));
 							break;
 						}
 					}
 				}
 				curObjTags.clear();
 				lastID = curID;
 			}
 			curObjTags.add(tagRes.getString("tag"));
 		}
 		
 		//Set the tags of the last object too.
 		//Get the index of the object with the lastID within the array.
 		iter = geoObjects.iterator();
 		while (iter.hasNext())
 		{
 			ClientGeoObject loopObj = iter.next();
 			if (loopObj.getId() == lastID)
 			{
 				loopObj.setTags(curObjTags.toArray(new String[curObjTags.size()]));
 				break;
 			}
 		}
 		
 		tagRes.close();
 		tagStmt.close();
 		
 		//Check if there are objects where we didn't find any tags.
 		iter = geoObjects.iterator();
 		while (iter.hasNext())
 		{
 			ClientGeoObject loopObj = iter.next();
 			if (loopObj.getTags() == null)
 				loopObj.setTags(new String[0]);
 		}
 	}
 	
 	/**
 	 * Get the tags for a object
 	 * @param objID the object's id
 	 * */
 	private String[] queryTags(long objId) throws SQLException {
 		PreparedStatement tagStmt = dbRead.prepareStatement("SELECT tag FROM objectTag WHERE obj_id = ?");
 		String[] rc;
 
 		tagStmt.setLong(1, objId);
 		ResultSet tagRes = tagStmt.executeQuery();
 		tagRes.last();
 		rc = new String[tagRes.getRow()];
 		tagRes.beforeFirst();
 
 		while (tagRes.next()) {
 			rc[tagRes.getRow()-1] = tagRes.getString(1);
 		}
 		tagStmt.close();
 
 		return rc;
 	}	
 
 	/**
 	 * Get the properties for a GeoObject
 	 * @param objId Object ID
 	 * @return Property Array
 	 * @throws SQLException
 	 */
 	private Property[] getProperties(long obj_id) throws SQLException {
 		Property[] rc;
 		PreparedStatement pstmt = dbRead.prepareStatement("SELECT name, value FROM objectProperty WHERE obj_id = ?"); 
 		pstmt.setLong(1, obj_id);
 		ResultSet rs= pstmt.executeQuery();
 		rs.last();
 		rc=new Property[rs.getRow()];
 		rs.beforeFirst();
 		while(rs.next()) {
 			rc[rs.getRow()-1] = new Property(rs.getString("name"),rs.getString("value"));
 		}
 		pstmt.close();
 
 		return rc;	
 	} 
 
 
 	/* (non-Javadoc)
 	 * @see at.fakeroot.sepm.server.IGeoObjectManager#insert(at.fakeroot.sepm.shared.server.DBGeoObject)
 	 */
 	public void insert (DBGeoObject obj) throws RemoteException
 	{
 		try {
 			PreparedStatement pstmt = dbWrite.prepareStatement("INSERT INTO geoObject(svc_id, uid, title, link, lng, lat) VALUES (?, ?, ?, ?, ?, ?)");
 			pstmt.setInt(1, obj.getSvcId());
 			pstmt.setString(2, obj.getUid());
 			pstmt.setString(3, obj.getTitle());
 			pstmt.setString(4, obj.getLink());
 			pstmt.setDouble(5, obj.getXPos());
 			pstmt.setDouble(6, obj.getYPos());
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 			// filter double tags
 			String tags[] = obj.getTags();
 			TreeSet<String> tagSet = new TreeSet<String>();
 			for (int i = 0; i < tags.length; i++) {
 				tagSet.add(tags[i].toLowerCase());
 			}
 
 			//insert new tags
 			pstmt = dbWrite.prepareStatement("INSERT INTO objectTag(obj_id, tag) VALUES (currval('geoobject_obj_id_seq'), ?)");
 			Iterator<String> it = tagSet.iterator();
 			while(it.hasNext()) {
 				pstmt.setString(1, it.next());
 				pstmt.executeUpdate();
 			}
 			pstmt.close();
 
 			//save the properties
 			pstmt=dbWrite.prepareStatement("INSERT INTO objectProperty (obj_id, name, value) VALUES (currval('geoobject_obj_id_seq'), ?, ?)");
 			Property[] prop = obj.getProperties();
 			for(int i=0; i<prop.length; i++) {
 				pstmt.setString(1, prop[i].getName());
 				pstmt.setString(2, prop[i].getValue());
 				pstmt.executeUpdate();
 			}
 			pstmt.close();
 
 			// expiring objects
 			Timestamp valid_until = obj.getValidUntil();
 			if (valid_until != null) {
 				pstmt = dbWrite.prepareStatement("INSERT INTO expiringObject (obj_id, valid_until) VALUES (currval('geoobject_obj_id_seq'),?)");
 				pstmt.setTimestamp(1, valid_until);
 				pstmt.close();
 			}
 			dbWrite.commit();
 		}
 		catch (SQLException e) {
 			try {
 				dbWrite.rollback();
 			}
 			catch (SQLException ex) {}
 			logger.error("SQL error in GeoObjectManager.insert", e);
 			throw new RemoteException("SQL error in geoObjectManager.insert", e);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see at.fakeroot.sepm.server.IGeoObjectManager#update(at.fakeroot.sepm.shared.server.DBGeoObject)
 	 */
 	public void update (DBGeoObject obj) throws RemoteException
 	{
 		try {
 			PreparedStatement pstmt = dbWrite.prepareStatement("UPDATE geoObject SET title = ?, link = ?, lng = ?, lat = ?, last_updated = now() WHERE obj_id = ?");
 			pstmt.setString(1, obj.getTitle());
 			pstmt.setString(2, obj.getLink());
 			pstmt.setDouble(3, obj.getXPos());
 			pstmt.setDouble(4, obj.getYPos());
 			pstmt.setLong(5, obj.getId());
 			pstmt.executeUpdate();
 
 
 			
 			// filter double tags
 			String tags[] = obj.getTags();
 			TreeSet<String> tagSet = new TreeSet<String>();
 			for (int i = 0; i < tags.length; i++) {
 				tagSet.add(tags[i].toLowerCase());
 			}
 
 			// overwrite tags
 			deleteTags(obj.getId());
 			pstmt = dbWrite.prepareStatement("INSERT INTO objectTag(obj_id, tag) VALUES (?, ?)");
 			Iterator<String> it = tagSet.iterator();
 			while(it.hasNext()){
 				String tag = it.next();
 				pstmt.setLong(1, obj.getId());
 				pstmt.setString(2, tag);
 				pstmt.executeUpdate();
 			}
 			pstmt.close();
 			
 			// overwrite properties
 			deleteProperties(obj.getId());
 			pstmt=dbWrite.prepareStatement("INSERT INTO objectProperty (obj_id, name, value) VALUES (?, ?, ?)");
 			Property[] prop = obj.getProperties();
 			for(int i=0; i<prop.length; i++){
 				pstmt.setLong(1, obj.getId());
 				pstmt.setString(2, prop[i].getName());
 				pstmt.setString(3, prop[i].getValue());
 				pstmt.executeUpdate();
 			}
 			pstmt.close();
 			
 			// overwrite expiringObject
 			pstmt = dbWrite.prepareStatement("DELETE FROM expiringObject where obj_id = ?");
 			pstmt.setLong(1, obj.getId());
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 			Timestamp valid_until = obj.getValidUntil();
 			if (valid_until != null) {
 				pstmt = dbWrite.prepareStatement("INSERT INTO expiringObject (obj_id, valid_until) VALUES (?,?)");
 				pstmt.setLong(1, obj.getId());
 				pstmt.setTimestamp(2, valid_until);
 				pstmt.close();
 			}
 			dbWrite.commit();
 		}
 		catch (SQLException e) {
 			try {
 				dbWrite.rollback();
 			}
 			catch (SQLException ex) {}
 			logger.error("Error in GeoObjectManager.update()", e);
 			throw new RemoteException("Error in GeoObjectManager.update()", e);
 		}
 	}
 
 	/**
 	 * Deletes the tags when object is updated
 	 * @param objId the id of the object to be updated 
 	 * */
 	private void deleteTags(long objId) throws SQLException{
 		PreparedStatement pstmt=dbWrite.prepareStatement("DELETE FROM objectTag WHERE obj_id = ?");
 		pstmt.setLong(1, objId);
 		pstmt.executeUpdate();
 	}
 
 	
 	/**
 	 * Deletes the properties when object is updated
 	 * @param objId the id of the object to be updated 
 	 * */
 	private void deleteProperties(long objId) throws SQLException{
 		PreparedStatement pstmt=dbWrite.prepareStatement("DELETE FROM objectProperty WHERE obj_id = ?");
 		pstmt.setLong(1, objId);
 		pstmt.executeUpdate();
 	}
 }
