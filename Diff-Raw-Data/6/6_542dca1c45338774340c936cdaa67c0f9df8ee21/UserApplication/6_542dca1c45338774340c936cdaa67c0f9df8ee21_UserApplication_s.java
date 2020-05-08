 package controllers;
 
 import static play.data.Form.form;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.codec.digest.DigestUtils;
 
 import models.Credential;
 import models.Role;
 import models.User;
 import play.Logger.ALogger;
 import play.data.Form;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security;
 import views.html.users.loggedInUserChangePasswordForm;
 import views.html.users.changePasswordForm;
 import views.html.users.userCreateForm;
 import views.html.users.userEditForm;
 import views.html.users.loggedInUserEditForm;
 import views.html.users.userList;
 
 /**
  * Manage a database of users
  */
 @Security.Authenticated(Secured.class)
 public class UserApplication extends Controller {
 
 	static ALogger logger = play.Logger.of(UserApplication.class);
 	
     /**
      * This result directly redirect to application home.
      */
     public static Result GO_HOME = redirect(
         routes.UserApplication.list(0, "name", "asc", "")
     );
     
     /**
      * Handle default path requests, redirect to users list
      */
     public static Result index() {
         return GO_HOME;
     }
 
     /**
      * Display the paginated list of users.
      *
      * @param page Current page number (starts from 0)
      * @param sortBy Column to be sorted
      * @param order Sort order (either asc or desc)
      * @param filter Filter applied on user names
      */
     public static Result list(int page, String sortBy, String order, String filter) {
         return ok(
             userList.render(
                 User.page(page, 10, sortBy, order, filter),
                 sortBy, order, filter
             )
         );
     }
     
     /**
      * Display the 'edit form' of a existing User.
      *
      * @param id Id of the user to edit
      */
     public static Result edit(Long id) {
     	
     	User user = User.find.byId(id);
     	
     	for(Role role : user.roles){
     		logger.debug(role.name);
     	}
     	
         Form<User> userForm = form(User.class).fill(
             User.find.byId(id)
         );
         return ok(
             userEditForm.render(id, userForm)
         );
     }
     
 
     /**
      * Display the 'edit form' of a existing User.
      *
      * @param id Id of the user to edit
      */
     public static Result loggedInUserEditProfile() {
     	
     	Long id = Long.valueOf(session("userId"));
     	
     	User user = User.find.byId(id);
     	
     	for(Role role : user.roles){
     		logger.debug(role.name);
     	}
     	
         Form<User> userForm = form(User.class).fill(
             User.find.byId(id)
         );
         return ok(
             loggedInUserEditForm.render(userForm)
         );
     }
     
 
     public static Result changePassword(Long id) {
     	
         Form<Credential> credentialForm = form(Credential.class).fill(
         		Credential.find.byId(id)
         );
         return ok(
             changePasswordForm.render(id, credentialForm)
         );
     }
     
 
     public static Result loggedInUserChangePassword() {
     	
     	Long id = Long.valueOf(session("userCredentialId"));
     	
         Form<Credential> credentialForm = form(Credential.class).fill(
         		Credential.find.byId(id)
         );
         return ok(
             loggedInUserChangePasswordForm.render(id, credentialForm)
         );
     }
     
     /**
      * Handle the 'edit form' submission 
      *
      * @param id Id of the user to edit
      */
     public static Result update(Long id) {
         Form<User> userForm = form(User.class).bindFromRequest();
         if(userForm.hasErrors()) {
             return badRequest(userEditForm.render(id, userForm));
         }
         
         List<Role> oldRoles = userForm.get().roles;
         List<Role> newRoles = new ArrayList<Role>();
         
         for(Role role : oldRoles){
         	if(role.id != null){
         		newRoles.add(Role.find.byId(role.id));
         	}
         }
         oldRoles = null;
         
         userForm.get().roles = newRoles;
 
         logger.debug(userForm.get().roles.toString());
 
         
         userForm.get().update(id);
         
         flash("success", "User " + userForm.get().name + " has been updated");
         return GO_HOME;
     }
     
     public static Result loggedInUserUpdateProfile() {
 
     	Long id = Long.valueOf(session("userId"));
     	
         Form<User> userForm = form(User.class).bindFromRequest();
         if(userForm.hasErrors()) {
             return badRequest(loggedInUserEditForm.render(userForm));
         }
         
        userForm.get().update(id);
         
 
         session("userName", userForm.get().name);
         session("email", userForm.get().email);
         
         flash("success", "User " + userForm.get().name + " has been updated");
         return redirect(routes.Application.index());
     }
     
     public static Result updatePassword(Long id) {
     	
         Form<Credential> credentialForm = form(Credential.class).bindFromRequest();
         if(credentialForm.hasErrors()) {
             return badRequest(changePasswordForm.render(id, credentialForm));
         }
         credentialForm.get().password = DigestUtils.md5Hex(credentialForm.get().password);
         
         credentialForm.get().update(id);
         flash("success", "User password has been updated");
         return GO_HOME;
     }
     
     public static Result loggedInUserUpdatePassword() {
 
     	Long id = Long.valueOf(session("userCredentialId"));
     	
         Form<Credential> credentialForm = form(Credential.class).bindFromRequest();
         if(credentialForm.hasErrors()) {
             return badRequest(loggedInUserChangePasswordForm.render(id, credentialForm));
         }
         credentialForm.get().password = DigestUtils.md5Hex(credentialForm.get().password);
         
         credentialForm.get().update(id);
         flash("success", "User password has been updated");
         return redirect(routes.Application.index());
     }
     
     /**
      * Display the 'new user form'.
      */
     public static Result create() {
         Form<User> userForm = form(User.class);
         return ok(
             userCreateForm.render(userForm)
         );
     }
     
     /**
      * Handle the 'new user form' submission 
      */
     public static Result save() {
         Form<User> userForm = form(User.class).bindFromRequest();
         if(userForm.hasErrors()) {
             return badRequest(userCreateForm.render(userForm));
         }
         userForm.get().credential.password = DigestUtils.md5Hex(userForm.get().credential.password);
         userForm.get().save();
         flash("success", "User " + userForm.get().name + " has been created");
         return GO_HOME;
     }
     
     /**
      * Handle user deletion
      */
     public static Result delete(Long id) {
         User.find.ref(id).delete();
         flash("success", "User has been deleted");
         return GO_HOME;
     }
 
     
     public static Result apiList(){
     	
     	List<User> users = User.find.findList();
     	
     	for(User user : users){
     		user.credential = null;
     	}
     	
     	return ok(Json.toJson(users));
     }
     
 
 }
             
