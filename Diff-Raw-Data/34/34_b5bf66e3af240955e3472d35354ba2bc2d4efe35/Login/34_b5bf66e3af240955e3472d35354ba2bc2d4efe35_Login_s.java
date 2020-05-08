 package de.raulin.rosario.helloCloud.usermanagement;
 
 import java.io.IOException;
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
 
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		final String email = (String) request.getParameter("email");
 		final String password = (String) request.getParameter("password");
 		try {
 			final User user = new User(email, password);
 			DynamoDBHelper.lookup(user);
			
 			final HttpSession s = request.getSession();
 			s.setAttribute("loggedIn", true);
 			s.setAttribute("name", user.getName());
			
 			response.sendRedirect(request.getContextPath()
 					+ "/login.jsp?success=1");
 		} catch (EmptyInputException e) {
 			response.sendRedirect(request.getContextPath()
 					+ "/login.jsp?empty=1");
 		} catch (InvalidLoginException e) {
 			response.sendRedirect(request.getContextPath()
 					+ "/login.jsp?invalid=1");
 		}
 	}
 
 }
