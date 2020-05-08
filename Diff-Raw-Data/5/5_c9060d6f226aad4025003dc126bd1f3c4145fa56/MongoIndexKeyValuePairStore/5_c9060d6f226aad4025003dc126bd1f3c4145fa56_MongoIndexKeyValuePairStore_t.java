 package fi.iki.tpp.neo4j.graphdb.index.mongodb;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.neo4j.helpers.Pair;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.WriteResult;
 
 import fi.iki.tpp.neo4j.graphdb.index.AbstractKeyValuePairStore;
 
 public class MongoIndexKeyValuePairStore extends AbstractKeyValuePairStore {
 	private DB db;
 	private DBCollection lookupCollection;
 
 	public MongoIndexKeyValuePairStore(
 			String hostName,
 			Integer port,
 			String databaseName,
 			String indexCollectionName
 			) throws Exception {
 		assertInputs(hostName, port, databaseName, indexCollectionName);
 
 		Mongo mongo = new Mongo(hostName, port);
 		this.db = mongo.getDB(databaseName);
 		this.lookupCollection = db.getCollection(indexCollectionName);
 
 		createIndexes();
 	}
 
 	private void assertInputs(String hostName, Integer port, String databaseName, String indexCollectionName) {
 		if (hostName == null || hostName.isEmpty()) throw new IllegalArgumentException("hostName not set");
 		if (databaseName == null || databaseName.isEmpty()) throw new IllegalArgumentException("databaseName not set");
 		if (indexCollectionName == null || indexCollectionName.isEmpty()) throw new IllegalArgumentException("indexCollectionName not set");
 		if (port == null) {
 			throw new IllegalArgumentException("port not set");
 		} else if (port < 1) {
 			throw new IllegalArgumentException(String.format("invalid port %d", port));
 		}
 	}
 
 	private void createIndexes() {
 		DBObject options = new BasicDBObject("background", true);
 
 		this.lookupCollection.ensureIndex(new BasicDBObject("key", 1), options);
 		this.lookupCollection.ensureIndex(new BasicDBObject("indexName", 1), options);
 	}
 
 	@Override
 	public void add(String indexName, String propertyName, Object propertyValue, Long entityId) {
 		DBObject object = new BasicDBObject();
 		object.put("key", getKey(indexName, propertyName, propertyValue));
 		object.put("entityId", entityId);
 		object.put("indexName", indexName);
 		object.put("propertyName", propertyName);
 		object.put("propertyValue", propertyValue);
 
 		WriteResult result = lookupCollection.insert(object);
		if (result.getCachedLastError() != null) result.getCachedLastError().throwOnError();
 	}
 
 	@Override
 	public void remove(String indexName, String propertyName, Object propertyValue, Long entityId) {
 		DBObject object = new BasicDBObject();
 		object.put("$and", new DBObject[] {
 				new BasicDBObject("key", getKey(indexName, propertyName, propertyValue)),
 				new BasicDBObject("entityId", entityId)
 				});
 
 		WriteResult result = lookupCollection.remove(object);
		if (result.getCachedLastError() != null) result.getCachedLastError().throwOnError();
 	}
 
 	@Override
 	public Collection<Long> get(String indexName, String propertyName, Object value) {
 		DBCursor cur = lookupCollection.find(new BasicDBObject("key", getKey(indexName, propertyName, value)));
 
 		List<Long> results = new ArrayList<Long>();
 		while(cur.hasNext()) {
 			DBObject result = cur.next();
 			results.add((Long) result.get("entityId"));
 		}
 
 		return results; 
 	}
 
 	@Override
 	public Collection<Pair<String, Object>> findByEntityId(String indexName, Long entityId) {
 		DBObject query = new BasicDBObject();
 		query.put("$and", new DBObject[] { new BasicDBObject("indexName", indexName), new BasicDBObject("value", entityId) });
 
 		List<Pair<String, Object>> results = new ArrayList<Pair<String, Object>>();
 		DBCursor cur = lookupCollection.find(query);
 		while(cur.hasNext()) {
 			DBObject result = cur.next();
 			results.add(Pair.of((String) result.get("propertyName"), result.get("propertyValue")));
 		}
 
 		return results;
 	}
 }
