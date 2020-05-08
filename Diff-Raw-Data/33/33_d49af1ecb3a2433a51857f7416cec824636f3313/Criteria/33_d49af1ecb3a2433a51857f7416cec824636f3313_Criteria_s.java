 package controllers.admin;
 
 import play.*;
 import play.data.*;
 import play.mvc.*;
 
 import models.*;
 import views.html.*;
 
 public class Criteria extends Controller {
 
 	public static Result index() {
 		return ok(views.html.criteria_admin.render(Criterion.find.all(), form(Criterion.class)));
 	}
 	
 	public static Result criterion(Long id) {
		Criterion criterion = Criterion.find.ref(id);
 		Form<Criterion> editForm = form(Criterion.class);
 		if (criterion != null) {
 			editForm = editForm.fill(criterion);
 		}
 		return ok(views.html.criterion_admin.render(criterion, editForm));
 	}
 	
 	public static Result create() {
 		Form<Criterion> createForm = form(Criterion.class).bindFromRequest();
 		createForm.get().save();
 		return ok(views.html.criteria_admin.render(Criterion.find.all(), form(Criterion.class)));
 	}
 	
 	public static Result update(Long id) {
 		Criterion criterion = Criterion.find.ref(id);
 		Form<Criterion> updateForm = form(Criterion.class).bindFromRequest();
 		Criterion updated = updateForm.get();
 		updated.update(criterion.id);
 		return ok(views.html.criterion_admin.render(updated, updateForm));
 	}
 	
 	public static Result delete(Long id) {
 		Criterion criterion = Criterion.find.ref(id);
 		criterion.delete();
 		return redirect("/admin/criterion");
 	}
 	
 }
