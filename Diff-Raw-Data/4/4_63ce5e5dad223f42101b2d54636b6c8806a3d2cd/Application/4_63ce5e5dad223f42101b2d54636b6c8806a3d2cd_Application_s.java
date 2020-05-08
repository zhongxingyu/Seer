 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import views.html.*;
 
 public class Application extends Controller {
   
   public static Result index() {
    //return ok(index.render("Your new application is ready."));
    return redirect(routes.Application.tasks());
   }
 
   public static Result tasks() {
     return TODO;
   }
 
   public static Result newTask() {
     return TODO;
   }
 
   public static Result deleteTask(Long id) {
     return TODO;
   }
   
 }
