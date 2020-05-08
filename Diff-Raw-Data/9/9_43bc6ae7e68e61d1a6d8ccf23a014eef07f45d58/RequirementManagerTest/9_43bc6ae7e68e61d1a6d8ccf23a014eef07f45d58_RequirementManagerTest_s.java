 
 package edu.wpi.cs.wpisuitetng.modules.RequirementManager.models;
 
 import static org.junit.Assert.*;
 
 import java.util.HashSet;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.wpi.cs.wpisuitetng.Session;
 import edu.wpi.cs.wpisuitetng.exceptions.NotFoundException;
 import edu.wpi.cs.wpisuitetng.exceptions.UnauthorizedException;
 import edu.wpi.cs.wpisuitetng.exceptions.WPISuiteException;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.MockData;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.RequirementEntityManager;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.RequirementPriority;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.core.models.Project;
 import edu.wpi.cs.wpisuitetng.modules.core.models.Role;
 import edu.wpi.cs.wpisuitetng.modules.core.models.User;
 
 /**
  * Testing of the RequirementEntityManager
  * @author Dylan
  *
  */
 public class RequirementManagerTest {
 
 	MockData db;
 	User existingUser;
 	Requirement req1;
 	Session defaultSession;
 	String mockSsid;
 	RequirementEntityManager manager;
 	Requirement req3;
 	User bob;
 	Requirement goodUpdatedRequirement;
 	Session adminSession;
 	Project testProject;
 	Project otherProject;
 	Requirement req2;
 	
 	/**
 	 * Set up objects and create a mock session for testing
 	 */
 	@Before
 	public void setUp() throws Exception {
 		User admin = new User("admin", "admin", "1234", 27);
 		admin.setRole(Role.ADMIN);
 		testProject = new Project("test", "1");
 		otherProject = new Project("other", "2");
 		mockSsid = "abc123";
 		adminSession = new Session(admin, testProject, mockSsid);
 		
 		existingUser = new User("joe", "joe", "1234", 2);
 		req1 = new Requirement(1, "Bob", "1.0", RequirementStatus.NEW, RequirementPriority.BLANK, "Desc", 1, 1);
 		
 		req2 = new Requirement(2, "Joe", "2.0", RequirementStatus.NEW, RequirementPriority.LOW, "Description", 2, 2);
 				
 		defaultSession = new Session(existingUser, testProject, mockSsid);
 		req3 = new Requirement(3, "Jim", "3.0", RequirementStatus.NEW, RequirementPriority.HIGH, "Desc", 1, 2);
 		
 		db = new MockData(new HashSet<Object>());
 		db.save(req1, testProject);
 		db.save(existingUser);
 		db.save(req2, otherProject);
 		db.save(admin);
 		manager = new RequirementEntityManager(db);
 	}
 
 	/**
 	 * Stores a new requirement and ensures the correct data was stored
 	 */
 	@Test
 	public void testMakeEntity() throws WPISuiteException {
 		Requirement created = manager.makeEntity(defaultSession, req3.toJSON());
 		assertEquals(3, created.getId()); // IDs are unique across projects
 		assertEquals("Jim", created.getName());
 		assertEquals("3.0", created.getRelease());
 		assertEquals(RequirementStatus.NEW, created.getStatus());
 		assertEquals("Desc", created.getDescription());
 		assertEquals(1, created.getEstimate());
 		assertEquals(2, created.getEffort());
 		assertSame(db.retrieve(Requirement.class, "id", 3).get(0), created);
 	}
 	
 	/**
 	 * Ensures a requirement can be retrieved from the database
 	 * @throws NotFoundException
 	 */
 	@Test
 	public void testGetEntity() throws NotFoundException {
 		Requirement[] gotten = manager.getEntity(defaultSession, "1");
 		assertSame(req1, gotten[0]);
 	}
 
 	/**
 	 * Ensures a NotFoundException is thrown when trying to
 	 * retrieve an invalid requirement
 	 */
 	@Test(expected=NotFoundException.class)
 	public void testGetBadId() throws NotFoundException {
 		manager.getEntity(defaultSession, "-1");
 	}
 
 	/**
 	 * Ensures that requirements can be deleted
 	 */
 	@Test
 	public void testDelete() throws WPISuiteException {
 		assertSame(req1, db.retrieve(Requirement.class, "id", 1).get(0));
 		assertTrue(manager.deleteEntity(adminSession, "1"));
 		assertEquals(0, db.retrieve(Requirement.class, "id", 1).size());
 	}
 	
 	/**
 	 * Ensures a NotFoundException is thrown when trying to delete
 	 * an invalid requirement
 	 */
 	@Test(expected=NotFoundException.class)
 	public void testDeleteMissing() throws WPISuiteException {
 		manager.deleteEntity(adminSession, "4534");
 	}
 	
 	/**
 	 * Ensures an UnauthorizedException is thrown when trying
 	 * to delete an entity while not authorized
 	 * @throws WPISuiteException
 	 */
 	@Test(expected=UnauthorizedException.class)
 	public void testDeleteNotAllowed() throws WPISuiteException {
 		manager.deleteEntity(defaultSession, Integer.toString(req1.getId()));
 	}
 	
 	/**
 	 * Ensures the deletion of all requirements funtions properly
 	 * @throws WPISuiteException
 	 */
 	@Test
 	public void testDeleteAll() throws WPISuiteException {
 		Requirement anotherRequirement = new Requirement();
 		manager.makeEntity(defaultSession, anotherRequirement.toJSON());
 		assertEquals(2, db.retrieveAll(new Requirement(), testProject).size());
 		manager.deleteAll(adminSession);
 		assertEquals(0, db.retrieveAll(new Requirement(), testProject).size());
 		// otherRequirement should still be around
 		assertEquals(1, db.retrieveAll(new Requirement(), otherProject).size());
 	}
 
 	@Test
 	public void testDeleteAllWhenEmpty() throws WPISuiteException {
 		manager.deleteAll(adminSession);
 		manager.deleteAll(adminSession);
 		// no exceptions
 	}
 	
 	@Test
 	public void testCount() throws WPISuiteException {
 		assertEquals(2, manager.Count());
 	}
}
 
