 package controllers;
 
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

 import java.util.HashMap;
 import java.util.Map;
 
 import models.Task;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.mvc.Http.Response;
 import play.test.Fixtures;
 import play.test.FunctionalTest;
 
 public class TasksTest extends FunctionalTest {
 	@Before
 	public void setUp() throws Throwable {
 		Fixtures.deleteDatabase();
 		Fixtures.loadModels("initial-data.yml");
 		Map<String, String> loginUserParams = new HashMap<String, String>();
 	    loginUserParams.put("username", "foo@bar.com");
 	    loginUserParams.put("password", "baz");
 	    POST("/login", loginUserParams);
 	}
 
 	@Test
 	public void doesntAllowAccessIfNotLoggedIn() throws Exception {
 		clearCookies();
 		Response response = GET("/tasks");
 		assertStatus(302, response);
 	}
 
 	@Test
 	public void rendersAllTasksInIndex() throws Exception {
 		Response response = GET("/tasks");
 		assertIsOk(response);
 		assertContentType("text/html", response);
 		assertCharset("UTF-8", response);
 		assertContentMatch("Your tasks", response);
 	}
 
 	@Test
 	public void showLoggedUserInIndex() throws Exception {
 		Response response = GET("/tasks");
 		assertContentMatch("Hello, foo@bar.com", response);
 	}
 
 	@Test
 	public void showsNiceMessageWhenThereAreNoTasks() throws Exception {
 		Task.deleteAll();
 		Response response = GET("/tasks");
 		assertContentMatch("No tasks", response);
 	}
 
 	@Test
 	public void listsTasksWithAndWithoutDate() throws Exception {
 		Response response = GET("/tasks");
		String responseContent = getContent(response);
		assertThat(responseContent, containsString("Buy a new PC"));
		assertThat(responseContent, containsString("One day..."));
		assertThat(responseContent, containsString("Finish this example"));
		assertThat(responseContent, containsString("20/08/2011"));
 	}
 
 	@Test
 	public void showASpecificTask() throws Exception {
 		Task firstTask = Task.all().first();
 		assertNotNull(firstTask);
 		Response response = GET("/tasks/" + firstTask.id);
 		assertContentMatch(firstTask.title, response);
 	}
 
 	@Test
 	public void tryToShowAnNonExistingTaskGives404() throws Exception {
 		Task.deleteAll();
 		assertEquals(0, Task.count());
 		Response response = GET("/tasks/1");
 		assertStatus(404, response);
 	}

	@Test
	public void showsOnlyTasksOfTheLoggedUser() throws Exception {
		Response response = GET("/tasks");
		assertThat(getContent(response), not(containsString("Kill Mr. Foo")));
	}
 }
