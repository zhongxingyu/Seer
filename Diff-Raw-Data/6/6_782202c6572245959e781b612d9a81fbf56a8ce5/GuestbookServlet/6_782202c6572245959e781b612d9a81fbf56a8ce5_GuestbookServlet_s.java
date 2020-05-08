 package com.tutorial.appengine;
 
 import java.io.IOException;
 import javax.servlet.http.*;
 //import com.google.appengine.api.users.*;
 
 @SuppressWarnings("serial")
 public class GuestbookServlet extends HttpServlet {
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		
 		String userPath = req.getServletPath();
 		
 		if(userPath.equals("/guestbook")){
 			
 			try{
 				req.getRequestDispatcher("/jsp/guestbook.jsp").forward(req, resp);
 			}catch(Exception ex){
 				ex.printStackTrace();
 			}
 			
 		}
 		/*UserService userService = UserServiceFactory.getUserService();
 		User user = userService.getCurrentUser();
 		
 		if(user != null){
 			resp.setContentType("text/plain");
 			resp.getWriter().println("Hello, " + user.getNickname());
 		} else{
 			resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
 		}
 		*/
 		//resp.setContentType("text/plain");
 		//resp.getWriter().println("Hello, this is guestbook!");
 	}
 }
