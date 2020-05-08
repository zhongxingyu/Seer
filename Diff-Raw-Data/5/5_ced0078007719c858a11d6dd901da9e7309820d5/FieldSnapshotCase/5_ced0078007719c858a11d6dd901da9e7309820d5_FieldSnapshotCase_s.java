 package edu.wheaton.simulator.test.statistics;
 
 /**
  * A JUnit test case for testing FieldSnapshot.java.
  * 
  * @author Akonwi Ngoh
  * Wheaton College, CSCI 335
  * Spring 2013
  */
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.wheaton.simulator.statistics.FieldSnapshot;
 
 public class FieldSnapshotCase {
 
 	@Before
 	public void setUp() {
 		//TODO FieldSnapshotCase.setUp() is empty
 	}
 
 	@After
 	public void tearDown() {
 		//TODO FieldSnapshotCase.setUp() is empty
 	}
 
 	@Test
 	public void test() {
 		//fail("Not yet implemented");
 	}
 
 	@Test
 	public void fieldSnapshotTest() {
 		FieldSnapshot fieldSnap = new FieldSnapshot("name", "akon");
 		Assert.assertNotNull(fieldSnap);
 		FieldSnapshot fieldSnapWithInt = new FieldSnapshot("akon", "12345");
 		Assert.assertNotNull(fieldSnapWithInt);
 	}
 }
