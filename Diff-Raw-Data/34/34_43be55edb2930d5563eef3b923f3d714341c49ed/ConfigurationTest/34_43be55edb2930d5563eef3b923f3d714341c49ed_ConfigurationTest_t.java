 package gears.base;
 
 import static org.junit.Assert.*;
 import gears.base.connection.SSHConnection;
 import gears.base.pkmg.DebianInstaller;
 
 import org.junit.Test;
 
 public class ConfigurationTest {
 	
	private final static String SSH_KEY = "keys/id_rsa";
	
 	@Test
 	public void testAddInstance() {
 		
 		Configuration config = new Configuration();
		Instance instance1 = new Instance("10.211.55.100", SSH_KEY, new SSHConnection(), new DebianInstaller());
 		config.addInstance("web", instance1);
 		
 		assertNotNull(config.getInstances("web"));
 		assertTrue(config.getInstances("web").size() == 1);
 		
		Instance instance2 = new Instance("10.211.55.101", SSH_KEY, new SSHConnection(), new DebianInstaller());
 		config.addInstance("web", instance2);
 		
 		assertNotNull(config.getInstances("web"));
 		assertTrue(config.getInstances("web").size() == 2);
 	}
 	
 	@Test
 	public void testMultipleConnect() {
 		
 		Configuration config = new Configuration();
		Instance instance1 = new Instance("10.211.55.100", SSH_KEY, new SSHConnection(), new DebianInstaller());
 		config.addInstance("web", instance1);
 		
		Instance instance2 = new Instance("10.211.55.101", SSH_KEY, new SSHConnection(), new DebianInstaller());
 		config.addInstance("web", instance2);
 		
 		boolean result = true;
 		for(Instance instance : config.getInstances("web")){
 			result = result && instance.connect();
 		}
 		// Test if connection was successfull
 		assertTrue(result);
 		
 	}
 	
 
 }
