 package jcommon.init;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import jcommon.Sys;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author David Hoyt <dhoyt@hoytsoft.org>
  */
 public class SystemLoaderTest {
 
 	public SystemLoaderTest() {
 	}
 
 	@BeforeClass
 	public static void setUpClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() {
 	}
 
 	@After
 	public void tearDown() {
 	}
 
 	@Test
 	public void testSystemLoader() {
 		assertTrue(Sys.initializeSystem());
 	}
 }
