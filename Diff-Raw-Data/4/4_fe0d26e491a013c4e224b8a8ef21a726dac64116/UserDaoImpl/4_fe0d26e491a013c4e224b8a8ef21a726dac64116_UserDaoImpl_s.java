 package ch.hszt.mdp.dao;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.hibernate.Query;
 import org.springframework.orm.hibernate3.HibernateTemplate;
 
 import ch.hszt.mdp.domain.User;
 
 /**
  * Return a User-Email Address from the User
  * 
  * @author Cyril Gabathuler
  * 
  */
 
 public class UserDaoImpl extends HibernateTemplate implements UserDao {
 
 	/**
 	 * Saves a UserObject to the Database with Hibernate
 	 */
 	public void save(User user) {
 		getSession().saveOrUpdate(user);
 	}
 
 	/**
 	 * Creates a UserList with User Objects. The Userlist is collected from the User Database.
 	 */
 	public User getUserByEmail(String email) {
 		return (User) getSession().createQuery("from User u where u.email='" + email + "'").uniqueResult();
 	}
 
 	public User getUser(int id) {
 		return (User) getSession().createQuery("from User u where u.id = :id").setParameter("id", id).uniqueResult();
 	}
 
 	/**
 	 * Checks if there is already a user with this email address.
 	 * 
 	 * @param email
 	 * @return <code>true</code> if the email address already exists
 	 */
 	public boolean duplicate(String email) {
 
 		Query q = getSession().createQuery("SELECT COUNT(u.id) FROM User u where u.email = :email");
 		q.setParameter("email", email);
 
 		long count = (Long) q.iterate().next();
 
 		return count > 0;
 	}
 	
 	/**
 	 * Accept friend by clicking on the ignore button in the GUI
 	 * Set accepted = 2 in the DB
 	 * @author Roger Bollmann
 	 */
 	public void ignoreFriend(int friendId, int id) {
 		
 		Query q = getSession().createQuery("update Friendship set accepted = '2' where primary_user = :id and secondary_user = :friendId");
 		q.setParameter("id", id);
 		q.setParameter("friendId", friendId);
 		
 		q.executeUpdate();
 	}
 	
 	/**
 	 * Accept friend by clicking on the accept button in the GUI
 	 * Set accepted = 1 in the DB
 	 * @author Roger Bollmann
 	 */
 	
 	public void acceptFriend(int friendId, int id) {
 		
 		Query q = getSession().createQuery("update Friendship set accepted = '1' where primary_user = :id and secondary_user = :friendId");
 		q.setParameter("id", id);
 		q.setParameter("friendId", friendId);	
 		
 
 		q.executeUpdate();
 	}
 	
 	public ArrayList<Integer> searchUser(String search){
 		
 		ArrayList<Integer> userids = new ArrayList<Integer>();
 		
 		String [] searchUser = (search.split(" "));
 		
 		for (int i = 0; i < searchUser.length; i++) {
			Query q = getSession().createQuery("SELECT u.id FROM User u where u.email like '%:searchString%' or u.prename like '%:searchString%' or u.surname like '%:searchString%'");
			q.setParameter("searchString", searchUser[i]);
 			Iterator iter = q.iterate();
 			
 			while (iter.hasNext()){
 				Integer userid = (Integer) iter.next();
 				userids.add(userid);
 			}
 		
 		}
 		
 		return userids;
 	}
 
 
 
 
 }
