 package fr.cpcgifts;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.servlet.http.*;
 import javax.jdo.Query;
 import javax.mail.Session;
 
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 import fr.cpcgifts.model.CpcUser;
 import fr.cpcgifts.model.Giveaway;
 import fr.cpcgifts.persistance.CpcUserPersistance;
 import fr.cpcgifts.persistance.GAPersistance;
 import fr.cpcgifts.persistance.PMF;
 
 @SuppressWarnings("serial")
 public class EnterGiveawayServlet extends HttpServlet {
 	
 	private static final Logger log = Logger.getLogger(EnterGiveawayServlet.class.getName());
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		UserService userService = UserServiceFactory.getUserService();
 		User user = userService.getCurrentUser();
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		HttpSession session = req.getSession();
 		CpcUser cpcuser = (CpcUser) session.getAttribute("cpcuser");
 		cpcuser = pm.getObjectById(CpcUser.class, cpcuser.getKey());
 
 		if (user != null && cpcuser != null) {
 
 			Map params = req.getParameterMap();
 			
 			String reqType = ((String[]) params.get("reqtype"))[0];
 			String gaID = ((String[]) params.get("gaid"))[0];
 			Giveaway ga = pm.getObjectById(Giveaway.class, KeyFactory.createKey(Giveaway.class.getSimpleName(),Long.parseLong(gaID)));
 			
 			if(reqType.equals("enter")) {
				if(! ga.getEntrants().contains(cpcuser.getKey())) { // si l'utilisateur est déjà inscrit.
					ga.addEntrant(cpcuser.getKey());
					cpcuser.addEntry(ga.getKey());
				}
 			} else {
 				ga.removeEntrant(cpcuser.getKey());
 				cpcuser.removeEntry(ga.getKey());
 			}
 			
 			
 			
 			resp.sendRedirect("/giveaway?gaID=" + ga.getKey().getId());
 			
 		} else {
 
 			resp.sendRedirect("/");
 		}
 		
 		pm.close();
 	}
 }
