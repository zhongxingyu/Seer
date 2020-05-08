 package com.vaggs.Servlets;
 
 import static com.vaggs.Utils.OfyService.ofy;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.vaggs.AirportDiagram.Airport;
 import com.vaggs.Utils.AtcUser;
 
 @SuppressWarnings("serial")
 public class TowerServ extends HttpServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException, ServletException {
 		UserService userService = UserServiceFactory.getUserService();
 		User user = userService.getCurrentUser();
 		
 		//for debugging purposes, logout via /tower?logout
 		if(req.getParameter("logout") != null) {
 			ofy().load().type(Airport.class).id("kpvd").get().removeConnectedUser(user.getNickname());
 			resp.sendRedirect(userService.createLogoutURL(req.getRequestURI()));
 			return;
 		}
 		
		if(user != null && AtcUser.GetUser(user.getNickname()) != null) {
 			req.getRequestDispatcher("/atc.jsp").forward(req, resp);
		} else {
 			resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
 		}
 	}
 }
