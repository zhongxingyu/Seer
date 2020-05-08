 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
 
 import models.*;
 
 import views.html.*;
 
 public class Application extends Controller {
   
    static Form<Login> taskForm = form(Login.class);
   public static Result index() {
      return ok(views.html.index.render(Login.all(), taskForm)
      );
   }
     
     public static Result login(){
         return TODO;
     }
     
     public static Result registrer(){
         return TODO;
     }
   
 }
