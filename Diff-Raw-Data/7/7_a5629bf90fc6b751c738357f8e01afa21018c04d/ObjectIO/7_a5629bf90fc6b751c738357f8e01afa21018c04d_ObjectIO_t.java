 package thesis.machine;
 
 import com.db4o.Db4o;
 import com.db4o.ObjectContainer;
 import com.db4o.ObjectSet;
 import com.db4o.query.Predicate;
 import java.util.UUID;
 
 import thesis.core.*;
 
 /**
  *
  * @author Daniel <dmilith> Dettlaff
  */
 public class ObjectIO extends Cobject {
 
     // directly access to ObjectContainer class from ObjectIO
     private ObjectContainer db;
     // is ObjectIO connected to database?
     private boolean connected = false;
 
    /**
      * returns reference to ObjectContainer which we operate on
      */
     public ObjectContainer getDb() {
         return db;
     }
 
    /**
      * creates connection to db4o
      */
     public void createConnectionToDb( String filename ) {
         db = Db4o.openFile( filename );
         connected = true;
     }
 
    /**
      * closing connection to db4o
      */
     public void closeConnectionToDb() {
         db.close();
         connected = false;
     }
 
    /**
      * save an Cobject or his children
      */
     public void save( Cobject myobj ) {
         if (!connected) return;
           db.set( myobj );
           if (debug) System.out.println( "DEBUG: Stored " + myobj );
     }
 
    /**
      * native load Cobject by uuid
      */
     public ObjectSet loadByUUID( final UUID uuid ) {
         if (!connected) return null;
           ObjectSet objects = db.query( new Predicate<Cobject>() {
             public boolean match(Cobject object) {
                 return object.getUUID().equals( uuid );
             }
           });
           return objects;
     }
 
    /**
      * native load Cobject (or list of Cobjects) by uuid of parent
      */
     public ObjectSet loadByParent( final UUID parent ) {
         if (!connected) return null;
           ObjectSet objects = db.query( new Predicate<Cobject>() {
             public boolean match(Cobject object) {
                 return object.getParent() == ( parent );
             }
           });
           return objects;
     }
 
     // TODO find best algorithm for finding newest object version
     public Cobject findNewestVersion( ObjectSet anObject ) {
         return null;
     }
 
    /**
      * native load Cobject (or list of Cobjects) by uuid of parent
      */
     public ObjectSet loadByType( final Cobject parent ) {
         if (!connected) return null;
           ObjectSet objects = db.query( new Predicate<Cobject>() {
             public boolean match(Cobject object) {
                 return object.getClass().equals( parent.getClass() );
             }
           });
           return objects;
     }
 
    /**
      * objectCount() will return amount of objects in database
      */
     public int objectCount() {
         if (!connected) return -1;
           ObjectSet results = db.queryByExample( new Cobject( false ) );
          return results.size();
     }
 
 }
