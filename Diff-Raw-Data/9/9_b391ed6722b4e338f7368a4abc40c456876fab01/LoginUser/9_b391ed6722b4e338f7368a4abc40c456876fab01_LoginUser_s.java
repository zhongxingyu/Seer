 package controllers;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import api.entities.ChoiceJSON;
 import api.entities.UserJSON;
 import api.helpers.GsonHelper;
 import api.requests.AuthenticateUserRequest;
 import api.requests.CreatePollRequest;
 import api.responses.AuthenticateUserResponse;
 import api.responses.CreatePollResponse;
 import play.libs.Crypto;
 import play.mvc.Controller;
 
 public class LoginUser extends BaseController {
 	public static String forward = "";
 	public static String pollToken = "";
 	
 	public static void index(String email) {
 		if (email == null)
 			email = "";
 		render(email);
 	}
 	
 	public static void logout() {
		APIClient.deauthenticate();
 		Application.index();
 	}
 	
 	public static void submit(String email, String password) {
 		// Validate that the question and answers are there.
 		validation.required(email);
 		validation.required(password);
 
 		// If we have an error, go to loginform.
 		if (validation.hasErrors()) {
 			params.flash();
 			validation.keep();
 			index(email);
 			return;
 		}
 		
 		// TODO: Check if the email provided belongs to an existing user
 		// If yes: Redirect the user to the login page - save the poll somewhere temporarily.
 		// If no: Create a new user and create the poll, and bind these two together.
 
 		try {
 			if (APIClient.authenticateSimple(email, password)) {
 				if (forward.equals("createpoll"))
 					CreatePoll.index("", null);
 				if (forward.equals("joinpoll"))
 					JoinPoll.index(pollToken);
 				Application.index();
 			}
 			else {
 				params.flash();
 				validation.addError("invalid", "Invalid email or password.");
 				validation.keep();
 				index(email);
 			}
 		} catch (Exception e) {
 			// It failed!
 			// TODO: Tell the user!
 			//e.printStackTrace();
 			params.flash();
 			validation.addError(null, e.getMessage());
 			validation.keep();
 			index(email);
 		}
 	}
 	
 	public static Long getCurrentUserId() {
 		if(session.get("user_id") == null) {
 			return null;
 		} else {
 			return Long.valueOf(session.get("user_id"));
 		}
 	}
 	
 	public static boolean isLoggedIn() {
 		return (session.get("user_id") != null && session.get("user_secret") != null);
 	}
 }
