 package fulbot.persistence.mongo;
 
 import javax.inject.Inject;
 
 import org.springframework.stereotype.Repository;
 
import com.mongodb.BasicDBList;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.util.JSON;
 
 import fulbot.persistence.IncomingEmailDao;
 
 @Repository
 public class MongoIncomingEmailDao implements IncomingEmailDao {
 
 	private static final String COLLECTION_NAME = "incomingEvents";
 	private final DB db;
 
 	@Inject
 	public MongoIncomingEmailDao(DB db) {
 		this.db = db;
 	}
 
 	@Override
 	public void save(String incomingEmailJson) {
		DBCollection collection = getCollection();
		BasicDBList parsedObjects = (BasicDBList) JSON.parse(incomingEmailJson);
		for (Object incomingEmail : parsedObjects) {
			collection.insert((DBObject)incomingEmail);
		}
 	}
 
 	private DBCollection getCollection() {
 		return db.getCollection(COLLECTION_NAME);
 	}
 	
 	
 	
 }
