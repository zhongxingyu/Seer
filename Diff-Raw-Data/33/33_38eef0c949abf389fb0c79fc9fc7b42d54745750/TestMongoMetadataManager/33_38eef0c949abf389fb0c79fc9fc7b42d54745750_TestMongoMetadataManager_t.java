 package org.lockss.metadata;
 
 import static org.lockss.db.MongoDbManager.PUBLICATIONS_COLLECTION;
 import static org.lockss.db.MongoDbManager.PUBLISHERS_COLLECTION;
 
 import java.io.File;
 import java.util.Properties;
 
 import org.lockss.config.ConfigManager;
 import org.lockss.db.MongoDbManager;
 import org.lockss.db.MongoHelper;
 import org.lockss.db.SqlDbManager;
 import org.lockss.plugin.PluginManager;
 import org.lockss.plugin.simulated.SimulatedArchivalUnit;
 import org.lockss.test.ConfigurationUtil;
 import org.lockss.test.LockssTestCase;
 import org.lockss.test.MockLockssDaemon;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.QueryBuilder;
 
 public class TestMongoMetadataManager extends LockssTestCase {
 	
 	  private MockLockssDaemon theDaemon;
 	  private MongoMetadataManager mongoMetadataManager;
 	  private MongoDbManager mongoDbManager;
 	  private PluginManager pluginManager;
 	  private String tempDirPath;
 	   
 	  @Override
 	  public void setUp() throws Exception {
 	    super.setUp();
 	    
 	    tempDirPath = getTempDir().getAbsolutePath();
 
 	    // set derby database log 
 	    System.setProperty("derby.stream.error.file",
 	                       new File(tempDirPath,"derby.log").getAbsolutePath());
 
 	    Properties props = new Properties();
 	    props.setProperty(SqlMetadataManager.PARAM_INDEXING_ENABLED, "true");
 	    props.setProperty(ConfigManager.PARAM_PLATFORM_DISK_SPACE_LIST,
 			      tempDirPath);
 	    ConfigurationUtil.setCurrentConfigFromProps(props);
 	    
 	    theDaemon = getMockLockssDaemon();
 	    theDaemon.getAlertManager();
 	    pluginManager = theDaemon.getPluginManager();
 	    pluginManager.setLoadablePluginsReady(true);
 	    theDaemon.setDaemonInited(true);
 	    pluginManager.startService();
 	    
 	    mongoDbManager = new MongoDbManager();
 	    theDaemon.setDbManager(mongoDbManager);
 	    mongoDbManager.initService(theDaemon);
 	    mongoDbManager.startService();
 	    
 	    mongoMetadataManager = new MongoMetadataManager();
 	    theDaemon.setMetadataManager(mongoMetadataManager);
 	    mongoMetadataManager = new MongoMetadataManager();
 	    mongoMetadataManager.initService(theDaemon);
 	    mongoMetadataManager.startService();
 	    
 	    DB mongoDatabase = mongoDbManager.getDb();
 	    mongoDatabase.dropDatabase();
 	  }
 	  
 	  
 	  public void testUniqueLongID() throws Exception{
 		  
 		  long createID =  mongoMetadataManager.findOrCreatePublisher("TestLongID");
 		  long createID2 =  mongoMetadataManager.findOrCreatePublisher("TestLongID2");
 		  
 		  assertNotEquals(createID, createID2);
 	
 		 
 	  }
 	  
 	  public void testCreateAndFindPublisher2() throws Exception{
 		  
 		  long createID =  mongoMetadataManager.findOrCreatePublisher("Create And Find");
 		  long findID = mongoMetadataManager.findOrCreatePublisher("Create And Find");
 		  assertEquals(createID, findID);
 		  
 	  }
 	  	  
 	  
 	  public void testCreateAndFindPlugin() throws Exception{
 		  
 		  long createID =  mongoMetadataManager.findOrCreatePlugin("test plugin", "windows");
 		  long findID = mongoMetadataManager.findOrCreatePlugin("test plugin", "windows");
 		  assertEquals(createID, findID);
 		  
 	  }
 	  
 	  public void testCreateAndFindPublication()throws Exception
 	  {
 		  long createID =  mongoMetadataManager.findOrCreatePublication("pIssn", "eIssn", "pIsbn", "eIsbn", 1111111L, "name", "date", "proprietaryId", "2");
 		  assertNotNull(createID);
 		  
 	  }
 
 
     
     public void testSearchISSN2() throws Exception
     {
         DB mongoDatabase = mongoDbManager.getDb();
         DBCollection collection = mongoDatabase.getCollection(PUBLICATIONS_COLLECTION);
     	
         long createPibsnID = mongoMetadataManager.findOrCreatePublication("abc", null, "pIsbn", "eIsbn", 1111111L, "name", "date", "proprietaryId", "2");
         long createEissnID = mongoMetadataManager.findOrCreatePublication(null, "abc", "pIsbn2", "eIsbn2", 222222L, "name2", "date2", "proprietaryId", "2");
         
         long createEissnID2 = mongoMetadataManager.findOrCreatePublication(null, "123", "pIsbn2", "eIsbn2", 1111111L, "name2", "date2", "proprietaryId", "2");
     	
 	    DBObject finalQuery = QueryBuilder.start().and(
                 QueryBuilder.start("publisherSeq").is(1111111L).get(),
                 QueryBuilder.start("proprietaryId").is("proprietaryId").get(),
                 QueryBuilder.start().or(
                         QueryBuilder.start("pIssn").is("abc").get(),
                         QueryBuilder.start("eIssn").is("abc").get()
                 ).get()
         ).get();
 	    	    
 	   DBCursor result = collection.find(finalQuery);
 	   
 	   int i = result.count();
	    assertEquals(2, i);
     }
     
     public void testCreateChildPublication()throws Exception
     {
     	
     	 DB mongoDatabase = mongoDbManager.getDb();
          DBCollection collection = mongoDatabase.getCollection(PUBLICATIONS_COLLECTION);
      	
          long createPibsnID = mongoMetadataManager.findOrCreatePublication("123", null, "pIsbn", "eIsbn", 1111111L, "name", "date", "proprietaryId", "2");
          long createEissnID = mongoMetadataManager.findOrCreatePublication("123", null, "pIsbn2", "eIsbn2", 1111111L, "name2", "date2", "proprietaryId", "2");
     	
     }
 
 
 }
