 package fr.cpcgifts;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import net.sf.jsr107cache.Cache;
 import net.sf.jsr107cache.CacheException;
 import net.sf.jsr107cache.CacheManager;
 
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 import fr.cpcgifts.model.CpcUser;
 import fr.cpcgifts.persistance.PMF;
 import fr.cpcgifts.utils.TextTools;
 
 @SuppressWarnings("serial")
 public class EditUserServlet extends HttpServlet {
 	
 	private static final Logger log = Logger.getLogger(EditUserServlet.class.getName());
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		
 		UserService userService = UserServiceFactory.getUserService();
 		User user = userService.getCurrentUser();
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		HttpSession session = req.getSession();
 		CpcUser cpcuser = (CpcUser) session.getAttribute("cpcuser");
 		cpcuser = pm.getObjectById(CpcUser.class, cpcuser.getKey());
 
 		if (user != null && cpcuser != null) {
 
 			@SuppressWarnings("unchecked")
 			Map<String, String[]> params = req.getParameterMap();
 			
 			String userId = params.get("userid")[0];
 			CpcUser userToUpdate = pm.getObjectById(CpcUser.class, KeyFactory.createKey(CpcUser.class.getSimpleName(),Long.parseLong(userId)));
 
 			String reqType = params.get("req")[0];
 
 			if(cpcuser.getKey().equals(userToUpdate.getKey()) || userService.isUserAdmin()) {
 			
 				if("changeimg".equals(reqType)) {
 					String imgUrl = TextTools.escapeHtml(params.get("imgurl")[0]);
 
 					userToUpdate.setAvatarUrl(imgUrl);
 					log.info(cpcuser + " replaced user " + userToUpdate + " image by " + imgUrl);
 				} else if("addprofile".equals(reqType)) {
 					String service = params.get("service")[0];
 					String link = TextTools.escapeHtml(params.get("link")[0]);
 					
 					userToUpdate.addProfile(service, link);
 					log.info(cpcuser + " added " + service + " link to profile " + link);					
 				} else if("email".equals(reqType)) {
 					String acceptEmails = null;
 					try {
 						acceptEmails = params.get("acceptemails")[0];
 					} catch(Exception e) {}
 					userToUpdate.setAcceptEmails("on".equals(acceptEmails));
 					
 					log.info(cpcuser + " set acceptEmails to " + acceptEmails);
 				}
 
 			} 
 			
 			resp.sendRedirect("/user?userID=" + userToUpdate.getKey().getId());
 			
 			
 			try {
 	            Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
 	            
 	            cache.remove(userToUpdate.getKey());
 				
 	        } catch (CacheException e) {
 	        	//rien
 	        }
 			
 		} else {
 			resp.sendRedirect("/");
 		}
 		
 		pm.close();
 		
 	}
 }
