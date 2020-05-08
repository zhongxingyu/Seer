 package j2ee.association.modele.servlets;
 
 import j2ee.association.bean.Userinfo;
 import j2ee.association.persistence.PersistenceServiceProvider;
 import j2ee.association.persistence.services.UserinfoPersistence;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class IdentificationServlet
  */
 public class IdentificationServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public IdentificationServlet() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// Forwarding the request to the associated view					
 		this.getServletContext().getRequestDispatcher("/jsp/identification.jsp").include(request, response);		
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		request.getSession().invalidate();
 		if (connectUser(request)) {			
			response.sendRedirect(request.getContextPath()+"/IndexServlet");
 		} else {
			response.sendRedirect(request.getContextPath()+"/IdentificationServlet");
 		}
 	}
 
 	private boolean connectUser(HttpServletRequest in){
 		HttpServletRequest request = in;
 		String userName = request.getParameter("id");
 		String userPasswd = request.getParameter("password");
 		if (checkUnicity(userName, userPasswd)) {
 			request.getSession();
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean checkUnicity(String userName, String userPasswd){
 		Map<String, Object> name = new HashMap<String, Object>();
 		name.put("usPseudo = ", userName);
 		UserinfoPersistence service = PersistenceServiceProvider.getService(UserinfoPersistence.class);
 		List<Userinfo> informations = service.search(name);
 		if (informations.size() != 1) {
 			return false;
 		} else {
 			return checkUser(userName, userPasswd, informations.get(0));
 		}
 	}
 
 	private boolean checkUser(String userName, String userPasswd, Userinfo userInfo) {
 		if (userName.equals(userInfo.getUsId())) {
 			if (userPasswd.equals(userInfo.getUsPassword())) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 }
