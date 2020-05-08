 package controllers;
 
 import java.util.List;
 
 import models.Task;
 import models.User;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.With;
 
 @With(Secure.class)
 public class Tasks extends Controller {
 	@Before
     static void setConnectedUser() {
         if(Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();
            renderArgs.put("user", user);
         }
     }
 	public static void index() {
		List<Task> tasks = Task.all().fetch();
 		render(tasks);
 	}
 	public static void show(Long id) {
 		Task task = Task.findById(id);
 		if (task == null) {
 			notFound();
 		} else {
 			render(task);
 		}
 	}
 }
