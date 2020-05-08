 package controllers.admin;
 
 import play.*;
 import play.data.*;
 import play.mvc.*;
 
 import controllers.*;
 import models.*;
 import views.html.*;
 
 @Security.Authenticated(Secured.class)
 public class Users extends Controller {
 
 	public static Result index() {
 		return ok(views.html.users_admin.render(User.find.all(), Role.find.all(), form(User.class)));
 	}
 	
 	public static Result user(String username) {
 		User user = User.find.where().eq("username", username).findUnique();
 		Form<User> editForm = form(User.class);
 		if (user != null) {
 			editForm = editForm.fill(user);
 		}
 		return ok(views.html.user_admin.render(user, user.ballotThisUser(), editForm));
 	}
 	
 	public static Result create() {
 		DynamicForm requestForm = form().bindFromRequest();
 		String username = requestForm.get("username");
 		String password = requestForm.get("password");
 		Long role_id = Long.parseLong(requestForm.get("role_id"));
 		Role role = Role.find.ref(role_id);
 		new User(username, password, role).save();
 		return ok(views.html.users_admin.render(User.find.all(), Role.find.all(), form(User.class)));
 	}
 	
 	public static Result update(String username) {
 		User edit_user = User.find.where().eq("username", username).findUnique();
 		DynamicForm requestForm = form().bindFromRequest();
 		String new_username = requestForm.get("new_username");
 		String new_password = requestForm.get("new_password");
 		String new_name = requestForm.get("new_name");
 		Long new_role_id = Long.parseLong(requestForm.get("new_role_id"));
 		Role new_role = Role.find.ref(new_role_id);
 		User update_user = new User(new_username, new_password, new_name, new_role, edit_user.isAdmin, edit_user.firstLogin);
 		update_user.update(edit_user.id);
		if (session("username") == edit_user.username) {
			session("username", update_user.username);
		}
 		return ok(views.html.user_admin.render(update_user, update_user.ballotThisUser(), form(User.class)));
 	}
 	
 	public static Result delete(String username) {
 		User user = User.find.where().eq("username", username).findUnique();
 		user.delete();
 		return ok(views.html.users_admin.render(User.find.all(), Role.find.all(), form(User.class)));
 	}
 	
 }
