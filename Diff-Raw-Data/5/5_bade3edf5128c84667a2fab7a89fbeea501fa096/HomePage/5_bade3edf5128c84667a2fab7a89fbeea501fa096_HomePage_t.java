 package groupone;
 
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.catalina.Session;
 
 /**
  * Servlet implementation class HomePage
  */
 @WebServlet("/HomePage")
 public class HomePage extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static String email;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public HomePage() {
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
 		// TODO Auto-generated method stub
 		HttpSession userSession = request.getSession(false);
 		email = userSession.getAttribute("userEmail").toString();
 		
 		String name = request.getParameter("button");
 		
		if(name.equals("Home")){
			request.getRequestDispatcher("/homeProposal.jsp").forward(request, response);
		}
		else if(name.equals("Browse")) {
 			request.getRequestDispatcher("/page_browse.jsp").forward(request, response);
 		}
 		else if(name.equals("Order History")) {
 			request.getRequestDispatcher("/page_orderHist.jsp").forward(request, response);
 		}
 		else if(name.equals("Account")) {
 			request.getRequestDispatcher("/page_account.jsp").forward(request, response);
 		}
 		else {
 			request.getRequestDispatcher("/index.jsp").forward(request, response);
 		}		
 	}
 
 }
