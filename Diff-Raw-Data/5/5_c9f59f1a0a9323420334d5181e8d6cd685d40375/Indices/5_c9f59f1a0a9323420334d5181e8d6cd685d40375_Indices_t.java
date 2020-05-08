 
 import java.util.*;
 import java.net.*;
 
 import com.mongodb.*;
 import com.mongodb.util.*;
 
 public class Indices {
     public static void setup() 
         throws UnknownHostException {
 
         Mongo m = new Mongo( new DBAddress( "127.0.0.1:27017/driver_test_framework" ) );
         DBCollection c = m.getCollection( "y" );
         c.dropIndexes();
         c.drop();
 
         c = m.getCollection( "x" );
         c.dropIndexes();
         c.drop();
 
         DBObject obj = new BasicDBObject();
         obj.put( "field1", "f1" );
         obj.put( "field2", "f2" );
         c.save( obj );
 
         obj = new BasicDBObject();
         obj.put( "field1", 1 );
         c.ensureIndex( obj );
        DBCursor cursor = c.find();
        cursor.next();
 
         obj = new BasicDBObject();
         obj.put( "field2", 1 );
         c.ensureIndex( obj );
        cursor = c.find();
        cursor.next();
     }
 
     public static void validate() {
         return;
     }
 }
