 package upTests;
 
 import java.util.HashMap;
 
 import junit.framework.Assert;
  
 import org.junit.BeforeClass;
import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.theories.suppliers.TestedOn;
 
 import upsilon.Configuration;
 import upsilon.Database;
 import upsilon.dataStructures.ResultKarma;
 import upsilon.dataStructures.StructureCommand;
 import upsilon.dataStructures.StructurePeer;
 import upsilon.dataStructures.StructureRemoteService;
 import upsilon.dataStructures.StructureService;
 
 import static org.hamcrest.Matchers.*; 
 import static org.hamcrest.MatcherAssert.*; 
 
 public class DbTest { 
 	private static Database db;

	@Before 
	public void setupConfiguration() {
		Configuration.instance.clear();	
	}
 	 
 	@BeforeClass
 	public static void setupDb() throws Exception {
 		String hostname = System.getProperty("TEST_DB_HOSTNAME", "localhost");
 		String username = System.getProperty("TEST_DB_USER", "root");
 		String password = System.getProperty("TEST_DB_PASS", "");
 		int port = 3306;   
 		String dbname = System.getProperty("TEST_DB_NAME", "upsilon");
 		 
 		db = new Database(hostname, username, password, port, dbname);		
 		db.connect(); 
 	}
 	
 	@Test
 	public void testBadConnections() throws Exception {
 		Assert.assertTrue(db.hasValidConnection());
 		
 		db.disconnect();
 		
 		Assert.assertFalse(db.hasValidConnection());
 		
 		db.connect();
 		 
 		Assert.assertTrue(db.hasValidConnection());  
 	}
 	
 	@Test
 	public void testDb() throws Exception {
 		assertThat(Configuration.instance.services.getImmutable(), hasSize(0));
 		 
 		StructureCommand cmd = new StructureCommand();
 		cmd.setCommandLine("echo");
 		   
 		StructureService service = new StructureService(); 
 		service.setCommand(cmd);    
 		service.addResult(ResultKarma.GOOD, "Test service."); 
 		service.setIdentifier("testService");
 		service.setDatabaseUpdateRequired(true);
 		service.setRegistered(true); 
 		
 		Configuration.instance.services.register(service);
 		assertThat(Configuration.instance.services.getImmutable(), hasSize(1));
 		 
 		assertThat(db.update(), is(true)); 
 		  
 		HashMap<String, String> columns = db.getRow("services", "identifier", "testService", "identifier"); 
 		 
 		assertThat(columns.keySet(), hasSize(1)); 
 		assertThat(columns.keySet(), contains("identifier"));  
 		assertThat(columns.values(), contains("testService"));
 		
 		Configuration.instance.services.remove(service);
 		assertThat(Configuration.instance.services.getImmutable(), hasSize(0));
 		
 		columns = db.getRow("services", "identifier", "testService", "identifier"); 
 		assertThat(columns.keySet(), hasSize(1)); 
 	}
 	
 	@Test 
 	public void testToString() { 
 		Assert.assertEquals("host: localhost, user: root, port: 3306, dbname: upsilon", db.toString());
 	}
 
 	@Test
 	public void testRemoteService() {
 		StructureRemoteService srs = new StructureRemoteService();
 		Configuration.instance.remoteServices.add(srs); 
 		
 		StructurePeer peer = new StructurePeer(); 
 		Configuration.instance.peers.register(peer);
 		
 	}
 }
