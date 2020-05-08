 package edu.wheaton.simulator.test.statistics;
 
 /**
  * A JUnit test case for testing StatisticsManager.java.
  * 
  * @author Grant Hensel
  * Wheaton College, CSCI 335
  * Spring 2013
  * 25 Mar 2013
  */
 
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableSet;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.AgentID;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.statistics.AgentSnapshot;
 import edu.wheaton.simulator.statistics.PrototypeSnapshot;
 import edu.wheaton.simulator.statistics.SnapshotFactory;
 import edu.wheaton.simulator.statistics.StatisticsManager;
 
 
 public class StatisticsManagerTest {
 
 	StatisticsManager sm;
 	Grid g; 
 	String categoryName;
 	Grid grid;
 	Prototype prototype;
 	HashMap<String, String> fields;
 	int population;
 	ImmutableSet<AgentID> children;
 	Integer step;
 	PrototypeSnapshot protoSnap;
 	PrototypeSnapshot protoSnap2;
 	AgentSnapshot aSnap;
 	
 	@Before
 	public void setUp() {
 		sm = new StatisticsManager();
 		g = new Grid(10,  10); 		
 		
 		//Add a test PrototypeSnapshot
 		categoryName = "testing";
 		grid = new Grid(10, 10);
 		prototype = new Prototype(grid, "tester");
 		fields = new HashMap<String, String>();
 		population = 50;
 		children = prototype.childIDs();
 		step = new Integer(1);
 		
 		protoSnap = new PrototypeSnapshot(categoryName, prototype.getPrototypeID(),
 				SnapshotFactory.makeFieldSnapshots(fields), population,
 				children, step);
 		
 	
 		
 		//Add another test PrototypeSnapshot
 		categoryName = "testing2";
 		prototype = new Prototype(grid, "tester2");
 		population = 40;
 		step = new Integer(2);
 		
 		protoSnap2 = new PrototypeSnapshot(categoryName, prototype.getPrototypeID(),
 				SnapshotFactory.makeFieldSnapshots(fields), population,
 				children, step);
 		
 		Agent a = prototype.createAgent();
 		aSnap = SnapshotFactory.makeAgentSnapshot(a, step);
 		
 	}
 
 	@After
 	public void tearDown() {
 		//Nothing to do here
 	}
 
 	@Test
 	public void testStatisticsManager() { 
 		Assert.assertNotNull("Constructor failed", sm);
 	}
 
 	@Test
 	public void testGetGridObserver() {
 		Assert.assertNotNull("Failed to get GridObserver", sm.getGridObserver()); 
 	}
 
 	@Test
 	public void testAddPrototypeSnapshot() {
 		Prototype p = new Prototype(g, "TestPrototype"); 
 		Assert.assertNotNull(p); 
 		
 		PrototypeSnapshot protoSnap = new PrototypeSnapshot("categoryname", p.getPrototypeID(),
 				SnapshotFactory.makeFieldSnapshots(new HashMap<String, String>()), 100, p.childIDs(), new Integer(2)); 
 		
 		sm.addPrototypeSnapshot(protoSnap);
 	}
 
 	@Test
 	public void testAddGridEntity() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetPrototypeIDs() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetPopVsTime() {
 		sm.addPrototypeSnapshot(protoSnap); 
 		sm.addGridEntity(aSnap);
 		
 		int[] result = sm.getPopVsTime(protoSnap.id);
 		System.out.println(result[0] + " " + result[1]); 
 		int[] expected = {1}; 
 		Assert.assertArrayEquals(expected, result); 
 	}
 
 	@Test
 	public void testGetAvgFieldValue() {
 		ArrayList<AgentSnapshot> snaps = new ArrayList<AgentSnapshot>();
 		HashSet<AgentID> ids = new HashSet<AgentID>();
 		String[] names = new String[] {"bear", "tom", "john", "piglet", "reese"};
 		
 		/* create snapshots */
 		for(int i = 0; i < 5; i++) {
 			Agent agent = prototype.createAgent();
 			try {
 				agent.addField("name", names[i]);
 				agent.addField("weight", "10");
 			} catch (ElementAlreadyContainedException e) {
 				e.printStackTrace();
 			}
 			ids.add(agent.getID());
 			for(int s = 1; s < 3; s++) {
 				snaps.add(new AgentSnapshot(agent.getID(), SnapshotFactory.makeFieldSnapshots(agent.getCustomFieldMap()), s, protoSnap.id));
 			}
 		}
 		
 		/* fill table w/ snapshots */
 		for(AgentSnapshot snap: snaps) {
 			sm.addGridEntity(snap);
 		}
 		
 		/* test method */
 		double[] avg = sm.getAvgFieldValue(protoSnap.id, "weight");
 		for(double i : avg) {
 			int a = (int) i;
 			org.junit.Assert.assertEquals(10, a);
 		}
 	}
 
 	@Test
 	public void testGetAvgLifespan() {
 		sm.addPrototypeSnapshot(protoSnap); 
 		
 		try {
 			sm.getAvgLifespan(protoSnap.id);
 		}
 		catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} 
 	}
 
 }
