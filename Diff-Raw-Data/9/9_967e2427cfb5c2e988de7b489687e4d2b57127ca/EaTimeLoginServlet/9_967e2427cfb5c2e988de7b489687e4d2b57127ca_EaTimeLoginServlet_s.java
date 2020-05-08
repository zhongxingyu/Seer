 package com.bingo.eatime;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 public class EaTimeLoginServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 4588543340482590495L;
 	
 	private static final Logger log = Logger.getLogger(EaTimeLoginServlet.class.getName());
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) {
 		String user = req.getParameter("user");
 		String password = req.getParameter("pwd");
 		HttpSession session = req.getSession();
 		if ((user.equals("ryan") && password.equals("crd")) || (user.equals("kevin") && password.equals("kevin"))) {
 			try {
 				String username = user;
 				session.setAttribute("loginStatus", "true");
 				session.setAttribute("user", username);
 				resp.sendRedirect("/eatime");
 			} catch (IOException e) {
 				log.log(Level.SEVERE, "Cannot redirect to /eatime.", e);
 			}
 		} else {
 			try {
 				session.setAttribute("loginStatus", "false");
 				resp.sendRedirect("/login.jsp");
 			} catch (IOException e) {
 				log.log(Level.SEVERE, "Cannot redirect to /login.jsp", e);
 			}
 		}
 	}
 
 }
