 package controllers;
 
 import java.security.MessageDigest;
 import java.security.*;
 import java.io.*;
 import java.math.BigInteger;
 import models.User;
 import models.Student;
 import java.util.*;
 import models.Tutor;
 import play.data.DynamicForm;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.api.mvc.Session;
 import views.html.*;
 import com.typesafe.plugin.MailerAPI;
 import com.typesafe.plugin.MailerPlugin;
 import com.avaje.ebean.Query;
 import play.data.*;
 import static play.data.Form.*;
 
 public class Application extends Controller {
 	Form<Student> studentForm = form(Student.class);
 	Form<Tutor> tutorForm = form(Tutor.class);
 
 
 	public static Result loginRoute(){
 
 		DynamicForm requestData = form().bindFromRequest();
 		double type = Double.parseDouble(requestData.get("type"));
 
 		// Student log in
 		if(type == 1){
 			String username = requestData.get("username");
 			String password = requestData.get("password");
 			studentLogin(username, password);
 			List<Tutor> emptyList = Collections.<Tutor>emptyList();
       		return ok(search.render(emptyList));
 
 		// Student register
 		}else if(type == 2){
 			String username = requestData.get("username");
 			String password = requestData.get("password");
 			String fullName = requestData.get("fullName");
 			String email = requestData.get("email");
 			studentRegister(username, password, fullName, email);
 			List<Tutor> emptyList = Collections.<Tutor>emptyList();
       		return ok(search.render(emptyList));
 
 		// Tutor sign in
 		}else if(type == 3){
 			String username = requestData.get("username");
 			String password = requestData.get("password");
 			tutorLogin(username, password);
 			Query<Tutor> tutorResults  = Tutor.find.where().contains("username", username).orderBy("rating");
         	List<Tutor> tutors = tutorResults.findList();
         	Tutor tutor = tutors.get(0);
       		return ok(profile.render(tutor, 1));
 
 		// Tutor register
 		}else if(type == 4){
 			String username = requestData.get("username");
 			String password = requestData.get("password");
 			String fullName = requestData.get("fullName");
 			String email = requestData.get("email");
 			tutorRegister(username, password, fullName, email);
 			Query<Tutor> tutorResults  = Tutor.find.where().contains("username", username).orderBy("rating");
        	 	List<Tutor> tutors = tutorResults.findList();
         	Tutor tutor = tutors.get(0);
 			return ok(profile.render(tutor, 1));
 
 		}
 		return unauthorized("Oops, you are not connected");
 
 	}
 
 	/**
 	 *
 	 * @return  if user logged in
 	 */
 	public static boolean isLoggedIn(){
 		String user = session("connected");
 		if(user==null){
 			return false;
 		}
 		else{
 			return true;
 		}
 	}
 
 	public static String loggedUser(){
 		String username = session("connected");
 		return username;
 	}
 
 
 	/**
 	 *
 	 * @return  index Page
 	 */
 
 	public static Result index() {
   	String user = session("connected");
     if(user != null) {
     	//go to the users homepage
 			if(Tutor.findTutor(user)!=null){
 				//Return Tutor Homepage
 				Tutor tutor = Tutor.findTutor(user);
 				return ok(profile.render(tutor, 1));
 			} else{
 				List<Tutor> emptyList = Collections.<Tutor>emptyList();
       			return ok(search.render(emptyList));
 			}
     } else {
     	//show signup or login
       return ok(index.render("Welcome"));
     }
   }
 
 	/**
 	 *
 	 * @return Tutor Home
 	 */
 
 /*
 	 public static Result TutorHome(){
 		 if(!isLoggedIn()){
 			 return redirect("/");
 		 }
 		 //
 		 //return ok(signedInTutorMain.render());
 		 return ok("TutorHOME");
 	 }
 	public static Result StudentHome(){
 		if(!isLoggedIn()){
 			return redirect("/");
 		}
 		String user = session("connected");
 		//
 		return ok("StudentHOME");
 	}
 	*/
 	/**
 	 *
 	 * @return  to Index page with log in info for student
 	 */
 
   public static void studentLogin(String username, String password){
 		if(Student.authenticate(username, password)){
			session("connected",Student.findStudent(username).getUsername());
 			
 		}
 		
   }
 
 	/**
 	 *
 	 * @return  to Index page of tutor
 	 */
 
   public static void tutorLogin(String username, String password){
		if(Tutor.authenticate(username, password)){
 			session("connected",Tutor.findTutor(username).getUsername());
 			
 		}
 		
   }
 
 	/**
 	 *
 	 * @return to Student Homepage
 	 */
 
   public static void studentRegister(String username, String password, String fullName, String email) {
   	DynamicForm requestData = form().bindFromRequest();
   	/*String username = requestData.get("username");
   	String email = requestData.get("email");
 		String fname = requestData.get("fname");
 		String lname = requestData.get("lname");
 		String fullName = fname+ " "+lname;
 		String password = requestData.get("password");*/
 		//TODO fix this
 		
 		
   	if(Student.existsStudent(username,email)){
   		index();
   	}
   	else{
   		Student user = new Student();
   		user.setUsername(username);
   		user.setEmail(email);
 			user.setName(fullName);
 			String salt  = User.saltGenerate();
 			user.setSalt(salt);
 			user.setPwhash(User.encrypt(password,salt));
 
 			if(user.validate()){
 				Student.create(user);
 				session("connected", username);
 				index();
 			}
   		
   	}
   }
 
 	/**
 	 *
 	 * @return responseToTutorRegistrationAttempt
 	 */
   public static void tutorRegister(String username, String password, String fullName, String email) {
   	  	
 				//Validate Data
   	  	if(Tutor.existsTutor(username,email)){
   	  		 index();
   	  	}
   	  	else{
   	  		Tutor user = new Tutor();
   	  		user.setUsername(username);
   	  		user.setEmail(email);
 					user.setName(fullName);
 					String salt  = User.saltGenerate();
 					user.setSalt(salt);
 					user.setPwhash(User.encrypt(password,salt));
 					if(user.validate()){
 						Tutor.create(user);
 						session("connected",username);
 						 index();
 					}
   	  		
   	  	}
   }
 
 	/**
 	 *
 	 * @return mainSignedOutPage
 	 */
 
 	public static Result logout(){
 		session().clear();
 		//Go to log out page
 		return  ok(index.render("Welcome"));
 	}
 /**
    * Sends an email to the specified recipients
    * 
    * @param emailSubject: The subject of the email
    * @param emailRecipient: The recipient of the email
    * @param emailHtml: The html text contained in the email
    */
 
   public static void sendEmail(String emailSubject, String emailRecipient, String emailHtml) {
     sendEmail(emailSubject, Arrays.asList(emailRecipient), emailHtml);
   }
 
   /**
    * Sends an email to the specified recipients
    * 
    * @param emailSubject: The subject of the email
    * @param emailRecipients: The recipients of the email
    * @param emailHtml: The html text contained in the email
    */
 
   public static void sendEmail(String emailSubject,
       List<String> emailRecipients, String emailHtml) {
     MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
     mail.addFrom("Tutor.me Mailing Robot <tutor.me.mailer@gmail.com>");
     mail.setSubject(emailSubject);
     for (String emailRecipient : emailRecipients) {
       mail.addRecipient(emailRecipient);
     }
     mail.sendHtml(emailHtml);
   }
 }
