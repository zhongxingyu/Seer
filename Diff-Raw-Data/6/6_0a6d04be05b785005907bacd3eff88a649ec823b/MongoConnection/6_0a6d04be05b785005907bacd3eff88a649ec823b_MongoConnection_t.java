 package net.onlite.morplay.mongo;
 
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.Morphia;
 import com.mongodb.Mongo;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Responsible for connection to MongoDB host or cluster.
  */
 public class MongoConnection {
     /**
      * Morphia instance
      */
     private final Morphia morphia = new Morphia();
 
     /**
      * Mongo instance
      */
     private final Mongo mongo;
 
     /**
      * Default database name
      */
     private String defaultStoreName = "test";
 
     /**
      * Mongo host databases
      */
     private final Map<String, MongoStore> stores = new HashMap<>();
 
     /**
      * Constructor.
      * Initialize mongo connection.
      * @param config Mongo configuration
      */
     public MongoConnection(MongoConfig config) {
         // Initialize connection to server
         mongo = new Mongo(config.getServerAddresses());
         mongo.setWriteConcern(config.getDefaultWriteConcern());
 
        defaultStoreName = config.getDatabases().get(0).getName();

         // Initialize data stores
         for (MongoConfig.DbConfig dbConf : config.getDatabases()) {
             Datastore ds;
             if (dbConf.isSecure()) {
                 ds = morphia.createDatastore(mongo, dbConf.getName(),
                         dbConf.getLogin(), dbConf.getPassword());
             } else {
                 ds = morphia.createDatastore(mongo, dbConf.getName());
             }
 
             stores.put(dbConf.getName(), new MongoStore(ds));
         }
     }
 
     /**
      * Get default data store
      * @return Data store instance or null
      */
     public MongoStore store() {
         return stores.get(defaultStoreName);
     }
 
     /**
      * Get data store by name
      * @param name Data store name
      * @return Data store instance
      */
     public MongoStore store(String name) {
         return stores.get(name);
     }
 
     /**
      * Close connection
      */
     public void close() {
         MongoStore.resetCache();
         stores.clear();
         mongo.close();
     }
 
     public Morphia morphia() {
         return morphia;
     }
 
     public Mongo mongo() {
         return mongo;
     }
 }
