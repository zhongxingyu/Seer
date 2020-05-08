 package controllers;
 
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import play.api.libs.Crypto;
 import play.data.Form;
 import play.data.format.Formats;
 import play.data.validation.Constraints.Required;
 import play.mvc.Result;
 import play.mvc.Results;
 
 import models.EMessages;
 import models.data.Link;
 import models.dbentities.UserModel;
 import models.user.AuthenticationManager;
 import models.user.UserType;
 
 import views.html.landing_page;
 import views.html.login.error;
 import views.html.login.register;
 import views.html.login.registerLandingPage;
 
 import com.avaje.ebean.Ebean;
 
 /**
  * This class receives all GET requests and based on there session identifier (cookie)
  * and current role in the system they will be served a different view.
  * @author Sander Demeester, Ruben Taelman
  */
 public class UserController extends EController{
 
 	/**
 	 * This methode gets requested when the user clicks on "signup".
 	 * @return Result page.
 	 */
 	public static Result signup(){
 		List<Link> breadcrumbs = new ArrayList<Link>();
 		breadcrumbs.add(new Link("Home", "/"));
 		breadcrumbs.add(new Link("Sign Up", "/signup"));
 		return ok(register.render(EMessages.get("register.title"),
 				breadcrumbs,
 				form(Register.class)
 				));
 	}
 
 	/**
 	 * this methode is called when the user submits his/here register information.
 	 * @return Result page
 	 */
 	public static Result register(){
 		// Bind play form request.
 		Form<Register> registerForm = form(Register.class).bindFromRequest();
 		List<Link> breadcrumbs = new ArrayList<Link>();
 		breadcrumbs.add(new Link("Home", "/"));
 		breadcrumbs.add(new Link("Sign Up", "/signup"));
 
 		// If the form contains error's (specified by "@"-annotation in the class "Register" then this will be true.
 		if(registerForm.hasErrors()){
			flash("error", EMessages.get(EMessages.get("error.text")));
 			return badRequest(register.render((EMessages.get("register.title")), breadcrumbs, registerForm));
 		}
 
 		Pattern pattern = Pattern.compile("[^a-z ]", Pattern.CASE_INSENSITIVE);
 		Matcher matcher = pattern.matcher(registerForm.get().name);
 
 
 		// check if date is lower then current date
 		try{
 			Date birtyDay    = new SimpleDateFormat("yyyy/mm/dd").parse(registerForm.get().bday);
 			Date currentDate = new Date();
 			
 			if(birtyDay.after(currentDate)){
 				flash("error", EMessages.get(EMessages.get("error.wrong_date_time")));
 				return badRequest(register.render((EMessages.get("register.title")), breadcrumbs, registerForm));
 			}
 		}catch(Exception e){
 			flash("error", EMessages.get(EMessages.get("error.date")));
 			return badRequest(register.render((EMessages.get("register.title")), breadcrumbs, registerForm));
 		}
 		// Check if the email adres is uniqe.
 		if(!registerForm.get().email.isEmpty()){
 
 			if(Ebean.find(UserModel.class).where().eq(
 					"email",registerForm.get().email).findUnique() != null){
 
 				flash("error", EMessages.get(EMessages.get("register.same_email")));
 				return badRequest(register.render((EMessages.get("register.title")), breadcrumbs, registerForm));
 			}
 		}
 
 
 
 		// Check if full name contains invalid symbols.
 		if(matcher.find()){
 			flash("error", EMessages.get(EMessages.get("error.invalid_symbols")));
 			return badRequest(register.render((EMessages.get("register.title")), breadcrumbs, registerForm));
 		}
 
 		// Compile new pattern to check for invalid email symbols. 
 		// These are all the symbols that are allow in email addresses.
 		// Alle symbols are containd in character classes, so no need for escaping.
 		pattern = Pattern.compile("[^a-z._+@0-9!#$%&'*+-/=?^_`{|}~]");
 		matcher = pattern.matcher(registerForm.get().email);
 
 		if(matcher.find()){
 			flash("error", EMessages.get(EMessages.get("error.invalid_email")));
 			return badRequest(register.render((EMessages.get("register.title")), breadcrumbs, registerForm));
 		}
 
 		// Try to validate email, this check happens on the client side, but date can be send without using the form.
 		// If the check fails the user is presented with a error page.
 		try{
 			new SimpleDateFormat("yyyy/mm/dd").parse(registerForm.get().bday);
 		}catch(Exception e){
 			flash("error", EMessages.get(EMessages.get("error.invalid_date")));
 			return badRequest(register.render((EMessages.get("register.title")), breadcrumbs, registerForm));
 		}
 
 		// Delegate create user to Authentication Manager.
 		String bebrasID = null;
 		try {
 			bebrasID = AuthenticationManager.getInstance().createUser(registerForm);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// Return a register succes page.
 		return ok(registerLandingPage.render(EMessages.get("info.success"), new ArrayList<Link>(), bebrasID));
 	}
 
 	/**
 	 * This methode is called when the users clicks on "login".
 	 * @return returns the users cookie.
 	 */
 	public static Result validate_login(String id, String password) throws Exception{
 		String cookie = "";
 		try {
 			//generate random id to auth user.
 			cookie = Integer.toString(Math.abs(SecureRandom.getInstance("SHA1PRNG").nextInt(100)));
 
 			//set the cookie. There really is no need for Crypto.sign because a cookie should be random value that has no meaning
 			cookie = Crypto.sign(cookie);
 			//response().setCookie(AuthenticationManager.COOKIENAME, cookie);
 
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 
 		// We do the same check here, if the input forms are empty return a error message.
 		if(id == "" || password == "") {
 			return badRequest(EMessages.get("register.giveinfo"));
 		} else if(AuthenticationManager.getInstance().validate_credentials(id, password, cookie)){
 			return ok(cookie);
 		} else {
 			return badRequest(EMessages.get("error.login"));
 		}
 	}
 
 	/**
 	 * Logout current user
 	 * @return Result
 	 */
 	public static Result logout(){
 		AuthenticationManager.getInstance().logout();
 		return Results.redirect(routes.Application.index());
 	}
 
 	/**
 	 * @return Returns a scala template based on the type of user that is requesting the page.
 	 **/
 	@SuppressWarnings("unchecked")
 	public static Result landingPage() throws Exception{
 		List<Link> breadcrumbs = new ArrayList<Link>();
 		breadcrumbs.add(new Link("Home", "/"));
 
 		UserType type = AuthenticationManager.getInstance().getUser().getType();
 		if(UserType.ANON.equals(type)) {
 			return Results.redirect(routes.Application.index());
 		} else {
 			return ok(views.html.landing_page.render(
 					AuthenticationManager.getInstance().getUser(),
 					breadcrumbs
 					));
 		}
 	}
 
 	/**
 	 * Inline class that contains public fields for play forms.
 	 */
 	public static class Register{
 		@Required
 		public String name;
 		public String email;
 		@Required
 		@Formats.DateTime(pattern = "yyyy/dd/mm")
 		public String bday;
 		@Required
 		public String password;
 		@Required
 		public String controle_passwd;
 		@Required
 		public String gender;
 		@Required
 		public String prefLanguage;
 	}
 	/**
 	 * Inline class that contains public fields for play forms.
 	 */
 	public static class Login{
 		public String id;
 		public String password;
 	}
 }
