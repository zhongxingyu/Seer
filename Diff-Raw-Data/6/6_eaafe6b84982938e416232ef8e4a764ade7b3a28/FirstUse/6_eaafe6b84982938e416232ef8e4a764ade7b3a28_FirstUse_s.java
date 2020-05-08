 package controllers;
 
 import java.util.Map;
 
 import play.mvc.*;
 import play.data.*;
 import utils.FormHelper;
 import utils.Pipeline2Engine;
 import models.*;
 
 /**
  * Helps with configuring the Web UI for the first time.
  * 
  * Configure database -> configure administrative account -> set webservice endpoint -> set upload directory -> welcome page!
  * 
  * @author jostein
  */
 public class FirstUse extends Controller {
 	
 	/**
 	 * GET /firstuse
 	 * @return
 	 */
 	public static Result getFirstUse() {
 		
 		User user = null;
 		
 		if (isFirstUse()) {
 			if (!"desktop".equals(Application.deployment()) && !"server".equals(Application.deployment())) {
 				// Application mode is not set
 				
 				User.flashBrowserId(user);
 				return ok(views.html.FirstUse.setDeployment.render(play.data.Form.form(Administrator.SetDeploymentForm.class)));
 			
 			} else if ("server".equals(Application.deployment())) {
 				// Server mode
 				
 				if (User.find.where().eq("admin", true).findRowCount() == 0) {
 					User.flashBrowserId(user);
 					return ok(views.html.FirstUse.createAdmin.render(play.data.Form.form(Administrator.CreateAdminForm.class)));
 				}
 				
 				// require authentication to complete the rest of the first use wizard
 				user = User.authenticate(request(), session());
 				if (user == null || !user.admin) {
 					return redirect(routes.Login.login());
 				}
 				
 				if (Setting.get("dp2ws.endpoint") == null) {
 					User.flashBrowserId(user);
 					return ok(views.html.FirstUse.setWS.render(play.data.Form.form(Administrator.SetWSForm.class)));
 				}
 				
 				if (Setting.get("uploads") == null) {
 					User.flashBrowserId(user);
 					return ok(views.html.FirstUse.setStorageDirs.render(play.data.Form.form(Administrator.SetStorageDirsForm.class)));
 				}
 				
 			} else if ("desktop".equals(Application.deployment())) {
 				// Desktop mode
 				
 				User admin = User.find.where().eq("email", "email@example.com").findUnique();
 				if (admin == null) {
 					admin = new User("email@example.com", "Administrator", "password", true);
 				} else {
 					admin.admin = true;
 				}
 				admin.save(Application.datasource);
 				admin.login(session());
 			}
 			
 		}
 		
 		user = User.authenticate(request(), session());
 		if (user == null || !user.admin) {
 			return redirect(routes.Login.login());
 		}
 		
 		if ("desktop".equals(Application.deployment()) && Pipeline2Engine.getState() != Pipeline2Engine.State.RUNNING) {
 			User.flashBrowserId(user);
 			return ok(views.html.FirstUse.configureDP2.render(Pipeline2Engine.errorMessages));
 		}
 		
 		return redirect(routes.FirstUse.welcome());
 	}
 	
 	public static Result welcome() {
 		if (FirstUse.isFirstUse())
 			return redirect(routes.FirstUse.getFirstUse());
 
 		User user = User.authenticate(request(), session());
 		if (user == null)
 			return redirect(routes.Login.login());
 
 		User.flashBrowserId(user);
 		return ok(views.html.FirstUse.welcome.render());
 	}
 	
 	public static Result postFirstUse() {
 		Map<String, String[]> query = request().queryString();
 		Map<String, String[]> form = request().body().asFormUrlEncoded();
 		
 		String formName = form.containsKey("formName") ? form.get("formName")[0] : "";
 		
 		if ("setDeployment".equals(formName)) {
 			if (!isFirstUse())
 				return redirect(routes.FirstUse.getFirstUse());
 			
 			Form<Administrator.SetDeploymentForm> filledForm = play.data.Form.form(Administrator.SetDeploymentForm.class).bindFromRequest();
 			Administrator.SetDeploymentForm.validate(filledForm);
 			
 			if (query.containsKey("validate")) {
 				User.flashBrowserId(null);
 				return ok(FormHelper.asJson(filledForm));
 			
 			} else if (filledForm.hasErrors()) {
 				User.flashBrowserId(null);
 				return badRequest(views.html.FirstUse.setDeployment.render(filledForm));
 			
 			} else {
 				String deployment = filledForm.field("deployment").valueOr("unknown");
 				Setting.set("deployment", deployment);
 				
 				return redirect(routes.FirstUse.getFirstUse());
 			}
 		}
 		
 		if ("createAdmin".equals(formName)) {
 			if (!isFirstUse())
 				return redirect(routes.FirstUse.getFirstUse());
 			
 			Form<Administrator.CreateAdminForm> filledForm = play.data.Form.form(Administrator.CreateAdminForm.class).bindFromRequest();
 			Administrator.CreateAdminForm.validate(filledForm);
 			
 			if (query.containsKey("validate")) {
 				User.flashBrowserId(null);
 				return ok(FormHelper.asJson(filledForm,new String[]{"password","repeatPassword"}));
 			
 			} else if (filledForm.hasErrors()) {
 				User.flashBrowserId(null);
 				return badRequest(views.html.FirstUse.createAdmin.render(filledForm));
 			
 			} else {
 				User admin = new User(filledForm.field("email").valueOr(""), "Administrator", filledForm.field("password").valueOr(""), true);
 				admin.save(Application.datasource);
 				admin.login(session());
 				
 				// Set some default configuration options
 				Setting.set("users.guest.name", "Guest");
 				Setting.set("users.guest.allowGuests", "false");
 				Setting.set("users.guest.showGuestName", "true");
 				Setting.set("users.guest.showEmailBox", "true");
 				Setting.set("users.guest.shareJobs", "false");
 				Setting.set("users.guest.automaticLogin", "false");
 				Setting.set("mail.enable", "false");
 				
 				return redirect(routes.FirstUse.getFirstUse());
 			}
 		}
 		
 		User user = User.authenticate(request(), session());
 		if (user == null || !user.admin) {
 			return redirect(routes.Login.login());
 		}
 		
 		if ("setWS".equals(formName)) {
 			Form<Administrator.SetWSForm> filledForm = play.data.Form.form(Administrator.SetWSForm.class).bindFromRequest();
 			Administrator.SetWSForm.validate(filledForm);
 			
 			if (query.containsKey("validate")) {
 				User.flashBrowserId(user);
 				return ok(FormHelper.asJson(filledForm,new String[]{"authid","secret"}));
 			
 			} if (filledForm.hasErrors()) {
 				User.flashBrowserId(user);
 	        	return badRequest(views.html.FirstUse.setWS.render(filledForm));
 	        	
 	        } else {
 	        	Administrator.SetWSForm.save(filledForm);
 	        	return redirect(routes.FirstUse.getFirstUse());
 	        }
 		}
 		
 		if ("setStorageDirs".equals(formName)) {
 			Form<Administrator.SetStorageDirsForm> filledForm = play.data.Form.form(Administrator.SetStorageDirsForm.class).bindFromRequest();
 			Administrator.SetStorageDirsForm.validate(filledForm);
 			
 			if (query.containsKey("validate")) {
 				User.flashBrowserId(user);
 				return ok(FormHelper.asJson(filledForm));
 			
 			} else if (filledForm.hasErrors()) {
 				User.flashBrowserId(user);
 	        	return badRequest(views.html.FirstUse.setStorageDirs.render(filledForm));
 	        	
 	        } else {
 	        	Administrator.SetStorageDirsForm.save(filledForm);
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
 		return User.findAll().size() == 0 || Setting.get("dp2ws.endpoint") == null || Setting.get("uploads") == null || "desktop".equals(Application.deployment()) && (Pipeline2Engine.cwd == null || !Pipeline2Engine.State.RUNNING.equals(Pipeline2Engine.getState()));
 	}
 	
 	public static void configureDesktopDefaults() {
 		Setting.set("uploads", System.getProperty("user.dir") + System.getProperty("file.separator") + "uploads" + System.getProperty("file.separator"));
 		Setting.set("dp2ws.endpoint", controllers.Application.DEFAULT_DP2_ENDPOINT_LOCAL);
 		Setting.set("dp2ws.authid", "");
 		Setting.set("dp2ws.secret", "");
		Setting.set("dp2ws.tempdir", System.getProperty("user.dir") + controllers.Application.SLASH + "local.temp" + controllers.Application.SLASH);
		Setting.set("dp2ws.resultdir", System.getProperty("user.dir") + controllers.Application.SLASH + "local.results" + controllers.Application.SLASH);
 	}
 }
