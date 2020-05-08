 import static org.junit.Assert.*;
 
 import com.google.appengine.api.images.Image;
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import theatreProject.server.PersistenceImpl;
 import theatreProject.shared.Persistence;
 import theatreProject.domain.shared.Status;
 import theatreProject.server.PersistenceImpl.User;
 import theatreProject.server.PersistenceImpl.InventoryObject;
 
 import org.junit.Test;
 import org.junit.Before;
 import org.junit.After;
 
 
 public class PersistenceTest {
 
 	private final LocalServiceTestHelper helper =
 			new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
 
 	@Before
 	public void setUp() {
 		helper.setUp();
 	}
 
 	@After
 	public void tearDown() {
 		helper.tearDown();
 	}
 
 	@Test
 	public void testUser() {
 		Persistence p = new PersistenceImpl();
		User u = new User("test@somewhere.com","J Doe","something",true);
 		User u2 = p.getUser(u.getEmail());
 		assertEquals(u, u2);
 	}
 
 	
 	 //============================================\\
 	//PersistenceImpl need changing for this to work\\
 //	@Test
 //	public void testObject() {
 //		Persistence p = new PersistenceImpl();
 //		//Need to change SavedObject to object in PersistenceImpl
 //		InventoryObject o = new InventoryObject(String ID, String name, String storageArea, Image image, Status status, String description, String disclaimers);
 //		p.saveInventoryObject(o);
 //		InventoryObject o2 = p.getInventoryObject(o.getID);
 //		assertEquals(o,o2);
 //	}
 //	
 //	public void testObjectSearch() {
 //		Persistence p = new PersistenceImpl();
 //	}
 	
 }
