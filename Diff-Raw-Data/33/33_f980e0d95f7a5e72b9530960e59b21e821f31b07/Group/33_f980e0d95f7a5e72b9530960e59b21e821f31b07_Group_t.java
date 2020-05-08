 /**
  * 
  */
 package bzb.gae.summer.jdo;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import bzb.gae.PMF;
 import bzb.gae.Utility;
 import bzb.gae.ws.Twitter;
 
 import com.google.appengine.api.datastore.Key;
 
 /**
  * @author bzb
  *
  */
 @PersistenceCapable
 public class Group {
 
 	private static final int TOTAL_CAPACITY = 4;
 	private static final int TIME_WINDOW = 3;
 	
 	private static final Logger log = Logger.getLogger(Group.class.getName());
 	
 	@PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Key key;
 	
 	@Persistent
 	private String arrivalTime;
 	
 	public Group (String arrivalTime) {
 		setArrivalTime(arrivalTime);
 	}
 	
 	public static Group getGroup (Key key) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			Query q = pm.newQuery("select from " + Group.class.getName());
 		    List<Group> groups = (List<Group>) q.execute();
 		    Iterator<Group> it = groups.iterator();
 		    while (it.hasNext()) {
 				Group thisGroup = it.next();
 				if (thisGroup.key.equals(key)) {
 					return thisGroup;
 				}
 		    }
 		} finally {
 			pm.close();
 		}
 		return null;
 	}
 
 	public Key getKey() {
         return key;
     }
 	
 	public void setArrivalTime(String arrivalTime) {
 		this.arrivalTime = arrivalTime;
 	}
 
 	public String getArrivalTime() {
 		return arrivalTime;
 	}
 	
 	public int size() {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		int count = 0;
 		try {
 			Query q = pm.newQuery("select from " + User.class.getName());
 			List<User> users = (List<User>) q.execute();
 			Iterator<User> i = users.iterator();
 			while (i.hasNext()) {
 				if (i.next().getGroupKey().equals(getKey())) {
 					count++;
 				}
 			}
 			return count;
 		} finally {
 			pm.close();
 		}
 	}
 	
 	public boolean moreRoom () {
 		if (size() < TOTAL_CAPACITY) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public List<User> getUsers (String exceptUsername) {
 		List<User> myUsers = new ArrayList<User>();
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			Query q = pm.newQuery("select from " + User.class.getName());
 			List<User> users = (List<User>) q.execute();
 			Iterator<User> i = users.iterator();
 			while (i.hasNext()) {
 				User user = i.next();
 				if (user.getGroupKey().equals(getKey()) && (exceptUsername == null || (exceptUsername != null && !user.getUsername().equals(exceptUsername)))) {
 					myUsers.add(user);
 				}
 			}
 		} finally {
 			pm.close();
 		}
 		return myUsers;
 	}
 	
 	public String toString (String username) {
 		String message = "Your group: ";
 		List<User> users = getUsers(username);
 		Iterator<User> i = users.iterator();
 		while (i.hasNext()) {
 			User user = i.next();
 			if (Utility.isValidPhone(user.getContact())) {
 				message += user.getUsername() + " " + user.getContact() + ", ";
 			} else if (user.getOptionalContact() != null && Utility.isValidPhone(user.getOptionalContact())) {
 				message += user.getUsername() + " " + user.getOptionalContact() + ", ";
 			} else if (Utility.isValidTwitter(user.getContact())) {			
 				message += user.getContact() + ", ";
 			} else {
 				message += user.getUsername() + " (fb), ";
 			}
 		}
 		if (users.size() > 0) {
 			message = message.substring(0, message.length() - 2);
 		}
 		return message;
 	}
 	
 	public void mailGroup () {
 		List<User> users = getUsers(null);
 		if (users.size() == 1) {
 			User user = users.get(0);
 			String message = "Unfortunately there was no-one else in your group. Travel on by yourself.";
 			if (Utility.isValidPhone(user.getContact())) {
 				Utility.sendSMS(user.getContact(), message);
 			} else if (user.getOptionalContact() != null && Utility.isValidPhone(user.getOptionalContact())) {
 				Utility.sendSMS(user.getOptionalContact(), message);
 			} else if (Utility.isValidTwitter(user.getContact())) {
 				log.warning("Twitter message " + Twitter.sendDirectMessage(user.getContact(), message));
 			}
 		} else if (users.size() > 1) {
 			Iterator<User> i = users.iterator();
 			while (i.hasNext()) {
 				User user = i.next();
 				String message = toString(user.getUsername()) + ". Meet at the station WHSmith";
 				if (Utility.isValidPhone(user.getContact())) {
 					Utility.sendSMS(user.getContact(), message);
 				} else if (user.getOptionalContact() != null && Utility.isValidPhone(user.getOptionalContact())) {
 					Utility.sendSMS(user.getOptionalContact(), message);
 				} else if (Utility.isValidTwitter(user.getContact())) {
 					log.warning("Twitter message " + Twitter.sendDirectMessage(user.getContact(), message));
 				}
 			}
 		}
 	}
 	
 	public boolean isInGroupsTimeWindow (String userArrivalTime) {
 		boolean isIn = false;
 
 		int userHours = Integer.parseInt(userArrivalTime.substring(0, userArrivalTime.length() - 2));
 		int userMinutes = userHours * 60 + Integer.parseInt(userArrivalTime.substring(userArrivalTime.length() - 2));
 
 		int groupHours = Integer.parseInt(getArrivalTime().substring(0, getArrivalTime().length() - 2));
 		int groupMinutes = groupHours * 60 + Integer.parseInt(getArrivalTime().substring(getArrivalTime().length() - 2));
 
 		if (userMinutes <= (groupMinutes + TIME_WINDOW) && userMinutes >= (groupMinutes - TIME_WINDOW)) {
 			isIn = true;
 		}
 		return isIn;
 	}
 }
