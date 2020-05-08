 package domain;
 
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ch.hszt.mdp.domain.Friendship;
 import ch.hszt.mdp.domain.User;
 
 public class GetTestObjects {
 	public static User getUser() {
 	
 		User user = new User();
 		user.setEmail("gabathuler@gmail.com");
 		user.setPrename("Cyril");
 		user.setSurname("Gabathuler");
 		user.setPassword("123");
 		user.setRepeat("123");
 		user.setId(1);
 		
 		user.setFriendships(getFriends());
 		return user;
 	}
 	public static List<Friendship> getFriends(){
 		
 	 	ArrayList<Friendship>  friendships = new ArrayList<Friendship>();
 		User user1 = new User();
 		user1.setEmail("roger.bollmann@gmail.com");
 		user1.setPrename("Roger");
 		user1.setSurname("Bollmann");
 		user1.setPassword("1234");
 		user1.setRepeat("1234");
 		
 		User user2 = new User();
 		user2.setEmail("gabathuler@gmail.com");
 		user2.setPrename("Cyril");
 		user2.setSurname("Gabathuler");
 		user2.setPassword("123");
 		user2.setRepeat("123");
 		
 		Friendship friends = new Friendship();
		friends.setPrimaryUser(user1);
		friends.setSecondaryUser(user2);
 		friends.setAccepted(1);
 		
 		friendships.add(friends);
 			
 		return friendships;
 		
 	}
 
 }
