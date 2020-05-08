 package models;
 
 import models.*;
 import org.junit.*;
 import static org.junit.Assert.*;
 import play.test.WithApplication;
 import static play.test.Helpers.*;
 import java.util.List;
 
 public class ModelsTest extends WithApplication {
 	@Before
 	public void setUp() {
 		start(fakeApplication(inMemoryDatabase()));
 	}
 
 	@Test
 	public void createAndRetrieveUser() {
 		new User("bob@gmail.com", "Bob", "secret").save();
 		User bob = User.find.where().eq("email", "bob@gmail.com").findUnique();
 		assertNotNull(bob);
 		assertEquals("Bob",bob.name);
 	}
 
 	@Test
 	public void tryAuthenticateUser() {
 		new User("bob@gmail.com","Bob","secret").save();
 		assertNotNull(User.authenticate("bob@gmail.com", "secret"));
 		assertNull(User.authenticate("bob@gmail.com", "badpassword"));
 		assertNull(User.authenticate("tom@gmail.com", "secret"));
 	}
 
 	@Test
 	public void findProjectsInvolving() {
 		new User("bob@gmail.com", "Bob", "secret").save();
 		new User("jane@gmail.com", "Jane", "secret").save();
 
 		Project.create("Play 2", "play", "bob@gmail.com");
 		Project.create("Play 1", "play", "jane@gmail.com");
 
 		List<Project> results = Project.findInvolving("bob@gmail.com");
 		assertEquals(1, results.size());
 		assertEquals("Play 2", results.get(0).name);
 	}
 
 	@Test
 	public void findTodoTasksInvolving(){
 		User bob = new User("bob@gmail.com","Bob","secret");
 		bob.save();
 
 		Project project = Project.create("Play 2", "play", "bob@gmail.com");
 		Task t1 = new Task();
 		t1.title = "Write tutorial";
 		t1.assignedTo = bob;
 		t1.done = true;
 		t1.save();
 
 		Task t2 = new Task();
 		t2.title = "Release next version";
 		t2.project = project;
 		t2.save();
 
 		List<Task> results = Task.findTodoInvolving("bob@gmail.com");
 		assertEquals(1, results.size());
 		assertEquals("Release next version",results.get(0).title);
 	}
 
 
 	@Test
 	public void fullTest(){
		//Ebean.save((List) Yaml.load("test-data.yml"));
 
 		// Count things
 		assertEquals(3, User.find.findRowCount());
 		assertEquals(7, Project.find.findRowCount());
 		assertEquals(5, Task.find.findRowCount());
 
 		// Try to authenticate as users
 		assertNotNull(User.authenticate("bob@example.com","secret"));
 		assertNotNull(User.authenticate("jane@example.com","secret"));
 		assertNull(User.authenticate("jeff@example.com","badpassword"));
 		assertNull(User.authenticate("tom@example.com","secret"));
 
 		// Find all Bob's projects
 		List<Project> bobsProjects = Project.findInvolving("bob@example.com");
 		assertEquals(5, bobsProjects.size());
 
 		// Find all Bob's todo tasks
 		List<Task> bobsTasks = Task.findTodoInvolving("bob@example.com");
 		assertEquals(4, bobsTasks.size());
 	}
 }
