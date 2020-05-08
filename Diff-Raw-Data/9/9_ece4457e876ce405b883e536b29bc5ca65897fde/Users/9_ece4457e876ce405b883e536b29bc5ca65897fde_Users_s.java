 package controllers.admin;
 
 import models.Defect;
 import models.User;
 import play.mvc.Controller;
 
 import java.util.List;
 
 /**
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 public class Users extends Controller {
 
     public static void index() {
         List<User> users = User.findAll();
         render(users);
     }
 
     public static void create(User user) {
        user.save();
         index();
     }
 
 }
