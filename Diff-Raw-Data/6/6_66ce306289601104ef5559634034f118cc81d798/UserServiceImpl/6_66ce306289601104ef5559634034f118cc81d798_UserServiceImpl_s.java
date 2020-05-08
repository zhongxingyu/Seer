 package ch.hszt.mdp.service;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import ch.hszt.mdp.dao.UserDao;
 import ch.hszt.mdp.domain.Activity;
 import ch.hszt.mdp.domain.Friendship;
 import ch.hszt.mdp.domain.Stream;
 import ch.hszt.mdp.domain.User;
 
 /**
  * Implementation for UserService. This handles the registration on back-end side.
  * 
  * @author Fabian Vogler
  * 
  */
 @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
 public class UserServiceImpl implements UserService {
 
 	private UserDao userDao;
 
 	public void setUserDao(UserDao userDao) {
 		this.userDao = userDao;
 	}
 
 	/**
 	 * This method saves a user into the database.
 	 * 
 	 * @param password
 	 * @param user
 	 * @param userDao
 	 *            User Data Access object defination for hibernate
 	 */
 	public void create(User user) {
 
 		String password = user.getPassword();
 
 		try {
 
 			// convert password to SHA1
 			password = sha1(password);
 
 			user.setPassword(password);
 			user.setRepeat(password);
 
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 
 		userDao.save(user);
 	}
 
 	public String sha1(String password) throws NoSuchAlgorithmException {
 
 		MessageDigest md = MessageDigest.getInstance("SHA1");
 		md.reset();
 
 		byte[] buffer = password.getBytes();
 		md.update(buffer);
 
 		byte[] digest = md.digest();
 
 		String hexStr = "";
 		for (int i = 0; i < digest.length; i++) {
 			hexStr += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
 		}
 
 		return hexStr;
 	}
 
 	/**
 	 * Creates a UserList with User Objects. The Userlist is collected from the User Database.
 	 */
 	public User getUserByEmail(String email) {
 		return userDao.getUserByEmail(email);
 	}
 
 	public List<Friendship> getAccepteFriendships(String email) {
 
 		User user = getUserByEmail(email);
 		List<Friendship> acceptedFriends = new ArrayList<Friendship>();
 
 		for (Friendship friend : user.getFriendships()) {
 
 			if (friend.getAccepted() == 1) {
 				acceptedFriends.add(friend);
 			}
 		}
 
 		return acceptedFriends;
 
 	}
 
 	public Stream getActivitiesFromFriends(String email) {
 		List<Friendship> friends = getAccepteFriendships(email);
 
 		DateTime now = new DateTime();
 
 		DateTime startOfToday = now.toDateMidnight().toInterval().getStart();
 		DateTime endOfToday = now.toDateMidnight().toInterval().getEnd();
		DateTime endOfYesterDay = now.minusDays(1).toDateMidnight().toInterval().getEnd();
 
 		Stream stream = new Stream();
 
 		for (Friendship friend : friends) {
 
 			for (Activity activity : friend.getSecondaryUser().getActivities()) {
 				if (activity.getTime().isAfter(startOfToday) && activity.getTime().isBefore(endOfToday)) {
 					stream.addTodaysActivity(activity);
				} else if (activity.getTime().isAfter(endOfYesterDay) && activity.getTime().isBefore(startOfToday)) {
 					stream.addYesterdaysActivities(activity);
 				} else {
 					stream.addPastActivities(activity);
 				}
 
 			}
 		}
 
 		return stream;
 	}
 
 	@Override
 	public User getUser(int id) {
 		return userDao.getUser(id);
 	}
 }
