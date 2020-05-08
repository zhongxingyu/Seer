 import java.net.UnknownHostException;
 import java.util.Date;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.Mongo;
 
 public class DBcontroller {
 	private Mongo mongoInstance;
 	private DB mongoDB;
 	private DBCollection convos;
 
 	private BasicDBObject symptoms;
 	private BasicDBObject disorders;
 	private BasicDBObject triggers;
 	private BasicDBObject convo;
 
 	public DBcontroller() throws UnknownHostException{
 		mongoInstance = new Mongo("localhost", 27017);		// Connects to local host Mongo Instance on port 27017
 		mongoDB = mongoInstance.getDB("easy-diagnosis"); 	// Attempts to get an existing DB instance from mongoInstance, creates one if it does not exist
 		convos = mongoDB.getCollection("conversations"); 	// Attempts to get an existing collection from easy-diagnosis, creates one if it does not exist
 		symptoms = new BasicDBObject();
 		disorders = new BasicDBObject();
 		triggers = new BasicDBObject();
 		convo = new BasicDBObject();
 	}
 
 	// Adds users symptom points to the symptoms document
 	public void addToSymptoms(String[] keys, int[] values){
 		for(int i=0; i<keys.length; i++)
 			symptoms.put(keys[i], values[i]);
 	}
 
 	// Adds users disorder points to the disorders document
 	public void addToDisorders(String[] keys, int[] values){
 		for(int i=0; i<keys.length; i++)
 			disorders.put(keys[i], values[i]);
 	}
 
 	// Adds all words entered by the user to the triggers document
 	public void addToTriggers(String[] keys, int[] values){
 		for(int i=0; i<keys.length; i++)
 			triggers.put(keys[i], values[i]);
 	}
 
 	// Adds each sub-document to the conversation document
 	public void addToConvo(){
 		convo.put("symptoms", symptoms);
 		convo.put("diagnoses", disorders);
 		convo.put("triggers", triggers);
 	}
 
 	// Adds the conversation document to the collection with a date stamp
 	public void addToConvos(){
 		Date date = new Date();
 		convo.put("Date", date.toString());
 		convos.insert(convo);
 	}
 
 	// Prints out all conversation documents in the collection
 	public void printAll(){
 		DBCursor cursor = convos.find();
 		try {
 			while(cursor.hasNext()) {
 				System.out.println(cursor.next());
 			}
 		} finally {
 			cursor.close();
 		}
 	}
 	
	// Empties collection from the database
 	public void clear(){
 		mongoInstance.dropDatabase("easy-diagnosis");
 		System.out.print("Dropped easy-diagnosis database from localhost");
 	}
 }
