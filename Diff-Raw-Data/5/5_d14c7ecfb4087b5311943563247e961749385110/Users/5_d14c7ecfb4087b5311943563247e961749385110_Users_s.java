 package controllers;
 
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Calendar;
 import java.util.List;
 
 import models.User;
 import play.Logger;
 import play.data.validation.Valid;
 import play.mvc.Controller;
 import play.mvc.With;
 
 @With(Secure.class)
 public class Users extends Main {
 
 	public static void create() {
 		render("@show");
 	}
 
 	@Check("admin")
 	public static void show(String login) {
 		User user = User.find("byLogin", login).first();
 		render(user);
 	}
 	
	@Check("admin")
 	public static void list() {
 		List<User> users = User.findAll();
 		render(users);
 	}
 	
 	public static void showMe() {
 		User user = User.find("byLogin", Security.connected()).first();
 		render("@show",user);
 	}
 	
 	private static String hash(String password) throws NoSuchAlgorithmException {
 		MessageDigest sha1;
 		sha1 = MessageDigest.getInstance("SHA1");
 		sha1.update(password.getBytes());
 		BigInteger hash = new BigInteger(1, sha1.digest());
 		return hash.toString(16).toUpperCase();
 		
 	}
 	
 	public static void save(String new_password, Boolean isAdmin, @Valid User user) {
 
 		if (new_password != null && !new_password.isEmpty() ) {
 			try {
 				user.hashed_password = hash(new_password); 		
 			} catch (NoSuchAlgorithmException e) {
 				validation.addError("hashed_password", "Hash failed");
 			}
 		}
 
 		user.admin = isAdmin;
 		
 		if (validation.hasErrors()) {
 			params.flash(); // add http parameters to the flash scope
 			// validation.keep(); // keep the errors for the next request
 			user.hashed_password = null;
 			render("@show");
 		}
 
 		if (user.created_on == null) {
 			user.created_on = Calendar.getInstance().getTime();
 		}
 		user.save();
		render("@show", user);
 
 	}
 
 	public static void delete(@Valid User user) {
 		user.delete();
 	}
 
 	public static User connect(String username, String password) {
 		User user = User.find("byLogin", username).first();
 		if (user != null) {
 			try {
 				if (user.hashed_password.equals(hash(password))) {
 					user.last_login_on = Calendar.getInstance().getTime();
 					user.save();
 				} else {
 					user = null;
 				}
 			} catch (NoSuchAlgorithmException e) {
 				user = null;
 			}
 			
 		}
 
 		return user;
 	}
 
 }
