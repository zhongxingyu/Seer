 package scRT.tracker.test;
 
 import static org.junit.Assert.*;
 
 import java.util.Iterator;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import scRT.tracker.ConfigurationAction;
 import scRT.tracker.ConfigurationRequirement;
 import scRT.tracker.ConfigurationValue;
 
 public class ConfigurationRequirementTest {
 	private ConfigurationRequirement cr;
 	
 	
 	@Before
 	public void setUp() throws Exception {
 
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testConfigurationRequirement() {
 		assert(true);
 	}
 
 	@Test
 	public void testAddCV() {
 		assert(true);
 	}
 
 	@Test
 	public void testGetCVSet() {
 		assert(true);
 	}
 
 	@Test
 	public void testGetCASet() {
		cr = new ConfigurationRequirement();
 		
 		Iterator<ConfigurationValue> iii = cr.getCVSet().iterator();
 		assertTrue(iii.hasNext());
 		assertEquals("cv1",iii.next().getValue());
 	}
 
 }
