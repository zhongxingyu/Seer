 package org.cvut.wa2.projectcontrol;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jdo.JDOObjectNotFoundException;
 import javax.jdo.PersistenceManager;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.cvut.wa2.projectcontrol.DAO.PMF;
 import org.cvut.wa2.projectcontrol.entities.DocumentEntity;
 import org.cvut.wa2.projectcontrol.entities.DocumentsToken;
 import org.cvut.wa2.projectcontrol.entities.Task;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
 import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
 import com.google.gdata.client.authn.oauth.OAuthException;
 import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
 import com.google.gdata.client.authn.oauth.OAuthSigner;
 import com.google.gdata.client.spreadsheet.*;
 import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
 import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
 
 public class AddDocumentsServlet extends HttpServlet {
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		UserService userService = UserServiceFactory.getUserService();
 		RequestDispatcher disp = null;
 		if (userService.isUserLoggedIn()) {
 			User user = userService.getCurrentUser();
			PersistenceManager pm = PMF.get();
 			DocumentsToken token = null;
 			try {
 				token = pm.getObjectById(DocumentsToken.class, user.getEmail());
 				String taskName = req.getParameter("taskName");
 				if (taskName != null) {
					PersistenceManager manager = PMF.get();
 					Task task = manager.getObjectById(Task.class, taskName);
 					if (task != null) {
 						GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
 						oauthParameters.setOAuthConsumerKey("anonymous");
 						oauthParameters.setOAuthConsumerSecret("anonymous");
 						oauthParameters.setOAuthToken(token.getToken());
 						oauthParameters.setOAuthTokenSecret(token
 								.getTokenSecret());
 						OAuthSigner signer = new OAuthHmacSha1Signer();
 
 						SpreadsheetService service = new SpreadsheetService(
 								"SpreadsheetService");
 						List<SpreadsheetEntry> listOfEntries = null;
 						try {
 							service.setOAuthCredentials(oauthParameters, signer);
 							SpreadsheetFeed feed = service
 									.getFeed(
 											new URL(
 													"https://spreadsheets.google.com/feeds/spreadsheets/private/full"),
 											SpreadsheetFeed.class);
 							listOfEntries = feed.getEntries();
 							List<DocumentEntity> listOfDocumentEntities = new ArrayList<DocumentEntity>();;
 							if (listOfEntries.size() == 0) {
 //								TODO
 							}else{
 								DocumentEntity entity = null;
 								for (SpreadsheetEntry entry : listOfEntries) {
 									entity = new DocumentEntity();
 									entity.setDocName(entry.getTitle().getPlainText());
 									entity.setHref(entry.getHtmlLink().getHref());
 									listOfDocumentEntities.add(entity);
 								}
 							}
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 
 					} else {
 						String errText = "Task with task name " + taskName
 								+ "doesnt exist.";
 						req.setAttribute("err", errText);
 						disp = req.getRequestDispatcher("err.jsp");
 						disp.forward(req, resp);
 					}
 				} else {
 					String errText = "parametru taskName is null";
 					req.setAttribute("err", errText);
 					disp = req.getRequestDispatcher("err.jsp");
 					disp.forward(req, resp);
 				}
 			} catch (JDOObjectNotFoundException e) {
 				String consumerKey = "anonymous";
 				String consumerSecret = "anonymous";
 				String scope = "https://spreadsheets.google.com/feeds https://docs.google.com/feeds";
 				String callback = "http://vrchlpet-projectcontrol.appspot.com/documentscallbackservlet";
 
 				GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
 				oauthParameters.setOAuthConsumerKey(consumerKey);
 				oauthParameters.setOAuthConsumerSecret(consumerSecret);
 				oauthParameters.setScope(scope);
 				oauthParameters.setOAuthCallback(callback);
 
 				OAuthSigner signer = new OAuthHmacSha1Signer();
 				GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(signer);
 				try {
 					oauthHelper.getUnauthorizedRequestToken(oauthParameters);
 					String approvalPageUrl = oauthHelper
 							.createUserAuthorizationUrl(oauthParameters);
 					req.getSession().setAttribute("tokenSecret",
 							oauthParameters.getOAuthTokenSecret());
 					resp.sendRedirect(approvalPageUrl);
 					return;
 				} catch (OAuthException ee) {
 					ee.printStackTrace();
 				}
 			} finally {
 				pm.close();
 			}
 		} else {
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
