 package edu.colorado.csci3308.inventory;
 
 import java.io.IOException;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class LoginServlet
  */
 @WebServlet("/LoginServlet")
 public class LoginServlet extends HttpServlet implements Servlet{
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String usrName = request.getParameter("Username");
		if(usrName == null)
			usrName = "I don't want to talk about it";
		request.getSession().setAttribute("Username", usrName);
 		
 		String dest = "ServerList.jsp";
 		request.getRequestDispatcher(dest).forward(request, response);
 	}
 
 }
