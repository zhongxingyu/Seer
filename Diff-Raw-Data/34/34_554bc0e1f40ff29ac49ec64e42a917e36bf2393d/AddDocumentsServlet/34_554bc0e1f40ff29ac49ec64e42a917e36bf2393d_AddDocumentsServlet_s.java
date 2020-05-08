 package org.cvut.wa2.projectcontrol;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 public class AddDocumentsServlet extends HttpServlet{
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		UserService userService = UserServiceFactory.getUserService();
		
 		RequestDispatcher disp = null;
 		if (userService.isUserLoggedIn()) {
 			
 		}else{
 			disp = req.getRequestDispatcher("/projectcontrol");
 			disp.forward(req, resp);
 		}
 		
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		doGet(req, resp);
 	}
 
 	
 	
 }
