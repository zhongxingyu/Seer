 /**
  * 
  */
 package bzb.gae.summer;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import bzb.gae.PMF;
 import bzb.gae.Utility;
 import bzb.gae.summer.jdo.Group;
 
 /**
  * @author psxbdb
  * 
  */
 @SuppressWarnings("serial")
 public class EndOfJourneyChecker extends HttpServlet {
 
	private static final int MINUTES_BEFORE_ARRIVAL = 15;
 	
 	private static final Logger log = Logger
 			.getLogger(EndOfJourneyChecker.class.getName());
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws IOException {
 		log.warning("Checking for groups reaching end of their journey");
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			Query q = pm.newQuery("select from " + Group.class.getName());
 		    List<Group> groups = (List<Group>) q.execute();
 		    Iterator<Group> it = groups.iterator();
 		    while (it.hasNext()) {
 				Group thisGroup = it.next();
 				int minutesRemaining = Utility.getMinutesUntil(thisGroup.getArrivalTime());
				if (minutesRemaining >= 0) {
					if (minutesRemaining < MINUTES_BEFORE_ARRIVAL) {
 						log.warning(thisGroup.getKey().toString() + " near end of journey; arriving at " + thisGroup.getArrivalTime());
 						thisGroup.mailGroup();
 					} else {
 						log.warning(thisGroup.getKey().toString() + " still on journey with " + minutesRemaining + " minutes remaining (arriving at " + thisGroup.getArrivalTime() + ")");
 					}
 				} else {
 					log.warning(thisGroup.getKey().toString() + " arrived at " + thisGroup.getArrivalTime());
 				}
 		    }
 		} finally {
 			pm.close();
 		}
 	}
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws IOException {
 		doGet(request, response);
 	}
 }
