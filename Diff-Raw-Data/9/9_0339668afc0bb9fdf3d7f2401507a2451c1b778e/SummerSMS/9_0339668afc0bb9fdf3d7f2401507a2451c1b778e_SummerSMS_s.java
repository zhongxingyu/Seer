 /**
  * 
  */
 package bzb.gae.summer;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.jdo.PersistenceManager;
 
 import bzb.gae.PMF;
 import bzb.gae.summer.exceptions.BadArrivalTimeException;
 import bzb.gae.summer.exceptions.TooFewArgumentsException;
 import bzb.gae.summer.exceptions.TooManyArgumentsException;
 import bzb.gae.summer.exceptions.UserAlreadyExistsException;
 
 /**
  * @author bzb
  * 
  */
 public class SummerSMS {
 
 	//private static final Logger log = Logger.getLogger(SummerSMS.class.getName());
 	private final static int EXPECTED_PARAMETERS = 3;
 
 	private String originator;
 	private String username;
 	private String arrivalTime;
 
 	public SummerSMS(String originator, String[] smsChunks)
 			throws UserAlreadyExistsException, BadArrivalTimeException,
 			TooManyArgumentsException, TooFewArgumentsException {
 		if (smsChunks.length > EXPECTED_PARAMETERS) {
 			throw new TooManyArgumentsException();
 		} else if (smsChunks.length < EXPECTED_PARAMETERS) {
 			throw new TooFewArgumentsException();
 		} else {
 			setOriginator(originator);
 			checkDetails(smsChunks);
 			if (getUsername() != null) {
 				PersistenceManager pm = PMF.get().getPersistenceManager();
 				User user = new User(getUsername(), getOriginator(),
 						getArrivalTime());
 				try {
 					pm.makePersistent(user);
 				} finally {
 					pm.close();
 				}
 			} else {
 				throw new UserAlreadyExistsException();
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
 
 	public void checkDetails(String[] smsChunks) throws BadArrivalTimeException {
 		String username = smsChunks[1].trim();
 		String arrivalTime = parseArrivalTime(smsChunks[2].trim());
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
			User user = pm.getObjectById(User.class, username);
			if (user != null) {
 				user.setArrivalTime(arrivalTime);
 				user.setPhoneNumber(getOriginator());
			} else {
 				setArrivalTime(arrivalTime);
 				setUsername(username);
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
 
 }
