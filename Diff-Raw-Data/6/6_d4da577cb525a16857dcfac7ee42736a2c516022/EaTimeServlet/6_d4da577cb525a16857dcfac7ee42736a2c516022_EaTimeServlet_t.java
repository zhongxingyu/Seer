 package com.bingo.eatime;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class EaTimeServlet extends HttpServlet {
 
 	private static final long serialVersionUID = -7796866550957261709L;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		String isLogin=(String) req.getSession().getAttribute("loginStatus");
 		if(isLogin==null)
			resp.sendRedirect("login.jsp");
		else if(isLogin.equals("false"))
			resp.sendRedirect("login.jsp");
 		RequestDispatcher rd = req.getRequestDispatcher("/main.jsp");
 		try {
 			rd.forward(req, resp);
 		} catch (ServletException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
