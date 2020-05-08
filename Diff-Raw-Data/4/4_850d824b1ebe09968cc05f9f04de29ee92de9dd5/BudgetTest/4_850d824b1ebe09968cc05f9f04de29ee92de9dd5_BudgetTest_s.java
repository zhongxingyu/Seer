 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package badm.models;
 
 import badm.BridgeHelper;
 import badm.Budget;
 import badm.Note;
 import cc.test.bridge.BridgeConstants.Side;
 import cc.test.bridge.BudgetInterface;
 import cc.test.bridge.LineInterface;
 import cc.test.bridge.NoteInterface;
 import java.util.ArrayList;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import org.junit.*;
 import org.workplicity.task.NetTask;
 import org.workplicity.util.Helper;
 import org.workplicity.util.WorkDate;
 import org.workplicity.worklet.WorkletContext;
 
 /**
  *
  * @author idontknow5691
  */
 public class BudgetTest {
 	
 	/**
 	 * The store or mongo db name
 	 */
 	public final static String STORE_NAME = "badm";
     
 	/**
 	 * The scratch pad for Helper
 	 */
 	private static WorkletContext context = WorkletContext.getInstance();
 	
 	private static Budget budget;
 	
 	public BudgetTest() {
 	}
 
 	@BeforeClass
 	public static void setUpClass() throws Exception {
 		try { 
 			// Set the store name since the default may be be not ours
                         NetTask.setStoreName(STORE_NAME);
                         NetTask.setUrlBase("http://localhost:8080/netprevayle/task");
 
                         // Attempt the login
                         if(!Helper.login("admin", "gazelle", context))
 				fail("login failed");
 
 		} catch (Exception e) {
 			fail("failed with exception: " + e);
                 }
 		
 		budget = (Budget) BridgeHelper.getBudgetFactory().create();
 		
 		budget.setDescription("My Budget");
 		
 	}
 
 	@AfterClass
 	public static void tearDownClass() throws Exception {
 		Helper.logout(Helper.whoAmI(context), context);
 	}
 	
 	@Before
 	public void setUp() {
 		
 	}
 	
 	@After
 	public void tearDown() {
 	}
 
 	@Test
 	public void insert()
 	{
                 System.out.println("insert");
 		//boolean successful = Helper.insert(budget, budget.getRepositoryName(), context);
 		try{
                 Boolean insert = budget.commit();
 		}catch(Exception e){
 			fail("Hey inserting a budget didn't work"+e);
 		}
 		System.out.println("after failed insert");
                 System.out.println(Helper.getTicket(context));
 	}
 	
 	@Test
 	public void fetch() {
 		try {
                     System.out.println("fetch");
                     System.out.println(Helper.getTicket(context));
 
                     String repos = budget.getRepositoryName();
 
                     System.out.println(Helper.getTicket(context));
 
                    BudgetInterface fetched_budget = 
                            (Budget) Helper.fetch(repos, budget.getId(), WorkletContext.getInstance());
 
                     if(fetched_budget == null) {
 			    fail("budget not found");
 		    }
 		    
                     System.out.println("fetched budget description = '"+fetched_budget.getDescription()+"'");
 
                     // Verify that the name is what we expect
 		    assertEquals(fetched_budget.getDescription(), budget.getDescription());
 
 		} catch (Exception e) {
 			fail("failed with exception: " + e);
 		}
 	}
 	
 	/**
 	 * Test of getDescription method, of class Budget.
 	 */
 	@Test
 	public void testGetDescription() {
 		System.out.println("getDescription");
 		Budget instance = new Budget();
 		String expResult = "this is a description";
 		instance.setDescription(expResult);
 		String result = instance.getDescription();
 		assertEquals(expResult, result);
 	}
 
 	/**
 	 * Test of setDescription method, of class Budget.
 	 */
 	@Test
 	public void testSetDescription() {
 		System.out.println("setDescription");
 		String d = "herpidy derp";
 		Budget instance = new Budget();
 		instance.setDescription(d);
 		assertEquals(d, instance.getDescription());
 	}
 
 	/**
 	 * Test of fetchLines method, of class Budget.
 	 */
 	@Test
 	public void testFetchLines() {
 		System.out.println("fetchLines");
 		Side side = null;
 		Budget instance = new Budget();
 		ArrayList expResult = null;
 		ArrayList result = instance.fetchLines(side);
 		assertEquals(expResult, result);
 		// TODO better test
 	}
 
 	/**
 	 * Test of fetchNotes method, of class Budget.
 	 */
 	@Test
 	public void testFetchNotes() {
 		System.out.println("fetchNotes");
 		Budget instance = new Budget();
 		ArrayList expResult = null;
 		ArrayList result = instance.fetchNotes();
 		assertEquals(expResult, result);
 		// TODO better test
 	}
 
 	/**
 	 * Test of createLine method, of class Budget.
 	 */
 	@Test
 	public void testCreateLine() {
 		System.out.println("createLine");
 		Budget instance = new Budget();
 		LineInterface expResult = null;
 		LineInterface result = instance.createLine();
 		assertEquals(expResult, result);
 		// TODO better test
 	}
 
 	/**
 	 * Test of createNote method, of class Budget.
 	 */
 	@Test
 	public void testCreateNote() {
 		System.out.println("createNote");
 		Budget instance = new Budget();
 		NoteInterface expResult = null;
 		NoteInterface result = instance.createNote();
 		assertEquals(expResult, result);
 		// TODO better test
 	}
 
 	/**
 	 * Test of add method, of class Budget.
 	 */
 	@Test
 	public void testAdd_NoteInterface() {
 		System.out.println("add");
 		NoteInterface ni = new Note();
 		Budget instance = new Budget();
                 instance.setId(34);
 		instance.add(ni);
                 Note note = (Note)ni;
                 System.out.println("The notes id is:(drumroll)"+note.getBudgetId());
 		
 		try {
 			assertEquals(true, note.commit());
 		} catch (Exception e) {
 			fail("failed with exception: " + e);
 		}
 	}
 
 	/**
 	 * Test of delete method, of class Budget.
 	 */
 	@Test
 	public void testDelete_NoteInterface() {
 		System.out.println("delete");
 		NoteInterface ni = null;
 		Budget instance = new Budget();
 		instance.delete(ni);
 		// TODO better test
 	}
 
 	/**
 	 * Test of add method, of class Budget.
 	 */
 	@Test
 	public void testAdd_LineInterface() {
 		System.out.println("add");
 		LineInterface li = null;
 		Budget instance = new Budget();
 		instance.add(li);
 		// TODO better test
 	}
 
 	/**
 	 * Test of delete method, of class Budget.
 	 */
 	@Test
 	public void testDelete_LineInterface() {
 		System.out.println("delete");
 		LineInterface li = null;
 		Budget instance = new Budget();
 		instance.delete(li);
 		// TODO better test
 	}
 
 	/**
 	 * Test of update method, of class Budget.
 	 */
 	@Test
 	public void testUpdate_LineInterface() {
 		System.out.println("update");
 		LineInterface li = null;
 		Budget instance = new Budget();
 		instance.update(li);
 		// TODO better test
 	}
 
 	/**
 	 * Test of update method, of class Budget.
 	 */
 	@Test
 	public void testUpdate_NoteInterface() {
 		System.out.println("update");
 		NoteInterface ni = null;
 		Budget instance = new Budget();
 		instance.update(ni);
 		// TODO better test
 	}
 
 	/**
 	 * Test of getId method, of class Budget.
 	 */
 	@Test
 	public void testGetId() {
 		System.out.println("getId");
 		Budget instance = new Budget();
 		Integer expResult = -1;
 		Integer result = instance.getId();
 		assertEquals(expResult, result);
 	}
 
 	/**
 	 * Test of setName method, of class Budget.
 	 */
 	@Test
 	public void testSetName() {
 		System.out.println("setName");
 		String string = "Testing";
 		Budget instance = new Budget();
 		instance.setName(string);
 		assertEquals(instance.getName(), string);
 	}
 
 	/**
 	 * Test of getName method, of class Budget.
 	 */
 	@Test
 	public void testGetName() {
 		System.out.println("getName");
 		Budget instance = new Budget();
 		String expResult = "Name";
 		instance.setName(expResult);
 		String result = instance.getName();
 		assertEquals(expResult, result);
 	}
 
 	/**
 	 * Test of getUpdateDate method, of class Budget.
 	 */
 	@Test
 	public void testGetUpdateDate() {
 		System.out.println("getUpdateDate");
 		Budget instance = new Budget();
 		WorkDate expResult = null;
 		WorkDate result = instance.getUpdateDate();
 		assertEquals(expResult, result);
 		// TODO better test
 	}
 
 	/**
 	 * Test of commit method, of class Budget.
 	 */
 	@Test
 	public void testCommit() {
 		System.out.println("commit");
 		Budget instance = new Budget();
 		Boolean expResult = false;
 		try{
 		Boolean result = instance.commit();
 		}catch(Exception e){
 			fail("Commit failed"+e);
 		}
 		// TODO better test
 	}
 
 	/**
 	 * Test of getRepositoryName method, of class Budget.
 	 */
 	@Test
 	public void testGetRepositoryName() {
 		System.out.println("getRepositoryName");
 		Budget instance = new Budget();
 		String expResult = "Budgets";
 		String result = instance.getRepositoryName();
 		assertEquals(expResult, result);
 	}
 }
