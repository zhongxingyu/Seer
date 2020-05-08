 package controllers.admin;
 
 import play.*;
 import play.data.*;
 import play.mvc.*;
 
 import models.*;
 import views.html.*;
 
 public class Users extends Controller {
 
 	public static Result index() {
 		return ok(views.html.users_admin.render(User.find.all(), form(User.class)));
 	}
 	
 	public static Result user(String username) {
		User user = User.find.ref(username);
 		Form<User> editForm = form(User.class);
 		if (user != null) {
 			editForm = editForm.fill(user);
 		}
 		return ok(views.html.user_admin.render(user, editForm));
 	}
 	
 	public static Result create() {
		return TODO;
 	}
 	
 	public static Result update(String username) {
		return TODO;
 	}
 	
 	public static Result delete(String username) {
		return TODO;
 	}
 	
 }
