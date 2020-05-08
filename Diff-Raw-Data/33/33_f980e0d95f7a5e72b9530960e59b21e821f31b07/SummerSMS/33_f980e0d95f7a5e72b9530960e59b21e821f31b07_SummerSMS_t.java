 /**
  * 
  */
 package bzb.gae.summer;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.jdo.JDOObjectNotFoundException;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import bzb.gae.PMF;
 import bzb.gae.Utility;
 import bzb.gae.exceptions.BadArrivalTimeException;
 import bzb.gae.exceptions.TooFewArgumentsException;
 import bzb.gae.exceptions.TooManyArgumentsException;
 import bzb.gae.exceptions.UserAlreadyExistsException;
 import bzb.gae.exceptions.UserNotFoundException;
 import bzb.gae.summer.jdo.Group;
 import bzb.gae.summer.jdo.User;
 
 import com.google.appengine.api.datastore.Key;
 
 /**
  * @author bzb
  * 
  */
 public class SummerSMS {
 
 	private static final Logger log = Logger.getLogger(SummerSMS.class.getName());
 	private final static int MAX_PARAMETERS = 4;
 	private final static int MIN_PARAMETERS = 3;
 
 	private String originator;
 	private String optionalContact;
 	private String username;
 	private String arrivalTime;
 	private Key groupKey;
 
 	public SummerSMS(String originator, String[] smsChunks)
 			throws UserAlreadyExistsException, BadArrivalTimeException,
 			TooManyArgumentsException, TooFewArgumentsException, UserNotFoundException {
 		if (smsChunks.length > MAX_PARAMETERS) {
 			throw new TooManyArgumentsException();
 		} else if (smsChunks.length < MIN_PARAMETERS) {
 			PersistenceManager pm = PMF.get().getPersistenceManager();
 			try {
 				String username = smsChunks[1].trim();
 				User user = pm.getObjectById(User.class, username);
 				pm.deletePersistent(user);
 				Utility.sendSMS(originator, "Successfully removed your user from the experience");
 			} catch (JDOObjectNotFoundException je) {
 				throw new UserNotFoundException();
 			} finally {
 				pm.close();
 			}
 			throw new TooFewArgumentsException();
 		} else {
 			checkDetails(originator, smsChunks);
 			checkGroup();
 			if (getUsername() != null) {
 				PersistenceManager pm = PMF.get().getPersistenceManager();
 				User user = new User(getUsername(), getOriginator(),
 						getArrivalTime(), getGroupKey(), getOptionalContact());
 				try {
 					pm.makePersistent(user);
 				} finally {
 					pm.close();
 				}
 			} else {
 				throw new UserAlreadyExistsException();
 			}
 			
 			PersistenceManager pm = PMF.get().getPersistenceManager();
 			try {
 				Group group = pm.getObjectById(Group.class, getGroupKey());
 				if (!group.moreRoom()) { // what if a user in a group that is already full, updates their details? The group will get messaged again
 					group.mailGroup();
 				} else {
 					Utility.sendSMS(originator, "You're registered but your group is not full yet; nearer to your arrival time we'll tell you what to do next");
 				}
 			} finally {
 				pm.close();
 			}
 		}
 	}
 
 	private void setOriginator(String originator) {
 		this.originator = originator.trim();
 	}
 
 	public String getOriginator() {
 		return originator;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void checkDetails(String originator, String[] smsChunks) throws BadArrivalTimeException {
 		setOriginator(originator);
 		String username = smsChunks[1].trim();
 		setUsername(username);
 		String arrivalTime = parseArrivalTime(smsChunks[2].trim());
 		setArrivalTime(arrivalTime);
		String optionalContact = null;
		if (smsChunks.length > 3 && smsChunks[3] != null) {
			optionalContact = smsChunks[3].trim();
			setOptionalContact(optionalContact);
		}
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			try {
 				User user = pm.getObjectById(User.class, username);
 				user.setArrivalTime(arrivalTime);
 				user.setContact(getOriginator());
 				user.setOptionalContact(optionalContact);
 			} catch (JDOObjectNotFoundException je) {
 
 			}
 		} finally {
 			pm.close();
 		}
 	}
 
 	public String parseArrivalTime(String rawArrivalTime)
 			throws BadArrivalTimeException {
 		String arrivalTime = null;
 		Pattern p = Pattern
 				.compile("^((?:[0-1]?[0-9])|(?:2[0-3]))(?:[\\:\\.])?(([0-5][0-9]))$");
 		Matcher m = p.matcher(rawArrivalTime);
 		if (m.matches()) {
 			arrivalTime = m.group(1); // hours
 			if (arrivalTime.length() == 1) {
 				arrivalTime = '0' + arrivalTime;
 			}
 			arrivalTime += m.group(2); // minutes
 		} else {
 			throw new BadArrivalTimeException();
 		}
 		return arrivalTime;
 	}
 
 	public void setArrivalTime(String arrivalTime) {
 		this.arrivalTime = arrivalTime;
 	}
 
 	public String getArrivalTime() {
 		return arrivalTime;
 	}
 
 	public void setGroupKey(Key groupKey) {
 		this.groupKey = groupKey;
 	}
 
 	public Key getGroupKey() {
 		return groupKey;
 	}
 	
 	public void checkGroup () {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			try {
 				User user = pm.getObjectById(User.class, getUsername());
 				log.warning("User already exists");
 	
 				if (user.getGroupKey() == null || !pm.getObjectById(Group.class, user.getGroupKey()).getArrivalTime().equals(getArrivalTime())) {
 					Query q = pm.newQuery("select from " + Group.class.getName());// + " where arrivalTime == " + getArrivalTime());
 					List<Group> groups = (List<Group>) q.execute();
 					Iterator<Group> it = groups.iterator();
 					while (it.hasNext()) {
 						Group thisGroup = it.next();
 						if (thisGroup.isInGroupsTimeWindow(getArrivalTime()) && thisGroup.moreRoom()) {
 					    	log.warning("Assigning user to different existing group");
 					    	user.setGroupKey(thisGroup.getKey());
 					    	setGroupKey(thisGroup.getKey());
 					    	break;
 					    }
 					}
 					if (getGroupKey() == null) {
 				    	log.warning("Creating a new group for user");
 				    	Group newGroup = new Group(getArrivalTime());
 				    	pm.makePersistent(newGroup);
 				    	user.setGroupKey(newGroup.getKey());
 				    	setGroupKey(newGroup.getKey());
 				    }
 				} else {
 					log.warning("User's existing group is fine");
 					setGroupKey(user.getGroupKey());
 				}
 			} catch (JDOObjectNotFoundException je) {
 				log.warning("User doesn't exist yet");
 				Query q = pm.newQuery("select from " + Group.class.getName());
 			    List<Group> groups = (List<Group>) q.execute();
 			    Iterator<Group> it = groups.iterator();
 			    while (it.hasNext()) {
 					Group thisGroup = it.next();
 					if (thisGroup.isInGroupsTimeWindow(getArrivalTime()) && thisGroup.moreRoom()) {
 						log.warning("Assigning user to existing group");
 						setGroupKey(thisGroup.getKey());
 						break;
 					}
 			    }
 			    if (getGroupKey() == null) {
 			    	log.warning("Creating a new group for user");
 			    	Group newGroup = new Group(getArrivalTime());
 			    	pm.makePersistent(newGroup);
 			    	setGroupKey(newGroup.getKey());
 			    }
 			}
 		} finally {
 			pm.close();
 		}
 	}
 
 	public void setOptionalContact(String optionalContact) {
 		this.optionalContact = optionalContact;
 	}
 
 	public String getOptionalContact() {
 		return optionalContact;
 	}
 
 }
