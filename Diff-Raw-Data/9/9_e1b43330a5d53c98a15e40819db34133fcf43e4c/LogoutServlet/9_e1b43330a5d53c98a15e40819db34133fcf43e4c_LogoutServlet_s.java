 package com.grademaster.servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.grademaster.Globals;
 import com.eakjb.EakjbData.Logging.*;
 
 public class LogoutServlet extends HttpServlet {
 	private static final long serialVersionUID = -9088365072065846961L;
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
 		Logger log = Globals.getLogger();
 		
 	    log.log("Logout servlet started.",ErrorLevel.INFO);
 	    
		req.getSession().setAttribute("loggedIn",false);
		req.getSession().setAttribute("user",null);
 	    
 		if (req.getParameter("redirect")==null || req.getParameter("redirect")=="") {
 	    	res.sendRedirect("index.do");
 	    } else {
 	    	res.sendRedirect(req.getParameter("redirect"));
 	    }
 	}
 }
