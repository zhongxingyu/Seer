 package cyberprime.servlets;
 
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import cyberprime.entities.Clients;
 import cyberprime.entities.dao.ClientsDAO;
 import cyberprime.util.Algorithms;
 import cyberprime.util.EmailSender;
 import cyberprime.util.FileMethods;
 
 
 /**
  * Servlet implementation class Register
  */
 //@WebServlet("/Register")
 public class Register extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public Register() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		HttpSession session = request.getSession();
 		Clients client = (Clients) session.getAttribute("client");
 		String image = (String)session.getAttribute("image");
 		System.out.println(image);
 		String pattern = (String)request.getParameter("pattern");
 		if(pattern.length() != 0){
 			
 			try {
 				client.setPattern(pattern);
 			} catch (NoSuchAlgorithmException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		else{
 			Object obj = new Object();
 			obj = "<p style='color:red'>*You did not choose a pattern</p>";
 			request.setAttribute("regResult", obj);
			request.getRequestDispatcher("pattern.jsp").forward(request, response);
 			return;
 		}
 		client.setActivation("Pending");
 		client.setToken();
 		String token = client.getToken();
 		String tokenHash = "";
 		Clients c = new Clients();
 			try {
 				tokenHash = Algorithms.encrypt(token,client.getUserId().substring(0,16));
 				client.setToken(tokenHash);
 				c = ClientsDAO.registerClient(client);
 				EmailSender email = new EmailSender(client);
 				email.sendActivationLink(token);
 			} catch (NoSuchAlgorithmException e) {
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		
 
 		if(c!=null){
 			session.removeAttribute("image");
 			session.removeAttribute("client");
 			FileMethods.fileDelete(image);
 			Object obj = new Object();
 			obj = "<p style='color:lime'>*You have successfully registered with us! We have sent you an activation link to your email.</p>";
 			request.setAttribute("regResult", obj);
 			request.getRequestDispatcher("templateLogin.jsp").forward(request, response);
 		}	
 		
 		else{
 			Object obj = new Object();
 			obj = "<p style='color:red'>*Registration failed</p>";
 			request.setAttribute("regResult", obj);
			request.getRequestDispatcher("pattern.jsp").forward(request, response);
 			return;
 		}
 		
 		
 	}
 
 }
