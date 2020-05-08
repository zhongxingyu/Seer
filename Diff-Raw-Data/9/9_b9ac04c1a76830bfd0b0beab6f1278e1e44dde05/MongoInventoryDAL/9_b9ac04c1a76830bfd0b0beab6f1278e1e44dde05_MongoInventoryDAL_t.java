 package com.stuffsystem.rest;
 
 import java.net.UnknownHostException;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 import com.mongodb.MongoException;
 import com.mongodb.WriteResult;
 import com.mongodb.util.JSON;
 
 /// This is a class that implements the DAL interface for a Mongo database.
 public class MongoInventoryDAL implements InventoryDAL
 {
     private static MongoClient client = null;
 
     // database handle
     private DB db = null;
 
     // items collection
     private DBCollection itemColl = null;
 
     public MongoInventoryDAL() {
         if(null == client) {
             try {
                 client = new MongoClient();
             }
             catch(Exception e) {
                 e.printStackTrace();
             }
         }
 
         // get the db
         db = client.getDB("stuff");
 
         // items collection
         itemColl = db.getCollection("items");
     }
 
     /// Get the number of items in the database.
     public long getItemCount() {
         return itemColl.count();
     }
 
     /// Remove all documents from the items collection.
     public long removeAllItems() {
         WriteResult deleteResult = null;
 
         try {
             deleteResult = itemColl.remove(new BasicDBObject());
         }
         catch(MongoException mE) {
             return -1;
         }
 
         return deleteResult.getN();
     }
 
     /// Delete all items in the items collection then add some test data.
     /// Returns:  A long int indicating the number of items now in the collection.
     public long resetItemsCollectionForTest(String dataSetName) {
         removeAllItems();
         long count = itemColl.count();
         if(0==count) {
             // OK
             // TODO
         }
         // else count is zero, and reset failed.
         return count;
     }
 
     /// Post item
     public String postItem(String jsonItem) {
        return "TODO";
 
     }
 
 
 }
 
