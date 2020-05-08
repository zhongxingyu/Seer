 package net.onlite.morplay;
 
 import com.github.jmkgreen.morphia.annotations.Embedded;
 import com.github.jmkgreen.morphia.annotations.Entity;
 import com.google.common.collect.Iterables;
 import net.onlite.morplay.mongo.MongoConfig;
 import net.onlite.morplay.mongo.MongoConnection;
 import net.onlite.morplay.mongo.MongoStore;
 import play.Application;
 import play.Play;
 import play.Plugin;
 import play.libs.Classpath;
 
 /**
  * Responsible for integration with Playframework
  */
 public class MorplayPlugin extends Plugin {
     /**
      * Mongo connection
      */
     private static MongoConnection connection;
 
     /**
      * Application instance
      */
     private Application application;
 
     /**
      * Instantiate plugin
      * @param application Application instance
      */
     public MorplayPlugin(Application application) {
 
         this.application = application;
     }
 
     /**
      * Get mongo connection instance
      * @return Mongo storage instance
      */
     public static MongoConnection connection() {
         return connection;
     }
 
     /**
      * Get default data store
      * @return Default data store instance
      */
     public static MongoStore store() {
         return connection.store();
     }
 
     /**
      * Get data store by name
      * @return Data store instance
      */
     public static MongoStore store(String name) {
         return connection.store(name);
     }
 
 
     @Override
     public void onStart() {
         // Read configuration
         MongoConfig config = new MongoConfig();
 
         // Get entities and embedded classes
         Iterable<String> entities = Classpath.getTypesAnnotatedWith(application, "models", Entity.class);
         Iterable<String> embedded = Classpath.getTypesAnnotatedWith(application, "models", Embedded.class);
 
         // Initialize mongo connection
         connection = new MongoConnection(config, Iterables.concat(entities, embedded), application);
     }
 
     @Override
     public void onStop() {
         if (connection != null) {
             connection.close();
         }
 
         connection = null;
     }
 }
