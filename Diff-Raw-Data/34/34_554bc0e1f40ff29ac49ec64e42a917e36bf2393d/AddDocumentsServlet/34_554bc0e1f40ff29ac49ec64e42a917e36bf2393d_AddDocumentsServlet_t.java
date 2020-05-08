 package org.cvut.wa2.projectcontrol;
 
 import java.io.IOException;
 
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import org.cvut.wa2.projectcontrol.DAO.PMF;
import org.cvut.wa2.projectcontrol.entities.DocumentsToken;

import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
 
 public class AddDocumentsServlet extends HttpServlet{
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		UserService userService = UserServiceFactory.getUserService();
 		RequestDispatcher disp = null;
 		if (userService.isUserLoggedIn()) {
			User user = userService.getCurrentUser();
			PersistenceManager pm = PMF.get().getPersistenceManager();
			DocumentsToken token = null;
			try {
				String consumerKey = "anonymous";
				String consumerSecret = "anonymous";
				String scope = "https://spreadsheets.google.com/feeds https://docs.google.com/feeds";
				String callback = "http://vrchlpet-projectcontrol.appspot.com/documentscallbackservlet";
				
				GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();	
				oauthParameters.setOAuthConsumerKey(consumerKey);
				oauthParameters.setOAuthConsumerSecret(consumerSecret);
				oauthParameters.setScope(scope);
				oauthParameters.setOAuthCallback(callback);

				
 			
			} catch (JDOObjectNotFoundException e) {
				
			}finally{
				pm.close();
			}
 		}else{
 			disp = req.getRequestDispatcher("/projectcontrol");
 			disp.forward(req, resp);
 		}
 		
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		doGet(req, resp);
 	}
 
 	
 	
 }
