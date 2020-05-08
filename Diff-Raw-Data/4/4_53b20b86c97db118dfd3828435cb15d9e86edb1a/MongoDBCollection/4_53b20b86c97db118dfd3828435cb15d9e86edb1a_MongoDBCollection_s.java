 package org.seventyeight.database.mongodb;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import org.apache.log4j.Logger;
 import org.bson.types.ObjectId;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author cwolfgang
  *         Date: 16-02-13
  *         Time: 21:45
  */
 public class MongoDBCollection {
 
     private static Logger logger = Logger.getLogger( MongoDBCollection.class );
 
     private DBCollection collection;
 
     public MongoDBCollection( DBCollection collection ) {
         this.collection = collection;
     }
 
     public MongoDBCollection( String colletionName ) {
         this.collection = MongoDBManager.getInstance().getDatabase().getCollection( colletionName ).getCollection();
     }
 
     public static MongoDBCollection get( String colletionName ) {
         return MongoDBManager.getInstance().getDatabase().getCollection( colletionName );
     }
 
     public MongoDBCollection save( MongoDocument document ) {
         collection.save( document.getDBObject() );
 
         return this;
     }
 
     public DBCollection getCollection() {
         return collection;
     }
 
     public void listDocuments() {
         logger.debug( "Number: " + collection.getCount() );
     }
 
 
     public List<MongoDocument> find( MongoDBQuery query ) {
         List<DBObject> objs = collection.find( query.getDocument() ).toArray();
         List<MongoDocument> docs = new ArrayList<MongoDocument>( objs.size() );
 
         for( DBObject obj : objs ) {
             docs.add( new MongoDocument( obj ) );
         }
 
         return docs;
     }
 
     public List<MongoDocument> find( MongoDBQuery query, int offset, int limit ) {
         List<DBObject> objs = collection.find( query.getDocument() ).skip( offset ).limit( limit ).toArray();
         List<MongoDocument> docs = new ArrayList<MongoDocument>( objs.size() );
 
         for( DBObject obj : objs ) {
             docs.add( new MongoDocument( obj ) );
         }
 
         return docs;
     }
 
     public MongoDocument getDocumentById( String id ) {
         BasicDBObject query = new BasicDBObject();
         query.put( "_id", new ObjectId( id ) );
         DBObject dbObj = collection.findOne( query );
         return new MongoDocument( dbObj );
     }
 
     public void createIndex( String name, MongoDocument index ) {
         collection.ensureIndex( index.getDBObject(), name );
     }
 
     public void remove( MongoDBQuery query ) {
         collection.remove( query.getDocument() );
     }
 
     public void update( MongoUpdate update ) {
         update( update, new MongoDBQuery() );
     }
 
     public void update( MongoUpdate update, MongoDBQuery query ) {
         if( update.getDocument().isEmpty() ) {
 
         } else {
             logger.debug( "Criteria: " + query.getDocument() );
             logger.debug( "Update  : " + update );
             collection.update( query.getDocument(), update.getDocument(), update.isUpsert(), update.isMulti() );
         }
     }
 
     public void show() {
         DBCursor cursor = collection.find();
 
         while( cursor.hasNext() ) {
             DBObject d = cursor.next();
             logger.info( d );
         }
     }
 }
