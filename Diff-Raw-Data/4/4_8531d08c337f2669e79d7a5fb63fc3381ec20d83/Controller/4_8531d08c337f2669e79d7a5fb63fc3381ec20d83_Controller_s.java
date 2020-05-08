 package ca.awesome;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.IOException;
 import java.sql.*;
 import java.text.*;
 
 
 public class Controller {
 	// view-accessible fields
 	public User user = null;
 	public String error = null;
 	
 	protected ServletContext context;
 	protected HttpServletRequest request;
 	protected HttpServletResponse response;
 	protected HttpSession session;
 
 	public Controller(ServletContext context, HttpServletRequest request,
 				HttpServletResponse response, HttpSession session) {
 		this.context = context;
 		this.request = request;
 		this.response = response;
 		this.session = session;
 
 		this.user = (User)session.getAttribute("user");
 		this.error = null;
 
 		DatabaseConnection.initialize(context);
 	}
 
 	public boolean userIsLoggedIn() {
 		return this.user != null;
 	}
 
 	public boolean userIsAdmin() {
 		return user.getType() == User.ADMINISTRATOR_T;
 	}
 		
 	/*
 	 * Checks whether a users is logged in,
 	 * if not, redirects to login page.
 	 * returns true if the user is logged in.
 	 */
 	public boolean requireLogin() {
 		if (user == null) {
 			try {
 				response.sendRedirect("login.jsp");
 			} catch (IOException exception) {
 				throw new RuntimeException("failed to redirect to login.jsp",
 							exception);
 			}
 			return false;
 		}
 		return true;
 	}
 
 	/*
 	 * Checks whether a user is logged in as admin.
 	 * If not, gives 403 forbidden error.
 	 * returns true if the user is logged in as admin.
 	 */
 	public boolean requireAdmin() {
 		if (!requireLogin()) {
 			return false;
 		}
 
 		if (!userIsAdmin()) {
 			userIsForbidden();
 			return false;
 		}
 		return true;
 	}
 
 	public void userIsForbidden() {
 		try {
 			// 403 == forbidden error
 			response.sendError(403);
 		} catch (IOException exception) {
 			throw new RuntimeException("Failed to send 403 error", exception);
 		}
 	}
 
 	public boolean hasError() {
 		return error != null;
 	}
 
 	public boolean requestIsPost() {
 		return "POST".equalsIgnoreCase(request.getMethod());
 	}
 
 	public String formatDate(java.util.Date date) {
 		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		StringBuffer buffer = new StringBuffer();
		format.format(date, buffer, null);
		return buffer.toString();
 	}
 
 	public Date parseDate(String text) {
 		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
 		return new Date(format.parse(text, new ParsePosition(0)).getTime());
 	}
 
 	protected boolean validateStringLength(String input, String name, int len) {
 		if (input.length() > len) {
 			if (error == null) {
 				error = "";
 			}
 
 			error += name + " cannot exceed " + len + " characters.";
 			return false;
 		}
 		return true;
 	}
 
 	protected DatabaseConnection getDatabaseConnection() {
 		return getDatabaseConnection(this.context);
 	}
 
 	protected DatabaseConnection getDatabaseConnection(ServletContext context) {
 		DatabaseConnection connection = new DatabaseConnection();
 		if (!connection.connect()) {
 			connection.close();
 			throw new RuntimeException("Failed to connect to database");
 		}
 		return connection;
 	}
 }
