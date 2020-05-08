 package edu.wheaton.simulator.test.statistics;
 
 import java.util.HashMap;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.statistics.AgentSnapshot;
 import edu.wheaton.simulator.statistics.AgentSnapshotTable;
 import edu.wheaton.simulator.statistics.PrototypeSnapshot;
 import edu.wheaton.simulator.statistics.Saver;
 import edu.wheaton.simulator.statistics.SnapshotFactory;
 
 public class SaverTest {
 
 	String s;
 	Agent agent;
 	Grid grid;
 	Prototype prototype;
 	Integer step;
 	
 	@Before
	public void setUp() throws Exception {
 		grid = new Grid(10, 10);
 		prototype = new Prototype(grid, "tester");
 		agent = prototype.createAgent();
 		try {
 			agent.addField("Pig", "Tom");
 			agent.addField("Monkey", "Olly");
 			agent.addField("Cat", "Joomba");
 		} catch (ElementAlreadyContainedException e) {
 			e.printStackTrace();
 		}
 		step = new Integer(23);
 	}
 
 	@After
	public void tearDown() throws Exception {
 		agent = null;
 		grid = null;
 		prototype = null;
 		step = null;
 	}
 
 	@Test
 	public void testSave() {
 		//Create two AgentSnapshots
 		AgentSnapshot agentSnap1 = new AgentSnapshot(agent.getID(), 
 				SnapshotFactory.makeFieldSnapshots(agent.getCustomFieldMap()), 
 				step, prototype.getName(), null, 0, 0);  
 		
 		AgentSnapshot agentSnap2 = new AgentSnapshot(prototype.createAgent().getID(), 
 				SnapshotFactory.makeFieldSnapshots(agent.getCustomFieldMap()), 
 				step, prototype.getName(), null, 0, 0);  
 		
 		//Create the table, add two AgentSnapshots
 		AgentSnapshotTable table = new AgentSnapshotTable();
 		table.putEntity(agentSnap1); 
 		table.putEntity(agentSnap2); 
 		
 		Saver s = new Saver(table, new HashMap<String, PrototypeSnapshot>());
 		s.save();
 	}
 }
