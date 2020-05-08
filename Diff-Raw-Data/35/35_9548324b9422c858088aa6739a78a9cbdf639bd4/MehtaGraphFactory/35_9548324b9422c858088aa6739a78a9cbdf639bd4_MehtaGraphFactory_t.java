 package de.deepamehta.mehtagraph;
 
 import de.deepamehta.mehtagraph.impl.Neo4jMehtaGraph;
 
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.factory.GraphDatabaseFactory;
 import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.IndexProvider;
import org.neo4j.index.lucene.LuceneIndexProvider;
import org.neo4j.kernel.ListIndexIterable;
import org.neo4j.kernel.impl.cache.CacheProvider;
import org.neo4j.kernel.impl.cache.SoftCacheProvider;

import java.util.ArrayList;
import java.util.List;
 import java.util.logging.Logger;
 
 
 
 public class MehtaGraphFactory {
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     private static Logger logger = Logger.getLogger("MehtaGraphFactory");
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
     public static MehtaGraph createInstance(String databasePath) {
         GraphDatabaseService neo4j = null;
         try {
             // ### TODO: activate for store upgrade
             /* neo4j = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(databasePath)
                 .setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
                 .newGraphDatabase(); */
            //
            // the cache providers
            List<CacheProvider> cacheProviders = new ArrayList();
            cacheProviders.add(new SoftCacheProvider());
            //
            // the index providers
            List<IndexProvider> provs = new ArrayList();
            provs.add(new LuceneIndexProvider());
            ListIndexIterable indexProviders = new ListIndexIterable();
            indexProviders.setIndexProviders(provs);
            //
            // the database setup
            GraphDatabaseFactory factory = new GraphDatabaseFactory();
            factory.setCacheProviders(cacheProviders);
            factory.setIndexProviders(indexProviders);
            neo4j = factory.newEmbeddedDatabase(databasePath);
            //
             return new Neo4jMehtaGraph(neo4j);
         } catch (Exception e) {
             if (neo4j != null) {
                 logger.info("Shutdown Neo4j");
                 neo4j.shutdown();
             }
             throw new RuntimeException("Creating a Neo4jMehtaGraph instance failed", e);
         }
     }
 }
