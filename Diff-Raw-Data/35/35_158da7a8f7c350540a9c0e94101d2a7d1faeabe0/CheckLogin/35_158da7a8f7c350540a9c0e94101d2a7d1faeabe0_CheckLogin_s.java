 package myservs;
 
 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 /**
  * Servlet implementation class CheckLogin
  */
 public class CheckLogin extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
     /**
      * Default constructor. 
      */
     public CheckLogin() {
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		response.setCharacterEncoding("UTF-8");
 		request.setCharacterEncoding("UTF-8");
 		
 		Auxiliary auxPoint = new Auxiliary();
 		String email=request.getParameter("email");
 		String paramPassword=request.getParameter("password");
 		String password=auxPoint.shaDigest(paramPassword);
 
 		DatabaseMethods point = new DatabaseMethods();
 		String[] userCredentials=point.identifyUser(email, password);
 
 		if (userCredentials[0]==null){
 			HttpSession userSession=request.getSession(true);
 			userSession.setMaxInactiveInterval(10);
 			userSession.setAttribute("login", "0");
 			userSession.setAttribute("falseLogin", "1");
 		}else{
 			//Set the session attributes
 			
 			HttpSession userSession=request.getSession(true);
 			userSession.setMaxInactiveInterval(86400);
 			userSession.setAttribute("login", "1");
 			userSession.setAttribute("falseLogin", "0");
 			userSession.setAttribute("name", userCredentials[1]);
 			userSession.setAttribute("surname", userCredentials[2]);
 			userSession.setAttribute("email", userCredentials[3]);
 			userSession.setAttribute("password", userCredentials[4]);
 			userSession.setAttribute("id", userCredentials[5]);
			userSession.setAttribute("telephone", userCredentials[6]);
			userSession.setAttribute("address", userCredentials[7]);
			userSession.setAttribute("postcode", userCredentials[8]);
			userSession.setAttribute("isadmin", userCredentials[9]);
 		}	
 		RequestDispatcher rd=getServletContext().getRequestDispatcher("/index.jsp");
 		if(rd!=null){
 			rd.forward(request, response);
 		}
 	}
 
 }
