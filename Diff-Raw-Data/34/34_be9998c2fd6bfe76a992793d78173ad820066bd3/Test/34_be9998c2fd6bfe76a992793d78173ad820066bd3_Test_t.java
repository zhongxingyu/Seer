 package controllers;
 
 import models.User;
 import play.mvc.Controller;
 import play.mvc.Result;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Stijn
  * Date: 3-4-13
  * Time: 0:11
  * To change this template use File | Settings | File Templates.
  */
 public class Test extends Controller {
     public static Result testBuddy() {
         List<User> users = User.all();
         for(User u : users){
            u = User.addBuddies(u);
         }
        return ok(views.html.testBuddies.render(users));
     }
 
     public static Result testAddTrip() {
          return ok();
     }
 }
