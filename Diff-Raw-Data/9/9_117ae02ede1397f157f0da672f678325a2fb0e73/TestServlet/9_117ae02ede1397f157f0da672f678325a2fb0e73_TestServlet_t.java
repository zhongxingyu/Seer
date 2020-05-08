 package se.chalmers.chessfeudserver;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class TestServlet
  */
 public class TestServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
     /**
      * Default constructor. 
      */
     public TestServlet() {
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("text/xml");
 		final ServletOutputStream out = response.getOutputStream();			
 			try {
				String userName = request.getParameter("username");
 		        String password = request.getParameter("password");
 				boolean authenticated = login(userName, password);
				out.print(authenticated);
 				} catch(Exception e) {
 				e.printStackTrace();
 				
 				} 
 
 			out.flush();
 			out.close();
 	}
 		
 
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		this.doGet(request, response);
 
 	}
 	
 	public boolean login(String userName, String password) {
 		if(userName == null || password == null) {
 			return false;
 		}
 		if (userName.equals("twister") && password.equals("awesomeness")) {
 			return true;
 		} else {
 			return false;
 		}
 		
 	}
 
 }
