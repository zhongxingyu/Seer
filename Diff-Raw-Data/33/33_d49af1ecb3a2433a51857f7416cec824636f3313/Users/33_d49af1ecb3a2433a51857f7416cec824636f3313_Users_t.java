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
		User user = User.find.where().eq("username", username).findUnique();
 		Form<User> editForm = form(User.class);
 		if (user != null) {
 			editForm = editForm.fill(user);
 		}
 		return ok(views.html.user_admin.render(user, editForm));
 	}
 	
 	public static Result create() {
		Form<User> createForm = form(User.class).bindFromRequest();
		createForm.get().save();
		return ok(views.html.users_admin.render(User.find.all(), form(User.class)));
 	}
 	
 	public static Result update(String username) {
		User user = User.find.ref(username);
		Form<User> updateForm = form(User.class).bindFromRequest();
		User updated = updateForm.get();
		System.out.println(user.username + " " + user.password);
		System.out.println(updated.username + " " + updated.password);
		updated.update(username);
		return ok(views.html.user_admin.render(updated, updateForm));
		/*Criterion criterion = Criterion.find.ref(id);
		Form<Criterion> updateForm = form(Criterion.class).bindFromRequest();
		Criterion updated = updateForm.get();
		updated.update(criterion.id);
		return ok(views.html.criterion_admin.render(updated, updateForm));*/
 	}
 	
 	public static Result delete(String username) {
		User user = User.find.ref(username);
		user.delete();
		return ok(views.html.users_admin.render(User.find.all(), form(User.class)));
 	}
 	
 }
