 package controllers;
 import play.mvc.Controller;
 import java.util.regex.*;
 
 
 import com.google.gson.JsonParseException;
 
 public class RegisterUser extends BaseController {
 	public static void getdata(String name, String email, String passw, String confpassw) throws Exception {
 		boolean changed = false;
 		if(!checkpassw(passw, confpassw)){
 			validation.addError("passwDontMatch", "Passwords doesn't match");
 			changed = true;
 		}
 		if(!checkemail(email)){
 			validation.addError("email", "Wrong email");
 			changed = true;
 		}
 		if(!validatePass(passw)){
 			validation.addError("validatePass", "Password is short");
 			changed = true;
 		}
 
 		if(!changed){
 			UserJSON uj = new UserJSON();
 			uj.name = "Name";
 			uj.email = email;
 			uj.backend = "class models.SimpleUserAuthBinding";
 			uj.attributes = new HashMap<String, String>();
 			uj.attributes.put("password", confpassw);
 			CreateUserResponse response = (CreateUserResponse)APIClient.send(new CreateUserRequest(uj));
 			if  (response.statusCode == 200)
 				success();
 			else {
 				validation.addError("emailTaken", "This email is already taken");
 				validation.keep();
 				showform();
 			}
 		} else {
 			validation.keep();
 			showform();
 		}
 	}
 	public static void showform(){
 		render();
 	}
 	public static void success(){
 		render();
 	}
 
 	//does passwords match?
 	public static boolean checkpassw(String passw, String confpassw){
 		return passw.equals(confpassw);
 	}
 	//check wrong symbols and length
 	public static boolean validatePass(String passw){
 		int length = passw.length();
 		return (length > 4);
 	}
 	public static boolean checkemail(String email) {
 		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
 		Matcher m = p.matcher(email);
 		boolean result = m.matches();
 		return result;
 	}
 }
