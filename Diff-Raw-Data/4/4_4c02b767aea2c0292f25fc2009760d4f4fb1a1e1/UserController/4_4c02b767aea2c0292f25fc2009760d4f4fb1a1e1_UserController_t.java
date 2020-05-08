 package controllers;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import play.Logger;
 import play.mvc.Http;
 
 import controllers.APIController.NotFoundException;
 import controllers.APIController.UnauthorizedException;
 
 import models.SimpleUserAuthBinding;
 import models.User;
 import models.UserAuthBinding;
 import api.requests.AuthenticateUserRequest;
 import api.requests.CreateUserRequest;
import api.requests.UpdateUserRequest;
 import api.responses.AuthenticateUserResponse;
 import api.responses.CreateUserResponse;
 import api.responses.EmptyResponse;
 import api.responses.ReadUserResponse;
 import api.responses.UpdateUserResponse;
 import api.entities.UserJSON;
 import api.helpers.GsonHelper;
 
 /**
  * Class that manages the responses in the API for Users.
  * @author OpenARMS Service Team
  *
  */
 public class UserController extends APIController {
 	/**
 	 * Method that authenticates the user.
 	 * It generates new secret for the user.
 	 * Used only when user is logging in.
 	 */
 	public static void authenticate() {
 		try {
 			// Takes the UserJSON from the http body
 			AuthenticateUserRequest req = GsonHelper.fromJson(request.body, AuthenticateUserRequest.class);
 			User user = User.find("byEmail", req.user.email).first();
 			if (user != null && user.userAuth instanceof SimpleUserAuthBinding) {
 				user = SimpleAuthBackend.authenticate(user);
 				if (user != null) {
 				    //Creates the UserJSON Response.
 					AuthenticateUserResponse response = new AuthenticateUserResponse(user.toJson());
 					renderJSON(response.toJson());
 				} else {
 					throw new UnauthorizedException();
 				}
 			} else {
 				throw new NotFoundException();
 			}
 		} catch (Exception e) {
 			//renderText(e.getMessage());
 			renderException(e);
 		}
 	}
 	
 	/**
 	 * Method that authorizes the user.
 	 * It keeps user logged in the system.
 	 * Used every time user sends any request.
 	 */
 	public static boolean authorize() {
 		User user = AuthBackend.getCurrentUser();
 		return (user != null);
 	}
 	
 	/**
 	 * Method that saves a new User in the DataBase.
 	 */
 	public static void create() {
 		try {	
 	    	// Takes the UserJSON and creates a new User object with this UserJSON.
 	        CreateUserRequest req = GsonHelper.fromJson(request.body, CreateUserRequest.class);
 	        User user = User.fromJson(req.user);
 	        if (user.userAuth instanceof SimpleUserAuthBinding)
 	        	((SimpleUserAuthBinding)user.userAuth).save();
 	        user.save();
 	           
 	        //Creates the UserJSON Response.
 	        CreateUserResponse response2 = new CreateUserResponse(user.toJson());
 	    	String jsonResponse = GsonHelper.toJson(response2);
 	    	System.out.println(jsonResponse);
 	    	renderJSON(jsonResponse);
 		} catch (Exception e) {
 			e.printStackTrace();
 			renderException(e);
 		}
 	}
 	
 	/**
 	 * Method that gets a User from the DataBase.
 	 */
 	public static void retrieve () {
 		try {
 			String userid = params.get("id");
 	
 			//Takes the User from the DataBase.
 			User user = User.find("byID", userid).first();
 	
 			if (user == null) {
 				throw new NotFoundException();
 			}
 			
 	        //If current user is not the same as the poll creator or there is no current user, throws an exception
 			User u = AuthBackend.getCurrentUser();
 			if (u == null || user.id != u.id) {
 		        throw new UnauthorizedException();
 		    }
 			
 			//Creates the UserJSON Response.
 			ReadUserResponse response = new ReadUserResponse(user.toJson());
 			String jsonResponse = GsonHelper.toJson(response);
 			renderJSON(jsonResponse);
 			
 		} catch (Exception e) {
 			renderException(e);
 		}
 	}
 	
 	/**
 	 * Method that edits a User already existing in the DB.
 	 */
 	public static void edit () {
 		try {
 			String userid = params.get("id");
 	
 			//Takes the User from the DataBase.
 			User originalUser = User.find("byID", userid).first();
 			
 			if (originalUser == null) {
 				throw new NotFoundException();
 			}
 			
 	        //If current user is not the same as the poll creator or there is no current user, throws an exception
 			User u = AuthBackend.getCurrentUser();
 			if (u == null || originalUser.id != u.id) {
 		        throw new UnauthorizedException();
 		    }
 			
 			//Takes the edited UserJSON and creates a new User object with this UserJSON.
			UpdateUserRequest req = GsonHelper.fromJson(request.body, UpdateUserRequest.class);
             User editedUser = User.fromJson(req.user);
             
             //Changes the old fields for the new ones.
             if (editedUser.name != null)
             	originalUser.name = editedUser.name;
             if (editedUser.email != null)
             	originalUser.email = editedUser.email;
             if (editedUser.secret != null)
             	originalUser.secret = editedUser.secret;
             if (editedUser.userAuth != null) {
             	// Compares originalAuth with editedAuth
             	if (editedUser.userAuth.getClass().toString().equals(originalUser.userAuth.getClass().toString())) {
             		// Check authentication method
             		if (originalUser.userAuth instanceof SimpleUserAuthBinding) {
             			Long idAuth = ((SimpleUserAuthBinding)originalUser.userAuth).id;
             			SimpleUserAuthBinding originalAuth = (SimpleUserAuthBinding)SimpleUserAuthBinding.find("byID", idAuth).fetch().get(0);
             			originalAuth.password = ((SimpleUserAuthBinding)editedUser.userAuth).password;
             			originalAuth.save();
             		}
             	}
             }
             originalUser.save();
             
             
             //Creates the PollJSON Response.
             UpdateUserResponse response = new UpdateUserResponse(originalUser.toJson());
         	String jsonResponse = GsonHelper.toJson(response);
         	renderJSON(jsonResponse);
             
 		} catch (Exception e) {
 			renderException(e);
 		}
 	}
 	
 	/**
 	 * Method that deletes a User existing in the DataBase.
 	 */
 	public static void delete () {
 		try {
 			String userid = params.get("id");
 	
 			//Takes the User from the DataBase.
 			User user = User.find("byID", userid).first();
 			
 			if (user == null) {
 				throw new NotFoundException();
 			}
 			
 	        //If current user is not the same as the poll creator or there is no current user, throws an exception
 			User u = AuthBackend.getCurrentUser();
 			if (u == null || user.id != u.id) {
 		        throw new UnauthorizedException();
 		    }
 			
 			//Deletes the Authentication from the DataBase.
 			if (user.userAuth instanceof SimpleUserAuthBinding) {
     			Long idAuth = ((SimpleUserAuthBinding)user.userAuth).id;
     			SimpleUserAuthBinding auth = (SimpleUserAuthBinding)SimpleUserAuthBinding.find("byID", idAuth).fetch().get(0);
     			auth.delete();
     		}
 			//Deletes the User from the DataBase and creates an empty UserJSON for the response.
 			user.delete();
 
 			renderJSON(new EmptyResponse().toJson());
 			
 		} catch (Exception e) {
 			renderException(e);
 		}
 	}
 }
