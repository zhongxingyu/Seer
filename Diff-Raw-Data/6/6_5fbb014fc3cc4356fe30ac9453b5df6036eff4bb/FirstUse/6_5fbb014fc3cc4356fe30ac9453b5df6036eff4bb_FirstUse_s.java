 package controllers;
 
import play.Logger;
 import play.mvc.*;
 import play.data.*;
 import models.*;
 
 /**
  * Helps with configuring the Web UI for the first time.
  * 
  * Configure database -> create admin account -> set webservice endpoint -> set upload directory -> configure SMTP -> welcome page!
  * 
  * @author jostein
  */
 public class FirstUse extends Controller {
 	
 	/**
 	 * GET /firstuse
 	 * @return
 	 */
 	public static Result getFirstUse() {
 		if (isFirstUse()) {
 			return ok(views.html.FirstUse.createAdmin.render(form(Administrator.CreateAdminForm.class)));
 		}
 		
 		User user = User.authenticate(session("userid"), session("email"), session("password"));
 		if (user == null || !user.admin) {
 			return redirect(routes.Login.login());
 		}
 		
 		if (Setting.get("dp2ws.endpoint") == null) {
 			return ok(views.html.FirstUse.setWS.render(form(Administrator.SetWSForm.class)));
 		}
 		
 		if (Setting.get("uploads") == null) {
 			return ok(views.html.FirstUse.setUploadDir.render(form(Administrator.SetUploadDirForm.class)));
 		}
 		
 		if (Setting.get("mail.from.email") == null) {
 			return ok(views.html.FirstUse.configureEmail.render(form(Administrator.ConfigureEmailForm.class)));
 		}
 		
 		return ok(views.html.FirstUse.welcome.render());
 	}
 	
 	public static Result postFirstUse() {
 		String formName = request().body().asFormUrlEncoded().containsKey("formName") ? request().body().asFormUrlEncoded().get("formName")[0] : "";
		for (String key : request().body().asFormUrlEncoded().keySet())
			for (String value : request().body().asFormUrlEncoded().get(key))
				Logger.debug(key+" : "+value);
		Logger.debug("formName: "+formName);
 		
 		if ("createAdmin".equals(formName)) {
 			if (!isFirstUse())
 				return redirect(routes.FirstUse.getFirstUse());
 			
 			Form<Administrator.CreateAdminForm> filledForm = form(Administrator.CreateAdminForm.class).bindFromRequest();
 			Administrator.CreateAdminForm.validate(filledForm);
 			
 			if (filledForm.hasErrors()) {
 				return badRequest(views.html.FirstUse.createAdmin.render(filledForm));
 			
 			} else {
 				User admin = new User(filledForm.field("email").valueOr(""), "Administrator", filledForm.field("password").valueOr(""), true);
 				admin.save();
 				session("userid", admin.id+"");
 				session("name", admin.name);
 				session("email", admin.email);
 				session("password", admin.password);
 				session("admin", admin.admin+"");
 				session("setws", "true");
 				
 				// Create the guest user (might as well do it here)
 				Setting.set("guest.name", "Guest");
 				Setting.set("guest.allowGuests", "false");
 				
 				return redirect(routes.FirstUse.getFirstUse());
 			}
 		}
 		
 		User user = User.authenticate(session("userid"), session("email"), session("password"));
 		if (user == null || !user.admin) {
 			return redirect(routes.Login.login());
 		}
 		
 		if ("setWS".equals(formName)) {
 			
 			Form<Administrator.SetWSForm> filledForm = form(Administrator.SetWSForm.class).bindFromRequest();
 			Administrator.SetWSForm.validate(filledForm);
 			
 			if (filledForm.hasErrors()) {
 	        	return badRequest(views.html.FirstUse.setWS.render(filledForm));
 	        	
 	        } else {
 	        	Setting.set("dp2ws.endpoint", filledForm.field("endpoint").valueOr(""));
 	        	Setting.set("dp2ws.authid", filledForm.field("authid").valueOr(""));
 	        	Setting.set("dp2ws.secret", filledForm.field("secret").valueOr(""));
 	        	return redirect(routes.FirstUse.getFirstUse());
 	        }
 		}
 		
 		if ("setUploadDir".equals(formName)) {
 			Form<Administrator.SetUploadDirForm> filledForm = form(Administrator.SetUploadDirForm.class).bindFromRequest();
 			Administrator.SetUploadDirForm.validate(filledForm);
 			
 			if(filledForm.hasErrors()) {
 	        	session("setws", "true");
 	        	return badRequest(views.html.FirstUse.setUploadDir.render(filledForm));
 	        	
 	        } else {
 	        	String uploadPath = filledForm.field("uploaddir").valueOr("");
 	        	if (!uploadPath.endsWith(System.getProperty("file.separator")))
 	        		uploadPath += System.getProperty("file.separator");
 	        	Setting.set("uploads", uploadPath);
 	        	return redirect(routes.FirstUse.getFirstUse());
 	        }
 		}
 		
 		if ("configureEmail".equals(formName)) {
 			Form<Administrator.ConfigureEmailForm> filledForm = form(Administrator.ConfigureEmailForm.class).bindFromRequest();
 			Administrator.ConfigureEmailForm.validate(filledForm);
 			
 			if(filledForm.hasErrors()) {
 				return badRequest(views.html.FirstUse.configureEmail.render(filledForm));
 
 			} else {
 				Setting.set("mail.username", filledForm.field("username").valueOr(""));
 				Setting.set("mail.password", filledForm.field("password").valueOr(""));
 				Setting.set("mail.smtp.host", filledForm.field("smtp").valueOr(""));
 				Setting.set("mail.smtp.port", "465"); // TODO: make configurable like the host
 				Setting.set("mail.smtp.ssl", "true"); // TODO: make configurable like the host
 				Setting.set("mail.from.name", "Pipeline 2");
 				Setting.set("mail.from.email", user.email);
 				return redirect(routes.FirstUse.getFirstUse());
 			}
 		}
 		
 		return getFirstUse();
 	}
 	
 	/**
 	 * Returns true if this is the first time that the Web UI are used (i.e. there are no registered users).
 	 * @return
 	 */
 	public static boolean isFirstUse() {
 		return User.findAll().size() == 0;
 	}
 
 }
