 package no.ntnu.idi.simplebank.servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import no.ntnu.idi.simplebank.Database;
 
 public class LoginServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1613861886766248905L;
 	
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		req.getRequestDispatcher("/WEB-INF/Login.jsp").forward(req, resp);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		
 		String username = request.getParameter("username");
 		String password = request.getParameter("password");
 		String next = request.getParameter("next");
 		
 		Database database = new Database();
 		boolean authenticated = database.authenticate(username, password);
 		
 		if (!authenticated) {
 			request.setAttribute("login_failed", new Boolean(true));
 			request.getRequestDispatcher("/WEB-INF/Login.jsp").forward(request, response);
 		} else {
 			HttpSession session = request.getSession(true);
 			session.setAttribute("logged_in_user", username);
 			if (next.equals("null")) {
 				response.sendRedirect(request.getContextPath() + "/Accountoverview");
 			}
 			response.sendRedirect(next);
 		}
 		
 	}
 	
 	
 
 }
