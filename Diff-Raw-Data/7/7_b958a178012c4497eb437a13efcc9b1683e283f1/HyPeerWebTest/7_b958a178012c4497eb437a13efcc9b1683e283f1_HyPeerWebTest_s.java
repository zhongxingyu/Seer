 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package hypeerweb;
 
 import hypeerweb.visitors.SendVisitor;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 import org.junit.After;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import validator.Validator;
 
 /**
  * HyPeerWeb testing
  */
 public class HyPeerWebTest {
 	//Testing variables
 	private static final int
 		MAX_SIZE = 500,					//Maximum HyPeerWeb size for tests
 		TEST_EVERY = 1,					//How often to validate the HyPeerWeb for add/delete
 		SEND_TESTS = 2000,				//How many times to test send operation
 		RAND_SEED = -1;					//Seed for getting random nodes (use -1 for a random seed)
 	private static final boolean
 		USE_DATABASE = false,			//Enables database syncing
 		USE_GRAPH = false;				//Starts a new thread for drawing the HyPeerWeb
 	private static HyPeerWeb web;
 	private static String curTest;
 	private static boolean hasRestored, hasPopulated;
 	
 	public HyPeerWebTest() throws Exception{
 		web = HyPeerWeb.initialize(USE_DATABASE, USE_GRAPH, RAND_SEED);
 		hasRestored = hasPopulated = false;
 	}
 	
 	/**
 	 * Populates the HyPeerWeb with some nodes
 	 */
 	public void populate() throws Exception{
 		//Restore the database, if we haven't already
 		if (!hasRestored && USE_DATABASE){
 			System.out.println("Restoring...");
 			try{
 				if (!(new Validator(web)).validate())
 					throw new Exception("FATAL ERROR: Could not restore the old database");
 			} catch (Exception e){
 				System.out.println("The database must be corrupt. Did you previously force execution to stop?");
 				System.out.println("Deleting the old database... Rerun the tests two more times to verify it works");
 				Database badDB = Database.getInstance();
 				badDB.clear();
 				throw e;
 			}
 			web.removeAllNodes();
 			hasRestored = true;
 		}
 		//Populate the DB with nodes, if needed
 		//Add a bunch of nodes if it validates afterwards, methods should be working
 		if (!hasPopulated || web.isEmpty()){
 			System.out.println("Populating...");
 			web.removeAllNodes();
 			Node temp;
 			int old_size = 0;
 			for (int i=1; i<=MAX_SIZE; i++){
 				if ((temp = web.addNode()) == null)
 					throw new Exception("Added node should not be null!");
 				if (web.getSize() != ++old_size)
 					throw new Exception("HyPeerWeb is not the correct size");
 				if (i % TEST_EVERY == 0)
 					assertTrue((new Validator(web)).validate());
 			}
 			hasPopulated = true;
 		}
 	}
 	public void begin(String type) throws Exception{
 		curTest = type;
 		populate();
 		System.out.println("BEGIN:\t"+type);
 	}
 	@After
 	public void end(){
 		if (curTest != null){
 			System.out.println("END:\t"+curTest);
 			curTest = null;
 		}
 	}
 	
 	/**
 	 * Test of addNode method
 	 */
 	@Test
 	public void testAdd() throws Exception {
 		//This is a dummy method for populate()
 		//Don't remove this method
 	}
 	
 	/**
 	 * Test of removeNode method (from zero, every time)
 	 */
 	@Test
 	public void testRemoveZero() throws Exception {
 		begin("REMOVING ZERO");
 		Node temp;
 		int old_size = web.getSize();
 		assert(old_size == MAX_SIZE);
 		for (int i=1; i<=MAX_SIZE; i++){
 			if ((temp = web.removeNode(web.getFirstNode())) == null)
 				throw new Exception("Removed node should not be null!");
 			if (web.getSize() != --old_size)
 				throw new Exception("HyPeerWeb is not the correct size");
 			if (i % TEST_EVERY == 0)
 				assertTrue((new Validator(web)).validate());
 		}
 	}
 	
 	/**
 	 * Test of removeNode method (picking randomly)
 	 */
 	@Test
 	public void testRemoveRandom() throws Exception {
 		begin("REMOVING RANDOM");
 		Node temp, rand;
 		int old_size = web.getSize();
 		for (int i=1; i<=MAX_SIZE; i++){
 			rand = web.getRandomNode();
 			if ((temp = web.removeNode(rand)) == null)
 				throw new Exception("Removed node should not be null!");
 			if (web.getSize() != --old_size)
 				throw new Exception("HyPeerWeb is not the correct size");
 			if (i % TEST_EVERY == 0)
 				assertTrue((new Validator(web)).validate());
 		}
 	}
 	
 	/**
 	 * Test of send visitor with valid target node
 	 */
 	@Test
 	public void testSendValid() throws Exception {
 		//Test send node
 		begin("SENDING VALID");
 		Node f1, f2, found;
 		for (int j=0; j<SEND_TESTS; j++){
 			f1 = web.getRandomNode();
 			do{
 				f2 = web.getRandomNode();
 			} while (f2 == f1);
 			SendVisitor x = new SendVisitor(f1.getWebId());
 			x.visit(f2);
 			found = x.getFinalNode();
 			if (found == null){
 				System.out.println("f1 = " + f1);
 				System.out.println("f2 = " + f2);
 			}
 			assertNotNull(found);
 			assert(found.getWebId() == f1.getWebId());
 		}
 	}
 	
 	/**
 	 * Test of send visitor with invalid target node
 	 */
 	@Test
 	public void testSendInvalid() throws Exception {
 		begin("SENDING INVALID");
 		Random r = new Random();
 		for (int i=0; i<SEND_TESTS; i++){
 			int bad_id = r.nextInt();
 			while (web.getNode(bad_id) != null)
 				bad_id *= 3;
 			SendVisitor x = new SendVisitor(bad_id);
 			x.visit(web.getFirstNode());
 			assertNull(x.getFinalNode());
 		}
 	}
 	
 	@Test
 	public void testBroadcast() throws Exception {
 		begin("TESTING BROADCAST");
 		ListNodesVisitor x = new ListNodesVisitor();
 		x.visit(web.getRandomNode());
 		if(x.getNodeList().size() < web.getSize()) {
 			for(Node n : web.getOrderedListOfNodes()) {
 				if(!x.getNodeList().contains(n)){
 					System.out.println("Missing: " + n);
 				}
 			}
 		}
 		assertTrue(x.getNodeList().size() == web.getSize());
 		for(Node n : x.getNodeList()) {
 			assertTrue(web.getNode(n.getWebId()) != null);
 		}
 		Set<Node> set = new HashSet<>(x.getNodeList());
 		assertTrue(set.size() == x.getNodeList().size());
 	}
 	
 	@Test
     public void whiteBoxIsaac(){
         //TESTS FOR METHOD: getCloserNode()
         /* Internal boundary value test for (target < 0)
          * target = 0 (equal)
          * target = 1 (slightly larger)
          * target = -1 (slightly smaller)
          */
         
         /* Loop testing for L.getAllLinks()
          * Node has no links (skip loop entirely)
          * one link (with one loop pass)
          * two links (with two loop passes)
          * > two links (with an arbitrary number of passes)
          */
         
         /* Relational testing for
          *
          */
         
         /*
         //Trying to find a negative node is a waste of time
         if (target < 0) return null;
         //Try to find a link with a webid that is closer to the target
         int base = this.scoreWebIdMatch(target);
         for (Node n: L.getAllLinks()){
             if (n.scoreWebIdMatch(target) > base)
                 return n;
         }
         //If none are closer, get a SNeighbor
         if (!mustBeCloser){
             for (Node sn: L.getSurrogateNeighborsSet()){
                 if (sn != null && sn.scoreWebIdMatch(target) == base)
                     return sn;
             }
         }
         //Otherwise, that node doesn't exist
         return null;
 		*/
     }
 
 }
