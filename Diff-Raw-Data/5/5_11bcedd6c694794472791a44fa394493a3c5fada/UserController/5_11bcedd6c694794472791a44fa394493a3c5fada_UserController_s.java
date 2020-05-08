 package controllers;
 
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import models.EMessages;
 import models.data.Link;
 import models.dbentities.UserModel;
 import models.user.AuthenticationManager;
 import models.user.User;
 import models.user.UserType;
 import play.Play;
 import play.api.libs.Crypto;
 import play.api.templates.Html;
 import play.api.templates.Template2;
 import play.data.Form;
 import play.data.format.Formats;
 import play.data.validation.Constraints.Required;
 import play.mvc.Result;
 import play.mvc.Results;
 import views.html.landingPages.AdminLandingPage;
 import views.html.landingPages.IndependentPupilLandingPage;
 import views.html.landingPages.OrganizerLandingPage;
 import views.html.landingPages.PupilLandingPage;
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
 	 * This hashmap embodies the mapping from a Type to a view.
 	 * Each view is responsible for getting all information from the DataModel and make a
 	 * beautiful view for the user :)
 	 */
 	private static HashMap<UserType, Class<?>> LANDINGPAGES = new HashMap<UserType, Class<?>>();
 	static {
 		LANDINGPAGES.put(UserType.ADMINISTRATOR, AdminLandingPage.class);
 		LANDINGPAGES.put(UserType.INDEPENDENT, IndependentPupilLandingPage.class);
 		LANDINGPAGES.put(UserType.INDEPENDENT, IndependentPupilLandingPage.class);
 		LANDINGPAGES.put(UserType.ORGANIZER, OrganizerLandingPage.class);
 		LANDINGPAGES.put(UserType.PUPIL,PupilLandingPage.class);
 	};
 
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
 		Pattern pattern = Pattern.compile("[^a-z ]", Pattern.CASE_INSENSITIVE);
 		Matcher matcher = pattern.matcher(registerForm.get().name);
 
 		// Check if the email adres is uniqe.
 		if(!registerForm.get().email.isEmpty()){
 
 			if(Ebean.find(UserModel.class).where().eq(
 					"email",registerForm.get().email).findUnique() != null){
 				return badRequest(error.render(EMessages.get("error.title"),new ArrayList<Link>(),form(Register.class),EMessages.get("register.same_email")));
 			}
 		}
 
 		// If the form contains error's (specified by "@"-annotation in the class "Register" then this will be true.
 		if(registerForm.hasErrors()){
 			return badRequest(error.render(EMessages.get("error.title"), new ArrayList<Link>(), form(Register.class), EMessages.get("error.text")));
 		}
 
 		if(matcher.find()){
 			return badRequest(error.render(EMessages.get("error.title"), new ArrayList<Link>(), form(Register.class), EMessages.get("error.invalid_symbols")));
 		}
 
 		pattern = Pattern.compile("[^a-z._+@0-9]");
 		matcher = pattern.matcher(registerForm.get().email);
 
 		if(matcher.find()){
 			return badRequest(error.render(EMessages.get("error.title"), new ArrayList<Link>(), form(Register.class), EMessages.get("error.invalid_email")));
 		}
 
 		try{
		new SimpleDateFormat("yyyy/mm/dd").parse(registerForm.get().bday);
 		}catch(Exception e){
 			return badRequest(error.render(EMessages.get("error.title"), new ArrayList<Link>(), form(Register.class), EMessages.get("error.invalid_date")));
 		}
		
 		// Delegate create user to Authentication Manager.
 		String bebrasID = null;
 		try {
 			bebrasID = AuthenticationManager.getInstance().createUser(registerForm);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return ok(registerLandingPage.render(EMessages.get("info.success"), new ArrayList<Link>(), bebrasID));
 	}
 
 	/**
 	 * This methode is called when the users clicks on "login".
 	 * @return returns the users cookie.
 	 */
 	public static Result validate_login(String id, String password) throws Exception{
 		// We do the same check here, if the input forms are empty return a error message.
 		if(id == "" || password == "") {
 			return badRequest(EMessages.get("register.giveinfo"));
 		} else if(AuthenticationManager.getInstance().validate_credentials(id, password)){
 			String cookie = "";
 			try {
 				//generate random id to auth user.
 				cookie = Integer.toString(Math.abs(SecureRandom.getInstance("SHA1PRNG").nextInt(100)));
 
 				//set the cookie. There really is no need for Crypto.sign because a cookie should be random value that has no meaning
 				cookie = Crypto.sign(cookie);
 				response().setCookie(AuthenticationManager.COOKIENAME, cookie);
 
 			} catch (NoSuchAlgorithmException e) {
 				e.printStackTrace();
 			}
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
 		breadcrumbs.add(new Link("Dashboard", "/home"));
 
 		UserType type = AuthenticationManager.getInstance().getUser().getType();
 		if(type.equals(UserType.ANON)) {
 			return Results.redirect(routes.Application.index());
 		} else {
 			Class<?> object = Play.application().classloader().loadClass("views.html.landingPages." + LANDINGPAGES.get(type).getSimpleName() + "$");
 			Template2<User,List<Link>, Html> viewTemplate = (Template2<User,List<Link>, Html>)object.getField("MODULE$").get(null);
 			return ok(viewTemplate.render(AuthenticationManager.getInstance().getUser(), breadcrumbs));
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
