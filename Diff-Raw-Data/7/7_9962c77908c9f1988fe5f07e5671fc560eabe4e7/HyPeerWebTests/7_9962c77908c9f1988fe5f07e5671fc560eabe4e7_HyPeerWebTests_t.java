 package hypeerweb;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * JUnit Test cases for the HyPeerWeb class
  * @author Jason Robertson
  */
 public class HyPeerWebTests {
 	
 	private HyPeerWeb web;
 	Node nodes[] = new Node[8];
 	
 	@Before
 	public void setup() {
 		web = HyPeerWeb.getSingleton();
 
 		nodes[0] = new Node(0,0);
 		nodes[1] = new Node(1,1);
 		nodes[2] = new Node(2,2);
 		nodes[3] = new Node(3,2);
 		nodes[4] = new Node(4,3);
 		nodes[5] = new Node(5,3);
 		nodes[6] = new Node(6,3);
 		nodes[7] = new Node(7,3);
 		
 		nodes[0].addNeighbor(nodes[1]);
 		nodes[0].addNeighbor(nodes[2]);
 		nodes[0].addNeighbor(nodes[4]);
 		
 		nodes[1].addNeighbor(nodes[0]);
 		nodes[1].addNeighbor(nodes[3]);
 		nodes[1].addNeighbor(nodes[5]);
 		
 		nodes[2].addNeighbor(nodes[0]);
 		nodes[2].addNeighbor(nodes[3]);
 		nodes[2].addNeighbor(nodes[6]);
 		
 		nodes[3].addNeighbor(nodes[1]);
 		nodes[3].addNeighbor(nodes[2]);
 		nodes[3].addNeighbor(nodes[7]);
 		
 		nodes[4].addNeighbor(nodes[0]);
 		nodes[4].addNeighbor(nodes[5]);
 		nodes[4].addNeighbor(nodes[6]);
 		
 		nodes[5].addNeighbor(nodes[1]);
 		nodes[5].addNeighbor(nodes[4]);
 		nodes[5].addNeighbor(nodes[7]);
 		
 		nodes[6].addNeighbor(nodes[4]);
 		nodes[6].addNeighbor(nodes[7]);
 		nodes[6].addNeighbor(nodes[2]);
 		
 		nodes[7].addNeighbor(nodes[3]);
 		nodes[7].addNeighbor(nodes[6]);
 		nodes[7].addNeighbor(nodes[5]);
 	}
 	
 	@After
 	public void tearDown() {
 		web.clear();
 		web = null;
 	}
 	
 	@Test
 	public void testGetSingleton(){
 		assertNotNull(HyPeerWeb.getSingleton());
 	}
 	
 	@Test
 	public void testSize(){
 		assertEquals(0, web.size());
 	}
 	
 	@Test
 	public void testAddNode(){
 		web.addNode(nodes[0]);
 		assertEquals(1, web.size());
 	}
 	
 	@Test
 	public void testGetNode(){
 		
 		web.addNode(nodes[0]);
 		web.addNode(nodes[1]);
 		
 		assertTrue(nodes[0].equals(web.getNode(0)));
 		assertTrue(nodes[1].equals(web.getNode(1)));
 	}
 	
 	@Test
 	public void testClear(){
 		
 		assertEquals(0,web.size());
 		
 		web.addNode(nodes[0]);
 		assertEquals(1,web.size());
 		
 		web.addNode(nodes[1]);
 		assertEquals(2,web.size());
 
 		web.addNode(nodes[2]);
 		assertEquals(3,web.size());
 		
 		web.clear();
 		assertEquals(0,web.size());
 	}
 	
 	@Test
 	public void testContains(){
 
 		web.addNode(nodes[0]);
 		assertTrue(web.contains(nodes[0]));
 	}
 	
 	@Test
 	public void testGetHyPeerWebDatabase(){
 		assertNotNull(web.getHyPeerWebDatabase());
 	}
 	
 	@Test
 	public void testReload(){
 		
 		web.addNode(nodes[0]);
 		web.addNode(nodes[1]);
 		web.addNode(nodes[2]);
 		assertEquals(3,web.size());
 		
		web.saveToDatabase();

 		web.clear();
 		assertEquals(0,web.size());
 		
 		web.reload();
 		assertEquals(3,web.size());
 	}
 	
 	@Test
 	public void testReloadWithName(){
 		
 		final String dbName = "testing.db";
 		HyPeerWebDatabase.initHyPeerWebDatabase(dbName);
 		
 		web.addNode(nodes[0]);
 		web.addNode(nodes[1]);
 		web.addNode(nodes[2]);
 		assertEquals(3,web.size());
 		
 		web.saveToDatabase();
 		
 		web.clear();
 		assertEquals(0,web.size());
 		
 		web.reload(dbName);
 		assertEquals(3,web.size());
 	}
 	
 	@Test
 	public void testRemoveNode(){
 		assertEquals(0,web.size());
 		
 		web.addNode(nodes[0]);
 		assertEquals(1,web.size());
 		
 		web.removeNode(nodes[0]);
 		assertEquals(0,web.size());
 	}
 	
 	@SuppressWarnings("static-access")
 	@Test
 	public void testSaveToDatabase(){
 		web.addNode(nodes[0]);
 		web.addNode(nodes[1]);
 		web.addNode(nodes[2]);
 		
 		web.saveToDatabase();
 		
 		assertEquals(3, web.getHyPeerWebDatabase().getSingleton().getAllWebIds().size());
 	}
 }
