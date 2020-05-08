package org.jenkinsci.plugins.mongodbdocumentupload;
 
 import com.mongodb.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Alex
  * Date: 08/05/13
  * Time: 23:04
  * To change this template use File | Settings | File Templates.
  */
 public class JsonInsert {
 
     private MongoClient _mongoClient;
     private String _dbName;
     private String _collectionName;
 
     public JsonInsert(MongoClient mongoClient, String dbName, String collectionName) {
         _mongoClient = mongoClient;
         _dbName = dbName;
         _collectionName = collectionName;
     }
 
     public void Insert(String jsonDocument)
     {
         DB db = _mongoClient.getDB(_dbName);
         DBCollection collection = db.getCollection(_collectionName);
 
         Object o = com.mongodb.util.JSON.parse(jsonDocument);
         DBObject dbObj = (DBObject) o;
 
         collection.insert(dbObj);
     }
 }
