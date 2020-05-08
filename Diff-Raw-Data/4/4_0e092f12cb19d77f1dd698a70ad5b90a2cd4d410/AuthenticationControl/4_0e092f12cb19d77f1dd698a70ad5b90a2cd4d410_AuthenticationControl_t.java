 package elfville.server.controller;
 
 import javax.crypto.SecretKey;
 
 import elfville.protocol.Response.Status;
 import elfville.protocol.utils.SharedKeyCipher;
 import elfville.protocol.SignInRequest;
 import elfville.protocol.Response;
 import elfville.protocol.SignUpRequest;
 import elfville.server.CurrentUserProfile;
 import elfville.server.SecurityUtils;
 import elfville.server.model.*;
 
 /* 
  * Controls sign in, sign up
  */
 public class AuthenticationControl extends Controller {
 
 	public static Response signIn(SignInRequest r, CurrentUserProfile currentUser) { 
 
 		Response resp= new Response(Status.FAILURE);
 		User user = database.userDB.findByUsernameHashedPassword(r.getUsername(), r.getPassword());
 
 		if (user == null) {
 			return resp;
 		}
 		if (!logInUser(user, r, currentUser)) {
 			return resp;
 		}
 
 		resp= new Response(Status.SUCCESS);
 		return resp;
 	}
 
 	private static boolean logInUser(User user, SignInRequest r, CurrentUserProfile currentUser) {
 		long currTime = System.currentTimeMillis();
 		if (!user.laterThanLastLogin(currTime) || !user.laterThanLastLogout(currTime)) {
 			return false;
 		}
 		user.setLastLogin(currTime);
 
 		// sign the user in
 		currentUser.setLastLogin(currTime);
 		currentUser.setNonce(r.getNonce());
 		currentUser.setCurrentUserId(user.getModelID());
 
 		return true;
 	}
 
 	public static void signOut(CurrentUserProfile currentUser) {
 		User user = database.userDB.findUserByModelID(currentUser.getCurrentUserId());
 		user.setLastLogout(System.currentTimeMillis());
 	}
 
 	public static Response signUp(SignUpRequest r,
 			CurrentUserProfile currentUser) {
<<<<<<< HEAD
 
 		Response resp= new Response(Status.FAILURE);
 		User user = database.userDB.findByUsername(r.getUsername());
 
 		//check to see if user already exists
 		if (user != null) {
			return new Response(Status.FAILURE, "Username already exists");
 		}
 
 		Elf elf = new Elf(r.getUsername(), r.description);
 		elf.save();
 		user = new User(elf, r.getUsername());
 		String hashedPassword;
 		try {
 			hashedPassword = SecurityUtils.generateRandomPepper(r.getPassword());
 		} catch (Exception e) {
 			e.printStackTrace();
 			return resp;
 		}
 		user.setPassword(hashedPassword);
 
 		if (!logInUser(user, r, currentUser)) {
 			return resp;
 		}
 
 		user.save();
 		resp = new Response(Status.SUCCESS);
 		return resp;
 	}
 
 }
