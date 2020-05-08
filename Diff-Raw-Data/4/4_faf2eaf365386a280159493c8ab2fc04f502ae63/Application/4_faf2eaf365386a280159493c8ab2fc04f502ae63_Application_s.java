 package controllers;
 
import com.zapodot.metrics.Metered;
import com.zapodot.metrics.Timed;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.index;
 
 public class Application extends Controller {
 
     @Timed
     @Metered
     public static Result index() {
         return ok(index.render("Your new application is ready."));
     }
 
     @Timed
     public static Result waitAWhile() {
         final long initialTime = System.currentTimeMillis();
         while(System.currentTimeMillis() < (initialTime + 3000L));
         return ok("Completed");
     }
   
 }
