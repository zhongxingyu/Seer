 package net.bbm485.db;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.google.gson.GsonBuilder;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.util.JSON;
 import org.bson.types.ObjectId;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import net.bbm485.exceptions.UserNotFoundException;
 
 public class DBManager {
 
     private Mongo mongo;
     private DB db;
     private DBCollection collection;
     private String dbName;
     private String collectionName;
 
     /**
      * * Getters and Setters **
      */
     public String getDbName() {
         return dbName;
     }
 
     public String getCollectionName() {
         return collectionName;
     }
 
     private void setDbName(String dbName) {
         this.dbName = dbName;
     }
 
     private void setCollectionName(String collectionName) {
         this.collectionName = collectionName;
     }
 
     /**
      * * End of Getters and Setters **
      */
     public DBManager(String dbName, String collectionName) {
         setDbName(dbName);
         setCollectionName(collectionName);
         initializeDB();
     }
 
     private void initializeDB() {
         try {
             mongo = new Mongo();
             db = mongo.getDB(dbName);
             collection = db.getCollection(collectionName);
 
         }
         catch (UnknownHostException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         catch (Exception ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public User getUser(String userId) throws UserNotFoundException {
         try {
             DBObject obj = new BasicDBObject("_id", new ObjectId(userId));
             DBObject userObj = collection.findOne(obj);
             User user = convertDBObject2User(userObj);
             return user;
         }
        catch (IllegalArgumentException e) {
             JSONObject errorMsg = new JSONObject();
             try {
                 errorMsg.put("fieldName", "userId").put("rejectedValue", userId);
             }
             catch (JSONException ex) {
             
             }
             throw new UserNotFoundException(errorMsg);
         }
     }
 
     public void createUser(User user) {
         DBObject dbObj = (DBObject) JSON.parse(user.toJson());
         collection.insert(dbObj);
         ObjectId id = (ObjectId) dbObj.get("_id");
         user.setId(id.toString());
         collection.update(dbObj, (DBObject) JSON.parse(user.toJson()));
     }
 
     public ArrayList<User> getUserList() {
         ArrayList<User> userList = new ArrayList<User>();
         DBCursor cursor = collection.find();
         while (cursor.hasNext())
             userList.add(convertDBObject2User(cursor.next()));
         return userList;
     }
 
     public void updateUser(String userId, JSONObject info) throws UserNotFoundException {
         try {
             DBObject obj = new BasicDBObject("_id", new ObjectId(userId));
             DBObject userObj = collection.findOne(obj);
             User foundUser = convertDBObject2User(userObj);
             foundUser.updateInfo(info);
             collection.update(userObj, convertUser2DBObject(foundUser));
         }
         catch (Exception e) {
             JSONObject errorMsg = new JSONObject();
             try {
                 errorMsg.put("fieldName", "userId").put("rejectedValue", userId);
             }
             catch (JSONException ex) {
             }
             throw new UserNotFoundException(errorMsg);
         }
     }
     
     public void deleteUser(String userId) throws UserNotFoundException {
         try {
             DBObject obj = new BasicDBObject("_id", new ObjectId(userId));
             DBObject userObj = collection.findOne(obj);
             collection.remove(userObj);
         }
         catch (Exception e) {
             JSONObject errorMsg = new JSONObject();
             try {
                 errorMsg.put("fieldName", "userId").put("rejectedValue", userId);
             }
             catch (JSONException ex) {
             }
             throw new UserNotFoundException(errorMsg);
         }
     }
     
     private DBObject convertUser2DBObject(User user) {
         return (DBObject) JSON.parse(user.toJson());
     }
 
     private User convertDBObject2User(DBObject obj) {
         return new GsonBuilder().serializeNulls().create().fromJson(JSON.serialize(obj), User.class);
     }
 }
