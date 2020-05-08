 package controllers;
 
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 
 import models.*;
 
 import play.libs.Mail;
 import play.mvc.Controller;
 
 import play.data.validation.Required;
 import repo.Repository;
 
 /**
  * Non-secured controller class.
  */
 public class Dummy extends Controller {
 	
 	/**
 	 * Displays the main welcome page.
 	 */
 	public static void index(){
 		if(!Security.isConnected())
 			render();
 		else
 			PageController.welcome();
 	}
 	
 	/**
 	 * Displays the form to create a new account
 	 */
 	public static void createAccount(){
 		render();
 	}
 	
 	/**
 	 * Creates a new user
 	 * @param email
 	 * @param password
 	 * @param firstName
 	 * @param lastName
 	 * @param address
 	 * @param phoneNumber
 	 * @param sex
 	 * @param code code to create the account as a physician
 	 */
 	public static void newAccount(@Required(message = "Please enter a valid email address") String email,
 			@Required(message = "Please enter a password") String password,
 			@Required(message = "Please enter your first name") String firstName,
 			@Required(message = "Please enter your last name") String lastName,
 			@Required(message = "Please enter your address")  String address,
 			@Required(message = "Please enter your phone number") String phoneNumber,
 			@Required String sex,
 			String code){
 		
     	//Check that the email isnt registered already
     	if(User.find("byUsername", email).fetch().size() > 0){
     		//Email already registered.
     		validation.addError("email", "Email already registered", "email");
     		
     	}
     	
     	if (validation.hasErrors()) {
             render("Dummy/createAccount.html", email, firstName, lastName, address, phoneNumber, code);
         }
     	
     	//If the code is correct, create user as physician
     	if(code.equals("physician")){
     		new Physician(email, password, firstName, lastName).save();
     	} else {
     		//Else, create them as a patient
     		new Patient(email, password, firstName, lastName, address, phoneNumber, sex.charAt(0)).save();
     	}
     	
     	//Authenticate them automatically
     	session.put("username", email);
     	PageController.welcome();
 	}
 	
 	/**
 	 * Reders the forgot password page
 	 */
 	public static void forgotPassword(){
 		render();
 	}
 	
 	
 	public static void sendPassword(@Required(message = "A valid email address is required") String email){
 		User user = User.find("byUsername", email).first();
 		
 		if(validation.hasErrors() || user == null){
 			render("Dummy/forgotPassword.html");
 		}
 		
 		try {
 			//Will need to set up email prefs in the conf to be able to use
 			SimpleEmail toSend = new SimpleEmail();
 			toSend.setFrom("noreply@something.com");
			toSend.addTo(email);
 			toSend.setSubject("Ultra Password");
 			toSend.setMsg("Your password to Ultra is: "+ Repository.decodePassword(user.getPassword()) );
 			Mail.send(toSend);
 		} catch (EmailException e) {
 			e.printStackTrace(System.out);
 		} 
 		render(email);
 	}
 }
