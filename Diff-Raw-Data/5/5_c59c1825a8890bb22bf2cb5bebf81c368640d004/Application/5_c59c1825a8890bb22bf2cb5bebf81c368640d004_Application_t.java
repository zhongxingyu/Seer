 package controllers;
 
 import play.*;
 import play.data.validation.Equals;
 import play.data.validation.Required;
 import play.mvc.*;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import org.joda.time.LocalDate;
 
 import models.*;
 
 /** 
  * 
  * @author niau
  * Protect Main Application Controller 
  * To activate the protection e.g authentication please remove the comment from the following route 
  * *      / =               module:secure
  */
 //@With(Secure.class)
 public class Application extends Controller {
 
     public static void index() {
         calendarMonth();
     }
     
     public static void createBirthdayEvent()
     {
     	render();
     }
     
     public static void createBirthdayEventForm(String name, String date, String birthdayPerson, Boolean surprise)
     {
     	validation.required(name);
     	validation.required(date);
     	validation.required(birthdayPerson);
     	validation.required(surprise);
     	SimpleDateFormat f = new SimpleDateFormat("MM/dd/yyyy");
 		try {
 			f.parse(date);
 		} catch (ParseException e) {
 			validation.addError("date", "Date is not in MM/DD/YYYY format");
 		}
 	    if(validation.hasErrors()) {
 	          params.flash(); // add http parameters to the flash scope
 	          validation.keep(); // keep the errors for the next request
	          createBirthdayEvent();
 	    }
     	//BirthdayEvent be = new BirthdayEvent(name, date, birthdayPerson, surprise);
     	//be.save();
     	//Put the view of the confirmation page
		index();
     }
     
     /**
      * This method is responsible for rendering the Calendar monthly view..  
      */
     public static void calendarMonth()
     {
     	render();
     }
 
     ///TODO validation hash password email
     /**
      * Get: register
      * direct to register form
      */
     public static void register() {
     	render();
     }
     /**
      * Post: process register form
      */
     public static void registerForm(
     		@Required String username, 
     		@Required String firstname,
     		@Required String lastname, 
     		@Required String email,
     		@Required String password, 
     		@Required 
     		@Equals("password") String confirmPassword, 
     		String button) {
     	
     	if (button.equals("Cancel")) {
     		index();
     		return;
     	}
     	
     	// form validation	
     	if (validation.hasErrors()) {
     		params.flash();
     		validation.keep();
     		register();
     	}
     	else {
     		User user = new User(username, firstname, lastname, email, password, 0);
     		user.save();
     		///TODO
     		// send Email 
     		
     		index();
     	}
     }
   
 }
