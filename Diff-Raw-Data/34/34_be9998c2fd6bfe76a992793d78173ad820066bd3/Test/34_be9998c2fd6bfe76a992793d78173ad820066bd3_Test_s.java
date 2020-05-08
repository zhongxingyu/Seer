 package controllers;
 
import models.BuddyLink;
 import models.User;
 import play.mvc.Controller;
 import play.mvc.Result;

import java.util.ArrayList;
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
            List<BuddyLink> budds = BuddyLink.findBuddies(u);
            u.buddies = new ArrayList<>();
            for(BuddyLink bl : budds){
                u.buddies.add(User.findById(bl.buddyId));
            }
         }
        return ok(views.html.test.render(users));
     }
 
     public static Result testAddTrip() {
          return ok();
     }
 }
