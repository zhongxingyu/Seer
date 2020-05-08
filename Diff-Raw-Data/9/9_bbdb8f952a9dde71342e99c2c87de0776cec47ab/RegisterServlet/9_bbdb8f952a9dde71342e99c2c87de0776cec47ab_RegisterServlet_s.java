 package servlets;
 
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 import java.text.ParseException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet implementation class RegisterServlet
  */
 @WebServlet("/RegisterServlet")
 public class RegisterServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public RegisterServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		 
 		
 		try {
 			Class.forName("org.postgresql.Driver");
 		} catch (ClassNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 	
 		String user = request.getParameter("user");
 		String password = request.getParameter("pass");
 		String email = request.getParameter("email");
 		String www = request.getParameter("www");
 		String date = request.getParameter("date");
 		String description = request.getParameter("des");
 		try {
 		
 		Register newUser = new Register(user,password,email,www,date,description); 
 		 
 		if ((((((newUser.isValid() && user.length()>=0) && password.length()!=0) && email.length()!=0 )) && (date.length()==0 || date.charAt(4)=='-'))){		 
 			
			HttpSession session = request.getSession(true); 
 			newUser.add();
					 
					 PrintWriter out = response.getWriter();
						out.print("<html><head></head><body><h3>YOU SUCCESFULLY REGISTERED TO OUR PORTAL!</h3>");
						out.print("Now you can log in to the site in this page </body></html>");
						out.print("<a href=index.jsp> LOGIN PAGE</a>"); 
 						
 		}
 					
 					 else{ 
 						 
 						 
 						    PrintWriter out = response.getWriter();
 							out.print("<html><head></head><body><h4 style=font-family:verdana;color:red;font-size:20px;> " +
 									"Something went wrong during the registration</h4>");
 							out.print("<h3 style=font-family:verdana;color:red;>" +
 									"Probably the choosen user is already in use or your email is already registered </body></html>");
 							out.print("<h3 style=font-family:verdana;color:red;> Or you haven't filled the compulsory fields</h3>"); 
 							out.print("<h3 style=font-family:verdana;color:red;> Or the inserted date is not in the right form</h3>"); 
 							out.print("<h3 style=font-family:verdana;color:red;> PLEASE RETRY</h3>");  
 						 
 							out.print("<a href=register.jsp> Go to registration page </a></body></html>");
 					
 					 }
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			
 			}
 			
 			} 
 	
 	
 		
 		
 		
 		
 		
 		
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
