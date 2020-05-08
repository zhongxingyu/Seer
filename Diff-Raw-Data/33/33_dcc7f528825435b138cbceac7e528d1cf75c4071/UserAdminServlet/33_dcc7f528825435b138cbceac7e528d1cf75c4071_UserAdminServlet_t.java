 package no.ntnu.idi.simplebank.servlets;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import no.ntnu.idi.simplebank.Database;
 import no.ntnu.idi.simplebank.User;
 
public class UserAdminServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -5207017716475906105L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		Database database = new Database();
 		
 		List<User> users = database.getAllUsers();
 		
 		req.setAttribute("users", users);
 		
 		req.getRequestDispatcher("/WEB-INF/Users.jsp").forward(req, resp);
 	}
 }
