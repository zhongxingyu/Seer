 package controllers;
 
 import java.util.List;
 import models.FriendShip;
 import play.mvc.*;
 import play.data.validation.*;
 
 @With(Secure.class)
 public class User extends Controller {
 
     /**
      * Action par défaut
      */
     public static void index()
     {
         redirect("/");
     }
 
     /**
      * Tableau de bord de l'utilisateur
      */
     public static void dashboard()
     {
         models.User user = User.connected();
         List<FriendShip> friends_waiting = FriendShip.find("friend = ? and status = ?", user, 0).fetch();
         List<FriendShip> friends_refuse = FriendShip.find("user = ? and status = ?", user, 1).fetch();
         List<FriendShip> friends_accept = FriendShip.find("user = ? and status = ?", user, 2).fetch();
         
         render(friends_waiting, friends_refuse, friends_accept);
     }
 
     /**
      * Action de déconnexion
      * @throws Throwable 
      */
     public static void logout() throws Throwable
     {
         Secure.logout();
     }
 
     /**
      * Page d'édition de profil
      * 
      * @view app/view/user/profile.html
      */
     public static void profile()
     {
         models.User user = User.connected();
         render(user);
     }
     
     public static void detail(Long id){
         models.User user = models.User.findById(id);
         render(user);
     }
 
     public static void editProfile(@Valid models.User user) {
         models.User userEdited = User.connected();
 
         if (validation.hasErrors()) {
             params.flash(); // add http parameters to the flash scope
             validation.keep(); // keep the errors for the next request
             redirect("/user/profile");
         }
         
        /*if(models.User.find("byEmail", user.email).first() != null && !models.User.find("byEmail", user.email).first().equals(userEdited)){
             validation.addError("user.email", play.i18n.Messages.get("error.email_exist"));
             params.flash(); // add http parameters to the flash scope
             validation.keep(); // keep the errors for the next request
             redirect("/user/profile");
        }*/
 
        //userEdited.email = user.email;
         userEdited.password = user.password;
         userEdited.firstname = user.firstname;
         userEdited.lastname = user.lastname;
         userEdited.save();
 
         redirect("/user/profile");
     }
 
     public static models.User connected() {
         models.User user = models.User.find("byEmail", Security.connected()).first();
         return user;
     }
 }
