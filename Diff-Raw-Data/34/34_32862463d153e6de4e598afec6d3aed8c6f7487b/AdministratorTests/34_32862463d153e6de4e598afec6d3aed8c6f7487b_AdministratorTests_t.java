 package test;
 
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

 import java.util.ArrayList;
 
 import main.AccessTracker;
 import main.Machine;
import main.SystemAdministrator;
 import main.Tool;
 import main.User;
 
 import org.junit.Test;
 
 public class AdministratorTests {
 
 	@Test
 	public void addPermissionTest() {
 		SystemAdministrator testAdmin = new SystemAdministrator("", "", 1);
 		User testUser = new User ("", "", 2);
 		Machine testMachine = new Machine("TPDD", "T-1");
 		testAdmin.addPermission(testUser, testMachine);
 		
 		assertTrue(testUser.getCertifiedMachines().contains(testMachine));
 	}
 	
 	@Test
 	public void removeUsersTest() {
 		AccessTracker tracker = new AccessTracker();
 		SystemAdministrator testAdmin = new SystemAdministrator("", "", 1);
 		ArrayList<User> testUsers = new ArrayList<User>();
 		User testUser = new User ("", "", 2);
 		testUsers.add(testUser);
 		tracker.createUser("", "", 2);
 		testAdmin.removeUsers(testUsers);
 		
 		assertFalse(tracker.getCurrentUsers().contains(testUser));
 	}
 	
 	@Test
 	public void removePermissionTest() {
 		SystemAdministrator testAdmin = new SystemAdministrator("", "", 1);
 		User testUser = new User ("", "", 2);
 		Machine testMachine = new Machine("TPDD", "T-1");
 		testAdmin.addPermission(testUser, testMachine);
 		testAdmin.removePermission(testUser, testMachine);
 		
 		assertFalse(testUser.getCertifiedMachines().contains(testMachine));
 	}
 	
 	@Test
 	public void generateReportTest() {
 		fail("Not yet implemented");
 	}
 	
 	@Test
 	public void addToolTest() {
 		AccessTracker tracker = new AccessTracker();
 		SystemAdministrator testAdmin = new SystemAdministrator("", "", 1);
 		Tool testTool = new Tool("HITCOO", 15);
 		testAdmin.addTool(testTool);
 		
 		assertTrue(tracker.getTools().contains(testTool));
 	}
 	
 	@Test
 	public void addMachineTest() {
 		AccessTracker tracker = new AccessTracker();
 		SystemAdministrator testAdmin = new SystemAdministrator("", "", 1);
 		Machine testMachine = new Machine("TPDD", "T-1");
 		testAdmin.addMachine(testMachine);
 		
 		assertTrue(tracker.getMachines().contains(testMachine));
 	}
 	
 	@Test
 	public void removeToolTest() {
 		AccessTracker tracker = new AccessTracker();
 		SystemAdministrator testAdmin = new SystemAdministrator("", "", 1);
 		Tool testTool = new Tool("HITCOO", 15);
 		testAdmin.addTool(testTool);
 		testAdmin.removeTool(testTool);
 		
 		assertFalse(tracker.getTools().contains(testTool));
 	}
 	
 	@Test
 	public void removeMachineTest() {
 		AccessTracker tracker = new AccessTracker();
 		SystemAdministrator testAdmin = new SystemAdministrator("", "", 1);
 		Machine testMachine = new Machine("TPDD", "T-1");
 		testAdmin.addMachine(testMachine);
 		testAdmin.removeMachine(testMachine);
 		
 		assertFalse(tracker.getMachines().contains(testMachine));
 	}
 	
 	@Test
 	public void lockUserTest() {
 		SystemAdministrator testAdmin = new SystemAdministrator("", "", 1);
 		User testUser = new User ("", "", 2);
 		testAdmin.lockUser(testUser);
 		
 		assertTrue(testUser.isLocked());
 	}
 	
 	@Test
 	public void unlockUserTest() {
 		SystemAdministrator testAdmin = new SystemAdministrator("", "", 1);
 		User testUser = new User ("", "", 2);
 		testAdmin.lockUser(testUser);
 		testAdmin.unlockUser(testUser);
 		
 		assertFalse(testUser.isLocked());
 	}
 
 }
