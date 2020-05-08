 package controllers;
 
 import models.Member;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 public class Register extends Controller {
 
     private final static Form<RegisterModel> registerForm = new Form<RegisterModel>(RegisterModel.class);
 
     public static Result index() {
         return ok(views.html.register.render(registerForm));
     }
 
     public static class RegisterModel {
 
         public String email;
         public String firstName;
         public String lastName;
         public String password;
     }
 
     public static Result add() {
         Form<RegisterModel> form = registerForm.bindFromRequest();
 
        form.reject("email", "test");
 
         if (form.hasErrors()) {
            form.reject("Please correct errors below");
             return badRequest(views.html.register.render(form));
         } else {
             Member.create(form.get().email, form.get().firstName, form.get().lastName, form.get().password);
             flash("success", "The member has been created");
             session().clear();
             session("email", form.get().email);
             return redirect(routes.Register.index());
         }
     }
 }
