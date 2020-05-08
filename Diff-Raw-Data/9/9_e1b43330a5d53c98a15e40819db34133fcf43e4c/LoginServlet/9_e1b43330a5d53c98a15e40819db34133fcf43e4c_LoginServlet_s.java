 package com.grademaster.servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.grademaster.Globals;
 import com.grademaster.auth.Authenticator;
 import com.eakjb.EakjbData.IDataStructure;
 import com.eakjb.EakjbData.Logging.*;
 
 public class LoginServlet extends HttpServlet {
 	private static final long serialVersionUID = -9088365072065846961L;
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
 		res.sendRedirect("shared/login_form.jsp");
 	}
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
 		Logger log = Globals.getLogger();
 		
 	    log.log("Login servlet running...",ErrorLevel.INFO);
 	    log.log("Login servlet is not complete yet.",ErrorLevel.WARNING);
 	    
 	    if (req.getParameter("username")==null || req.getParameter("username")=="" || req.getParameter("password")==null || req.getParameter("password")=="") {
 	    	res.sendRedirect("shared/login_form.jsp?error=Empty username or password");
 	    } else {
 	    	Authenticator auth=null;
 			try {
 				auth = new Authenticator();
 			} catch (Exception e) {
 				log.log(e);
 			}
 	    	IDataStructure user=auth.authUser(req.getParameter("username"), req.getParameter("password"), req.getParameter("type"));
 	    	if (user==null) {
 	    		res.sendRedirect("shared/login_form.jsp?error=Incorrect username or password");
 	    	} else {
 	    		req.getSession(true).setAttribute("user", user);
 	    		req.getSession(true).setAttribute("loggedIn", true);
 	    		if (req.getParameter("redirect")==null || req.getParameter("redirect")=="") {
 	    			res.sendRedirect("index.do");
 	    		} else {
 	    			res.sendRedirect(req.getParameter("redirect"));
 	    		}
 		    }
 	    }
 	}
 }
