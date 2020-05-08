 package models.helpers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.User;
 
 /**
  * 
  * @author Alex Jarvis axj7@aber.ac.uk
  */
 public class UserConnectionHelper {
 	
 	/**
 	 * 
 	 * @param user1
 	 * @param user2
 	 * @return
 	 */
 	public static boolean createUserConnectionRequest(User user1, User user2) {
 		
 		// If the users are already connected, return false
 		if (isUsersConnected(user1, user2)) {
 			return false;
 		}
 		
 		// If there is already an existing user connection request, return false
 		if (user1.userConnectionRequestsTo.contains(user2) && user2.userConnectionRequestsFrom.contains(user1)) {
 			return false;
 		}
 		
 		// If there is already an existing user connection request (in the other direction), return false
 		if (user2.userConnectionRequestsTo.contains(user1) && user1.userConnectionRequestsFrom.contains(user2)) {
 			return false;
 		}
 		
 		// Create the user connection request
 		user1.userConnectionRequestsTo.add(user2);
 		user1.save();
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @param user1
 	 * @param user2
 	 * @return
 	 */
 	public static boolean removeUserConnectionRequest(User user1, User user2) {
 		
		if (user1.userConnectionRequestsTo.contains(user2)) {
 			user1.userConnectionRequestsTo.remove(user2);
			user1.save();
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * 
 	 * @param user1
 	 * @param user2
 	 */
 	public static void createUserConnection(User user1, User user2) {
 		user1.userConnectionsTo.add(user2);
 		user1.save();
 	}
 	
 	/**
 	 * 
 	 * @param user1
 	 * @param user2
 	 */
 	public static void removeUserConnection(User user1, User user2) {
 		user1.userConnectionsTo.remove(user2);
 		user1.save();
 	}
 	
 	/**
 	 * 
 	 * @param user
 	 * @return
 	 */
 	public static List<User> userConnectionsAsUsers(User user) {
 		List<User> connectedUsers = new ArrayList<User>();
 		connectedUsers.addAll(user.userConnectionsTo);
 		connectedUsers.addAll(user.userConnectionsFrom);
 		return connectedUsers;
 	}
 	
 	/**
 	 * 
 	 * @param user1
 	 * @param user2
 	 * @return
 	 */
 	public static boolean isUsersConnected(User user1, User user2) {
 		return userConnectionsAsUsers(user1).contains(user2);
 	}
 
 }
