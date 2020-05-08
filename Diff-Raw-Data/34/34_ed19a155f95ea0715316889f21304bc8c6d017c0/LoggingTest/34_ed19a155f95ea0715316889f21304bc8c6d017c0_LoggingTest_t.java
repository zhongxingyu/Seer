 package dk.dtu.imm.distributedsystems.projects.sensornetwork.sink;
 
 import org.junit.Test;
 
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.logging.LoggingUtility;
 
 public class LoggingTest {
 
 	@Test
 	public void testLowLevelLogging() {
 		
 		Object[] parameters = {"rId", "mes", "pac", "value"};
 		
 		for (int i=1; i<10; i++) {
 			LoggingUtility.logMessage(parameters);
 		}
 	}
 	
	// TODO Tests using LogChecker
 
 }
