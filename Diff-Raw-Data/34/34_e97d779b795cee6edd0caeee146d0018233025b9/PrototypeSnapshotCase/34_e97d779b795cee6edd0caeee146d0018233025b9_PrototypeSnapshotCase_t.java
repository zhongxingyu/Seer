 package edu.wheaton.simulator.test.statistics;
 
 /**
  * A JUnit test case for testing PrototypeSnapshot.java.
  * 
  * @author Nico Lasta
  * Wheaton College, CSCI 335
  * Spring 2013
  * 25 Mar 2013
  */
 import java.awt.Color;
 import java.util.HashMap;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableSet;
 
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.AgentID;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.statistics.PrototypeSnapshot;
 import edu.wheaton.simulator.statistics.SnapshotFactory;
 
 public class PrototypeSnapshotCase {
 	
 	String categoryName;
 	Grid grid;
 	Prototype prototype;
 	HashMap<String, String> fields;
 	int population;
 	ImmutableSet<AgentID> children;
 	Integer step; 
 	byte[] design; 
 
 	/**
 	 * Initialize variables.
 	 */
 	@Before
 	public void setUp() {
 		categoryName = "testing";
 		grid = new Grid(10, 10);
 		prototype = new Prototype(grid, "tester");
 		fields = new HashMap<String, String>();
 		fields.put("Age", "1"); 
 		population = 50;
 		children = prototype.childIDs();
 		step = new Integer(23);
 		design = new byte[10]; 
 	}
 	
 	/**
 	 * Tests to make sure a PrototypeSnapshot object was successfully created.
 	 */
 	@Test
 	public void prototypeSnapshotTest() {		
 		PrototypeSnapshot protoSnap = new PrototypeSnapshot(categoryName, 
 				SnapshotFactory.makeFieldSnapshots(fields), population,
 				children, step, new Color(10, 10, 10), design);
 		Assert.assertNotNull("PrototypeSnapshot not created.", protoSnap);
 	}
 	
 	/**
 	 * Tests the serialize() method 
 	 */
 	@Test
 	public void serializeTest(){
 		PrototypeSnapshot protoSnap = new PrototypeSnapshot(categoryName, 
 				SnapshotFactory.makeFieldSnapshots(fields), population,
 				children, step, new Color(10, 10, 10), design);
 		
 		String expected = "PrototypeSnapshot\ntesting\n-16119286\n0000000000\nFieldSnapshot Age 1"; 
 		System.out.println(protoSnap.serialize()); 
 		Assert.assertEquals(expected, protoSnap.serialize()); 	
 	}
 }
