 package cyberprime.servlets;
 
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import cyberprime.entities.Clients;
 import cyberprime.entities.dao.ClientsDAO;
 import cyberprime.util.FileMethods;
 
 /**
  * Servlet implementation class ChangePattern
  */
 @WebServlet("/ChangePattern")
 public class ChangePattern extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ChangePattern() {
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
 		Clients client = (Clients) session.getAttribute("c");
 		String image = (String) session.getAttribute("image");
 		String pattern = (String)request.getParameter("pattern");
 		
 		client.setActivation("Active");
 		
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
 			request.setAttribute("resetResult", obj);
 			request.getRequestDispatcher("patternReset.jsp").forward(request, response);
 			return;
 		}
 		
 		ClientsDAO.changePattern(client);
 		ClientsDAO.activateClients(client);
		session.removeAttribute("image");
 		FileMethods.fileDelete(image);
 		
 		request.getRequestDispatcher("secured/templateNewHome.jsp").forward(request, response);
 	}
 
 }
