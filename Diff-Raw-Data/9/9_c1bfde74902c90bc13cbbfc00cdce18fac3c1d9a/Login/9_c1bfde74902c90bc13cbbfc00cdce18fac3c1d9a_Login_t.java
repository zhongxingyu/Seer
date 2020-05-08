 package Fabflix;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 public class Login extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	public Login() {
 		super();
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession();// Get client session
 
 		String email = request.getParameter("email");
 		String password = request.getParameter("password");
 
 		if (!validUser(request, email, password)) {
 			session.setAttribute("login", false);
 			response.sendRedirect("login.jsp");
 		} else {
 			session.setAttribute("login", true);
 			session.setAttribute("user.login", email);
 			ShoppingCart.initCart(request, response);
 			try {
 				String target = (String) session.getAttribute("user.dest");
 				// retrieve address if user goes to a page w/o logging in
 				if (target != null) {
 					session.removeAttribute("user.dest");
 					// redirect to page the user was originally trying to go to
 					response.sendRedirect(target);
 					return;
 				}
 			} catch (Exception ignored) {
 			}
 
 			// Couldn't redirect to the target. Redirect to the site's homepage.
 			response.sendRedirect("");
 		}
 
 	}
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
 			request.getSession().setAttribute("title", "Login");
 			response.sendRedirect("login.jsp");
 	}
 
 	private boolean validUser(HttpServletRequest request, String email, String password) {
 		// Validate user
 		try {
 			Connection dbcon = Database.openConnection();
 			HttpSession session = request.getSession();// Get client session
 			boolean valid = false;
 
 			Statement statement = dbcon.createStatement();
 			String query = "SELECT * FROM employees e WHERE email = '" + email + "' AND password = '" + password + "'";
 			ResultSet rs = statement.executeQuery(query);
 			
 			// if employee exists with that password, then login as admin
 			if (rs.next()) {
 				session.setAttribute("user.name", rs.getString("fullname"));
 				session.setAttribute("isAdmin", true);
 				valid = true;
 			} else {
 				statement = dbcon.createStatement();
 				query = "SELECT * FROM customers c WHERE email = '" + email + "' AND password = '" + password + "'";
 				rs = statement.executeQuery(query);
 				
 				// if person exists with that password
 				if (rs.next()) {
 					session.setAttribute("user.name", rs.getString("first_name") + " " + rs.getString("last_name"));
 					session.setAttribute("user.id", rs.getString("id"));
 					session.setAttribute("isAdmin", false);
 					valid = true;
 				}
 			}
 			
 			dbcon.close();
 			return valid;
 
 		} catch (SQLException ex) {
 			System.out.println("<HTML><HEAD><TITLE>MovieDB: Error</TITLE></HEAD><BODY>");
 			while (ex != null) {
 				System.out.println("SQL Exception:  " + ex.getMessage());
 				ex = ex.getNextException();
 			} // end while
 			System.out.println("</BODY></HTML>");
 		} // end catch SQLException
 		catch (java.lang.Exception ex) {
 			System.out.println("<HTML>" + "<HEAD><TITLE>" + "MovieDB: Error" + "</TITLE></HEAD>\n<BODY>" + "<P>SQL error in doGet: " + ex.getMessage() + "<br>"
 					+ ex.toString() + "</P></BODY></HTML>");
 		}
 
 		return false;
 	}
 
 	// if not logged in, redirect to login page
 	public static boolean kickNonUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		HttpSession session = request.getSession();
 		Boolean login = (Boolean) session.getAttribute("login");
 
 		// Check login
 		if (login == null || !login) {
 			String URL = request.getRequestURL().toString();
 			String qs = request.getQueryString();
 			if (qs != null) {
 				URL += "?" + qs;
 			}
 			// Save destination till after logged in
 			session.setAttribute("user.dest", URL);
 			// send to login page if not logged in
 			response.sendRedirect("login");
 			return true;
 		}
 		return false;
 	}
 	
 	public static boolean kickNonAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		HttpSession session = request.getSession();
 		Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
 		
 		if (isAdmin == null || !isAdmin) {
			response.sendRedirect("Home");
 			return true;
 		}
 		return false;
 	}
 }
