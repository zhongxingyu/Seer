 package controllers.user.management;
 
 import it.sauronsoftware.ftp4j.FTPException;
 import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
 
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.spec.KeySpec;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.crypto.spec.PBEKeySpec;
 
 import org.bouncycastle.util.encoders.Hex;
 import org.springframework.validation.FieldError;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.Expr;
 import com.avaje.ebean.InvalidValue;
 import com.avaje.ebean.ValidationException;
 import com.avaje.ebean.annotation.Transactional;
 
 import controllers.EController;
 import controllers.question.QuestionManager;
 import controllers.question.ServerManager;
 import controllers.util.InputChecker;
 import controllers.util.PasswordHasher;
 import controllers.util.PasswordHasher.SaltAndPassword;
 
 import models.EMessages;
 import models.data.Language;
 import models.data.Link;
 import models.data.UnavailableLanguageException;
 import models.data.UnknownLanguageCodeException;
 import models.dbentities.QuestionModel;
 import models.dbentities.UserModel;
 import models.management.ModelState;
 import models.question.Server;
 import models.user.AuthenticationManager;
 import models.user.Gender;
 import models.user.GenderWrap;
 import models.user.IDGenerator;
 import models.user.Role;
 import models.user.User;
 import models.user.UserType;
 import models.user.UserTypeWrap;
 import play.data.Form;
 import play.data.validation.ValidationError;
 import play.mvc.Content;
 import play.mvc.Results;
 import play.mvc.Result;
 import views.html.commons.noaccess;
 import views.html.login.register;
 import views.html.question.editQuestionForm;
 import views.html.question.newQuestionForm;
 import views.html.question.server.newServerForm;
 import views.html.user.management.usermanager;
 import views.html.user.management.edituser;
 import views.html.user.management.createuser;
 
 
 /**
  * Code for Bebras Application by AVANK
  * User: thomas
  */
 
 public class UserManagerController extends EController {
 	
 	private static String edit_id;
 	
     public static boolean isAuthorized() {
         return AuthenticationManager.getInstance().getUser().hasRole(Role.MANAGEUSERS);
     }
     
     private static List<Link> defaultBreadcrumbs() {
         List<Link> breadcrumbs = new ArrayList<Link>();
         breadcrumbs.add(new Link("app.home", "/"));
         breadcrumbs.add(new Link(EMessages.get("user.management.others"), "/manage/others"));
         return breadcrumbs;
     }
 	
     public static Result showUsers(int page, String orderBy, String order, String filter){
 	    // breadcrumbs links
     	List<Link> breadcrumbs = defaultBreadcrumbs();
 		
 	    // authorized?
         if(!isAuthorized()) return Results.ok(noaccess.render(breadcrumbs));
         
         if(orderBy.equals("wrap_type")){
         	orderBy = "type";
         }else if(orderBy.equals("wrap_gender")){
         	orderBy = "gender";
         }else if(orderBy.equals("wrap_language")){
         	orderBy = "preflanguage";
         }
         
         // check which type of current logged in user we have.
         // And set the specific dataList for the type of user.
         
         UserManager userManager = new UserManager(ModelState.READ);
         if(AuthenticationManager.getInstance().getUser().getType().toString().equals("ADMINISTRATOR")){
         	// case ADMIN
         	userManager.setDataSet("ADMINISTRATOR");
         }else{
         	// case ORGANIZER
         	userManager.setDataSet("ORGANIZER");
         }
         
         userManager.setOrder(order);
         userManager.setOrderBy(orderBy);
         userManager.setFilter(filter);
        
         return Results.ok(
             usermanager.render(userManager.page(page), userManager, orderBy, order, filter, breadcrumbs)
         );
 	}
 		
     public static Result createUser(){	
 	    // breadcrumbs links
     	List<Link> breadcrumbs = new ArrayList<Link>();
 	    breadcrumbs.add(new Link("app.home", "/"));
 	    breadcrumbs.add(new Link(EMessages.get("user.management.create"), "/manage/users/create"));
 		
 	    // authorized?
         if(!isAuthorized()) return Results.ok(noaccess.render(breadcrumbs));
         Form<UserModel> form = form(UserModel.class).bindFromRequest();
 
         UserManager manager = new UserManager(ModelState.CREATE);
         manager.setIgnoreErrors(true);
 
         return ok(createuser.render(form, manager, breadcrumbs));
     }
     
     @Transactional
     public static Result removeUser(String id){
 	    // breadcrumbs links
     	List<Link> breadcrumbs = defaultBreadcrumbs();
 		
 	    // authorized?
         if(!isAuthorized()) return Results.ok(noaccess.render(breadcrumbs));
         
         // check if id is not the same as the id of the current active user
         if(AuthenticationManager.getInstance().getUser().getID().equals(id)){
 		    flash("error", EMessages.get(EMessages.get("user.error.sameid")));
 		    return Results.redirect(controllers.user.management.routes.UserManagerController.showUsers(0,"name","asc",""));
         }
         Ebean.delete(Ebean.find(UserModel.class).where().eq("id",id).findUnique());
         
         return Results.redirect(controllers.user.management.routes.UserManagerController.showUsers(0,"name","asc",""));
     }
     
     // find UserModel based on id
     public static UserModel findUser(String id){
     	UserModel userModel = Ebean.find(UserModel.class).where().eq(
                 "id",id).findUnique();
     	return userModel;
     }
     
     public static UserType getUserType(UserModel um){
     	return um.type;
     }
         
     @Transactional
     public static Result updateUser(){
     	List<Link> breadcrumbs = new ArrayList<Link>();
 	    breadcrumbs.add(new Link("app.home", "/"));
 	    breadcrumbs.add(new Link(EMessages.get("user.management.edit"), "/manage/users/update"));
         
 	    // old usermodel
 	    UserModel def_model = Ebean.find(UserModel.class).where().eq(
 				"id",edit_id).findUnique();
 	    
         if(!isAuthorized()) return ok(noaccess.render(breadcrumbs));
 
         Form<UserModel> form = form(UserModel.class).bindFromRequest();
 
         if(form.hasErrors()) {
             return badRequest(edituser.render(form, new UserManager(ModelState.UPDATE), breadcrumbs));
         }
         
         try {
         	
         	Date currentDate = new Date();
         	Date birthday = new SimpleDateFormat("dd/MM/yyyy").parse(form.data().get("birthdate"));   	
     		
         	// validate birthday
 			if(birthday.after(currentDate)){
 				flash("error", EMessages.get(EMessages.get("error.wrong_date_time")));
 				return badRequest(edituser.render(form, new UserManager(ModelState.UPDATE), breadcrumbs));
 			}
 			def_model.birthdate = birthday;
 			
 			Date regday = new SimpleDateFormat("dd/MM/yyyy").parse(form.data().get("registrationdate"));
     		def_model.registrationdate = regday;
     		
     		// name
     		def_model.name = form.data().get("name");
     		
     		// check if email is unique and validate
 			if(Ebean.find(UserModel.class).where().and(Expr.eq("email",form.data().get("email")),Expr.ne("id",def_model.id)).findUnique() != null){
 				flash("error", EMessages.get(EMessages.get("register.same_email")));
 				return badRequest(edituser.render(form, new UserManager(ModelState.UPDATE), breadcrumbs));
 			}
 			
 			if(!InputChecker.getInstance().isCorrectEmail(form.data().get("email"))){
 			    if(!form.data().get("email").isEmpty()){
 			        flash("error", EMessages.get(EMessages.get("user.error.wrong_email")));
 	        	    return badRequest(edituser.render(form, new UserManager(ModelState.UPDATE), breadcrumbs));
 			    }else{
 			        def_model.email = null;
 			    }
 			}else{
 			    def_model.email = form.data().get("email");
 			}
 
 			// password
 			if(!form.data().get("password1").isEmpty()){
 				
 				// check if two passwords are the same
 				if(!form.data().get("password1").equals(form.data().get("password2"))){
 		            flash("error", EMessages.get(EMessages.get("user.error.passwnotequal")));
 	        	    return badRequest(edituser.render(form, new UserManager(ModelState.UPDATE), breadcrumbs));
 				}
 				
     		    // edit passw
     		    String passw = AuthenticationManager.getInstance().simulateClientsidePasswordStrengthening(form.data().get("password1"));
     		
     		    SaltAndPassword sp = PasswordHasher.generateSP(passw.toCharArray());
     		    String passwordHEX = sp.password;
     		    String saltHEX = sp.salt;
     		    
     		    def_model.password = passwordHEX;
     		    def_model.hash = saltHEX;
 			}
 			
 			// check if blocked
 			if(form.data().get("blocked") == null){
 				// unblocked
 				def_model.blockeduntil = null;
 			}else{
 				// blocked
 
 				// check if the user left the blockinput empty
 				if(form.data().get("blockeduntil").isEmpty()){
 					def_model.blockeduntil = null;
 				}else{
 					Date cd = new Date();
 		        	Date bu = new SimpleDateFormat("dd/MM/yyyy").parse(form.data().get("blockeduntil"));   	
 		    		
 		        	// check if valid blockeduntil date
 					if(!bu.after(cd)){
 						flash("error", EMessages.get(EMessages.get("user.error.blockdateinvalid")));
 						return badRequest(edituser.render(form, new UserManager(ModelState.UPDATE), breadcrumbs));	
 					}else{
 						// set
 						def_model.blockeduntil = new SimpleDateFormat("dd/MM/yyyy").parse(form.data().get("blockeduntil"));
 					}
 				}	
 			}
 			
     		def_model.gender = GenderWrap.getUserType(form.data().get("wrap_gender.id"));
     		def_model.type = UserTypeWrap.getUserType(form.data().get("wrap_type.id"));
    		def_model.preflanguage = form.data().get("wrap_language.id");
     		def_model.comment = form.data().get("comment");
     		
         } catch (Exception e) {
             flash("error", e.getMessage());
             return badRequest(edituser.render(form, new UserManager(ModelState.UPDATE), breadcrumbs));
         }
         
         Ebean.save(def_model);
 
         // success!
         flash("success", EMessages.get("user.success.edited", edit_id));
         return Results.redirect(controllers.user.management.routes.UserManagerController.showUsers(0,"name","asc",""));
     }
     
     @Transactional(readOnly=true)
     public static Result editUser(String id){
         ArrayList<Link> breadcrumbs = new ArrayList<Link>();
         Content showpage;
         breadcrumbs.add(new Link(EMessages.get("app.home"),"/"));
         breadcrumbs.add(new Link(EMessages.get("user.management.edit"),"/manage/users/"+id+"/edit"));
 	    
         // authorized?
         if(!isAuthorized()) return Results.ok(noaccess.render(breadcrumbs));
         
         // check if id is not the same as the id of the current active user
         if(AuthenticationManager.getInstance().getUser().getID().equals(id)){
 		    flash("error", EMessages.get(EMessages.get("user.error.sameid")));
 		    return Results.redirect(controllers.user.management.routes.UserManagerController.showUsers(0,"name","asc",""));
         }
         
         UserManager manager = new UserManager(ModelState.UPDATE);
         
         Form<UserModel> form = form(UserModel.class).bindFromRequest();
         
         edit_id = id;
         UserModel id_model = Ebean.find(UserModel.class).where().eq(
 				"id",id).findUnique();
         
         // setting the default values
         form.get().name = id_model.name;
         form.get().email = id_model.email;
         form.get().birthdate = id_model.birthdate;
         form.get().registrationdate = id_model.registrationdate;
         form.get().preflanguage = id_model.preflanguage;
         try {
 			form.get().wrap_language = Language.getLanguage(id_model.preflanguage);
 		} catch (Exception e) {
 			flash("error", e.getMessage());
             return Results.redirect(controllers.user.management.routes.UserManagerController.showUsers(0,"name","asc",""));
 		}
 		
 		form.get().comment = id_model.comment;
 		
 		// setting blocked/blockdate values
 		if(id_model.blockeduntil == null){
 			form.get().blocked = false;
 		}else{
 			form.get().blocked = true;
 			form.get().blockeduntil = id_model.blockeduntil;
 		}
 		
         manager.setIgnoreErrors(true);
 
         edit_id = id;
         
         return ok(edituser.render(form, manager, breadcrumbs));
     }
     
     @Transactional
     public static Result saveUser(){
     	List<Link> breadcrumbs = new ArrayList<Link>();
 	    breadcrumbs.add(new Link("app.home", "/"));
 	    breadcrumbs.add(new Link(EMessages.get("user.management.create"), "/manage/users/create"));
         
         if(!isAuthorized()) return ok(noaccess.render(breadcrumbs));
 
         Form<UserModel> form = form(UserModel.class).bindFromRequest();
 
         if(form.hasErrors()) {
             return badRequest(createuser.render(form, new UserManager(ModelState.CREATE), breadcrumbs));
         }
         
         try {
         	Date currentDate = new Date();
         	
     		// set calendar and validate
 			if(form.get().birthdate.after(currentDate)){
 				flash("error", EMessages.get(EMessages.get("error.wrong_date_time")));
 				return badRequest(createuser.render(form, new UserManager(ModelState.CREATE), breadcrumbs));
 			}
     		Calendar birthday = Calendar.getInstance();
     		birthday.setTime(form.get().birthdate);
     		
     		// check if email is unique and validate
 			if(Ebean.find(UserModel.class).where().eq(
 					"email",form.get().email).findUnique() != null){
 				flash("error", EMessages.get(EMessages.get("register.same_email")));
 				return badRequest(createuser.render(form, new UserManager(ModelState.CREATE), breadcrumbs));
 			}
 			
 			if(!InputChecker.getInstance().isCorrectEmail(form.get().email)){
 				if(!form.get().email.isEmpty()){
 		            flash("error", EMessages.get(EMessages.get("user.error.wrong_email")));
 	        	    return badRequest(createuser.render(form, new UserManager(ModelState.CREATE), breadcrumbs));
 				}else{
 					form.get().email = null;
 				}
 			}
     		
     		// set id
     		String bebrasID = null;
     		bebrasID = IDGenerator.generate(form.get().name, birthday);
     		
     		
     		// set passw
     		String passw = AuthenticationManager.getInstance().simulateClientsidePasswordStrengthening(bebrasID);
     		
     		SaltAndPassword sp = PasswordHasher.generateSP(passw.toCharArray());
     		String passwordHEX = sp.password;
     		String saltHEX = sp.salt;
     		
     		// set other fields in usermodel
     		form.get().password = passwordHEX;
     		form.get().hash = saltHEX;
     		form.get().id = bebrasID;
     		form.get().gender = GenderWrap.getUserType(form.data().get("wrap_gender.id"));
     		form.get().type = UserTypeWrap.getUserType(form.data().get("wrap_type.id"));
    		form.get().preflanguage = form.data().get("wrap_language.id");
     		form.get().comment = form.data().get("comment");
     		
     		// save
             form.get().save();
             
             UserModel dum = Ebean.find(UserModel.class).where().eq(
 					"id",bebrasID).findUnique();
                        
         } catch (Exception e) {
             flash("error", e.getMessage());
             return badRequest(createuser.render(form, new UserManager(ModelState.CREATE), breadcrumbs));
         }
 
         flash("success", EMessages.get("user.success.added", form.get().id));
         return Results.redirect(controllers.user.management.routes.UserManagerController.showUsers(0,"name","asc",""));
     }
     
     /*
      * HELPMETHODS FOR DEFAULTFORMINFOPASSW => SHOWING CORRECT LISTVALUES FOR USER WITH
      * ID: EDIT_ID
      */
     
     // USERTYPE
     public static String getUserType(){
     	UserModel dum = Ebean.find(UserModel.class).where().eq(
 				"id",edit_id).findUnique();
     	return dum.type.toString();
     }
     
     // USERLANG
     public static String getUserLang(){
     	UserModel dum = Ebean.find(UserModel.class).where().eq(
 				"id",edit_id).findUnique();
     	return dum.preflanguage;
     }
     
     // USERGENDER
     public static String getUserGender(){
     	UserModel dum = Ebean.find(UserModel.class).where().eq(
 				"id",edit_id).findUnique();
     	return dum.gender.toString();
     }
 }
