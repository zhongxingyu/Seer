 package ca.awesome;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.IOException;
 import java.sql.*;
 
 
 public class LoginController extends Controller {
 	public static String USERNAME_FIELD = "USERNAME";
 	public static String PASSWORD_FIELD = "PASSWORD";
 	public static String PASSWORD_CONF_FIELD = "PASSWORD_CONF";
 	public static String FIRST_NAME = "FIRSTNAME";
 	public static String LAST_NAME = "LASTNAME";
 	public static String ADDRESS = "ADDRESS";
 	public static String EMAIL = "EMAIL";
 	public static String PHONE = "PHONE";
 
 	public LoginController(ServletContext context, HttpServletRequest request,
 				HttpServletResponse response, HttpSession session) {
 		super(context, request, response, session);
 	}
 
 	public boolean attemptLogin() {
 		String userName = request.getParameter(USERNAME_FIELD).trim();
 		String password = request.getParameter(PASSWORD_FIELD).trim();
 
 		DatabaseConnection connection = getDatabaseConnection(context);
 		User foundUser = User.findUserByName(userName, connection);
 
 		if (foundUser != null && foundUser.getPassword().equals(password)) {
 			user = foundUser;
 			session.setAttribute("user", user);
 			return true;
 		}
 
 		return false;
 	}
 
 	public void logout() {
 		session.setAttribute("user", null);
 		user = null;
 	}
 
 	public boolean attemptChangePassword() {
 		String password = request.getParameter(PASSWORD_FIELD).trim();
 		String passwordConf = request.getParameter(PASSWORD_CONF_FIELD).trim();
 
 		if (!password.equals(passwordConf)) {
 			error = "password fields did not match";
 			return false;
 		}
 
 		DatabaseConnection connection = getDatabaseConnection(context);
 		User.updateUserPassword(user, password, connection);
 		return true;
 	}
 
 	// GET editPersonalInfo.jsp
 	public void getUpdateInfo() {
 		if (!user.loadPersonalInfo(getDatabaseConnection())) {
 			user.addEmptyPersonalInfo();
 		}
 	}
 
 	public boolean attemptUpdateInfo() {
 		String firstName = request.getParameter(FIRST_NAME).trim();
 		String lastName = request.getParameter(LAST_NAME).trim();
 		String address = request.getParameter(ADDRESS).trim();
 		String email = request.getParameter(EMAIL).trim();
 		String phone = request.getParameter(PHONE).trim();
 
 		boolean valid = true;
 		// do validation
 		valid &= validateStringLength(firstName, "first name", 24);
 		valid &= validateStringLength(lastName, "last name", 24);
 		valid &= validateStringLength(address, "address", 128);
 		valid &= validateStringLength(email, "email", 128);
 		valid &= validateStringLength(phone, "phone", 10);
 
 		if (valid == false) {
 			return false;
 		}
 
		if (!user.upsertPersonalInfo(firstName, lastName, address, email,
				phone, getDatabaseConnection())) {
			error = "Failed to edit personal info.";
			return false;
		}
		return true;
 	}
 };
