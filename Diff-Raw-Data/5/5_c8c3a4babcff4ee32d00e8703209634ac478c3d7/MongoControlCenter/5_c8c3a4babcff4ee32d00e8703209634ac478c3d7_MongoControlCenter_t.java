 package helpers;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 import com.mongodb.QueryBuilder;
 import com.mongodb.BasicDBList;
 
 public class MongoControlCenter {
 
 	private MongoClient mongoClient;
 
 	private DB db;
 	private DBCollection initiatives;
 	private DBCollection risks;
 	private DBCollection milestones;
 	private DBCollection events;
 	private DBCollection users;
 
 	private BasicDBList ids;
 
 	public MongoControlCenter(String address, int port)
 			throws UnknownHostException {
 
 		mongoClient = new MongoClient(address, port);
 
 	}
 
 	/**
 	 * 
 	 * Sets the database of the Mongo DB controller.
 	 * 
 	 * @param dbName
 	 *            - The name of the Database to access
 	 * 
 	 */
 	public void setDatabase(String dbName) {
 		db = mongoClient.getDB(dbName);
 
 		initiatives = db.getCollection("initiatives");
 		risks = db.getCollection("risks");
 		milestones = db.getCollection("milestones");
 		events = db.getCollection("events");
 		users = db.getCollection("users");
 
 		ids = new BasicDBList();
 	}
 
 	/**
 	 * Closes the connection to the database.
 	 */
 	public void closeConnection() {
 		mongoClient.close();
 	}
 
 	public Object getInitiativeById(String entityId) {
 
 		BasicDBObject entityIdQuery = new BasicDBObject("entityId", entityId);
 
 		DBCursor cursor = initiatives.find(entityIdQuery);
 
 		DBObject temp = new BasicDBObject();
 
 		try {
 			while (cursor.hasNext()) {
 				temp = cursor.next();
 			}
 		} finally {
 			cursor.close();
 		}
 
 		return temp;
 
 	}
 
 	/**
 	 * Get a specific milestone by its ID.
 	 * 
 	 * @param entityId
 	 * @return entity object
 	 */
 	public Object getMilestoneById(String entityId) {
 		BasicDBObject entityIdQuery = new BasicDBObject("entityId", entityId);
 
 		DBCursor cursor = milestones.find(entityIdQuery);
 
 		DBObject temp = new BasicDBObject();
 
 		try {
 			while (cursor.hasNext()) {
 				temp = cursor.next();
 			}
 		} finally {
 			cursor.close();
 		}
 
 		return temp;
 	}
 
 	/**
 	 * Get a specific risk by its ID.
 	 * 
 	 * @param entityId
 	 * @return an entity object
 	 */
 	public Object getRiskById(String entityId) {
 		BasicDBObject entityIdQuery = new BasicDBObject("entityId", entityId);
 
 		DBCursor cursor = risks.find(entityIdQuery);
 
 		DBObject temp = new BasicDBObject();
 
 		try {
 			while (cursor.hasNext()) {
 				temp = cursor.next();
 			}
 		} finally {
 			cursor.close();
 		}
 
 		return temp;
 	}
 
 	/**
 	 * Gets events that the user is part of aka ones that they reported, are
 	 * assigned to, are watchers of or is the buisiness owner of.
 	 * 
 	 * @param user
 	 * @return
 	 */
 	public Object[] getEventsForUser(String user) {
 		ArrayList<DBObject> results = new ArrayList<DBObject>();
 
 		// Query that returns results that the user is part of
 		DBObject baseQuery = new QueryBuilder()
 				.or(new BasicDBObject("assignee", user))
 				.or(new BasicDBObject("watchers", user))
 				// does this work because watchers is a list?
 				.or(new BasicDBObject("reporter", user))
 				.or(new BasicDBObject("businessOwner", user)).get();
 
 		/* ------------- Get Results --------------- */
 
 		// Get all events associated with initiatives user is part of
 
 		DBCursor queryCursor = initiatives.find(baseQuery);
 		setIdsFromQueryResults(queryCursor);
 
 		BasicDBObject eventQuery = new BasicDBObject("entity.entityId",
 				new BasicDBObject("$in", ids));
 
 		queryCursor = events.find(eventQuery.append("entity.entityType",
 				"INITIATIVE"));
 		results.addAll(getResults(queryCursor));
 
 		ids.clear();
 
 		// Get all events associated with risks user is part of
 
 		queryCursor = risks.find(baseQuery);
 		setIdsFromQueryResults(queryCursor);
 
 		eventQuery = new BasicDBObject("entity.entityId", new BasicDBObject(
 				"$in", ids));
 
 		queryCursor = events.find(eventQuery
 				.append("entity.entityType", "RISK"));
 		results.addAll(getResults(queryCursor));
 
 		ids.clear();
 
 		// Get all milestones associated with results user is part of
 
 		queryCursor = milestones.find(baseQuery);
 		setIdsFromQueryResults(queryCursor);
 
 		eventQuery = new BasicDBObject("entity.entityId", new BasicDBObject(
 				"$in", ids));
 
 		queryCursor = events.find(eventQuery.append("entity.entityType",
 				"MILESTONE"));
 		results.addAll(getResults(queryCursor));
 
 		return results.toArray();
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public Object[] getTeamEventsForUser(String user) {
 		ArrayList<DBObject> results = new ArrayList<DBObject>();
 
 		DBObject query = new BasicDBObject("username", user);
 		DBCursor cursor = users.find(query);
 
 		ArrayList<String> groups = new ArrayList<String>();
 		try {
 			while (cursor.hasNext()) {
 				groups = (ArrayList<String>) cursor.next().get("groups");
 			}
 		} finally {
 			cursor.close();
 		}
 
 		BasicDBObject eventQuery = new BasicDBObject("entity.entityId",
 				new BasicDBObject("$in", ids));
 
 		// for each group that the user is part of
 		for (String group : groups) {
 
 			// find all team initiative related events
 
 			DBObject teamQuery = new QueryBuilder()
 					.or(new BasicDBObject("businessGroups", group))
 					.or(new BasicDBObject("providerGroups", group)).get();
 
 			DBCursor teamCursor = initiatives.find(teamQuery);
 			setIdsFromQueryResults(teamCursor);
 
 			teamCursor = events.find(eventQuery.append("entity.entityType",
 					"INITIATIVE"));
 
 			results.addAll(getResults(teamCursor));
 			ids.clear();
 
 			// find all team risk related events
 
 			teamCursor = risks.find(teamQuery);
 			setIdsFromQueryResults(teamCursor);
 
 			eventQuery = new BasicDBObject("entity.entityId",
 					new BasicDBObject("$in", ids));
 			teamCursor = events.find(eventQuery.append("entity.entityType",
 					"RISK"));
 
 			results.addAll(getResults(teamCursor));
 			ids.clear();
 
 			// find all team milestone related events
 
 			teamCursor = milestones.find(teamQuery);
 			setIdsFromQueryResults(teamCursor);
 
 			eventQuery = new BasicDBObject("entity.entityId",
 					new BasicDBObject("$in", ids));
 			teamCursor = events.find(eventQuery.append("entity.entityType",
 					"MILESTONE"));
 
 			results.addAll(getResults(teamCursor));
 			ids.clear();
 		}
 
 		return results.toArray();
 	}
 
 	/**
 	 * Gets all events of that user's organization .
 	 * 
 	 * @param user
 	 * @return
 	 */
 	public Object[] getOrgEventsForUser(String user) {
 		ArrayList<DBObject> results = new ArrayList<DBObject>();
 
 		DBObject baseQuery = new QueryBuilder()
 				.or(new BasicDBObject("allowedAccessUsers", user))
				.or(new BasicDBObject("allowedAccessUsers", new BasicDBObject("$size", 0))).get();
 
 		/* ------------- Get Results --------------- */
 
 		// Get all events associated with initiatives user is part of
 
 		DBCursor queryCursor = initiatives.find(baseQuery);
 		setIdsFromQueryResults(queryCursor);
 
 		BasicDBObject eventQuery = new BasicDBObject("entity.entityId",
 				new BasicDBObject("$in", ids));
 
 		queryCursor = events.find(eventQuery.append("entity.entityType",
 				"INITIATIVE"));
 		results.addAll(getResults(queryCursor));
 
 		ids.clear();
 
 		// Get all events associated with risks user is part of
 
 		queryCursor = risks.find(baseQuery);
 		setIdsFromQueryResults(queryCursor);
		
 		eventQuery = new BasicDBObject("entity.entityId", new BasicDBObject(
 				"$in", ids));
 
 		queryCursor = events.find(eventQuery
 				.append("entity.entityType", "RISK"));
 		results.addAll(getResults(queryCursor));
 
 		ids.clear();
 
 		// Get all milestones associated with results user is part of
 
 		queryCursor = milestones.find(baseQuery);
 		setIdsFromQueryResults(queryCursor);
 
 		eventQuery = new BasicDBObject("entity.entityId", new BasicDBObject(
 				"$in", ids));
 
 		queryCursor = events.find(eventQuery.append("entity.entityType",
 				"MILESTONE"));
 		results.addAll(getResults(queryCursor));
 
 		return results.toArray();
 	}
 
 	/*
 	 * -----------Helper Functions--------------
 	 */
 
 	private void setIdsFromQueryResults(DBCursor resultsCursor) {
 
 		DBObject temp;
 		try {
 			while (resultsCursor.hasNext()) {
 				temp = resultsCursor.next();
 				try {
 					ids.add(((BasicDBObject) temp).getString("entityId"));
 				} catch (NullPointerException e) {
 					// This occurs when there is no element entityId on the
 					// object.
 					// TODO: Setup logging so we can find these rogue entities
 					// and destroy them
 				}
 			}
 		} finally {
 			resultsCursor.close();
 		}
 	}
 
 	private ArrayList<DBObject> getResults(DBCursor resultsCursor) {
 		ArrayList<DBObject> temp = new ArrayList<DBObject>();
 
 		try {
 			while (resultsCursor.hasNext()) {
 				temp.add(resultsCursor.next());
 			}
 		} finally {
 			resultsCursor.close();
 		}
 
 		return temp;
 	}
 }
