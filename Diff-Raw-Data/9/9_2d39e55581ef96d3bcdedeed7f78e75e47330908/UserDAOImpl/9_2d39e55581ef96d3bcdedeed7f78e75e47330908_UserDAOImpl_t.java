 package com.fauxwerd.dao;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.hibernate.SessionFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import com.fauxwerd.model.Invite;
 import com.fauxwerd.model.Role;
 import com.fauxwerd.model.User;
 import com.fauxwerd.model.UserFollow;
 
 @Repository
 public class UserDAOImpl implements UserDAO {
 	
 	private static final Logger log = LoggerFactory.getLogger(UserDAO.class);
 	
 	@Autowired
 	SessionFactory sessionFactory;
 
 	@Override
 	public void saveUser(User entity) {
 		sessionFactory.getCurrentSession().save(entity);
 	}
 	
 	public void saveOrUpdateUser(User user) {
 		sessionFactory.getCurrentSession().saveOrUpdate(user);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public User getUser(String email) {
 		List<User> users = sessionFactory.getCurrentSession().createQuery("select h from User h where email='" + email + "'").list();
 	
 		if (users.size() > 0)
 			return users.get(0);
 		else
 			return null;
 	}	
 
 	public User getUserById(Long userId) {
 		return (User) sessionFactory.getCurrentSession().get(User.class, userId);		
 	}	
 	
 	public Role getRole(String name) {
 		return (Role) sessionFactory.getCurrentSession().createQuery("from Role as role where role.name = ?")
 		.setString(0, name)
 		.uniqueResult();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<User> listUsers() {
         return sessionFactory.getCurrentSession().createQuery("from User")
         .list();		
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<User> listUsersExcept(User user) {
 		return sessionFactory.getCurrentSession().createQuery("from User as user where user.id != ?")
 		.setLong(0, user.getId())
 		.list();
 	}
 	
 	public void addInvite(Invite invite) {
 		sessionFactory.getCurrentSession().save(invite);
 	}
 	
 	public void updateInvite(Invite invite) {
 		sessionFactory.getCurrentSession().update(invite);
 	}
 	
 	public Invite getInvite(String code) {
 		return (Invite)sessionFactory.getCurrentSession().createQuery("from Invite as invite where invite.code = ?")
 		.setString(0, code)
 		.uniqueResult();		
 	}
 
 	public void saveOrUpdateUserFollow(UserFollow userFollow) {
 		sessionFactory.getCurrentSession().saveOrUpdate(userFollow);
 	}
 	
 	public void followUser(User user, User toFollow) {
 		//reattach objects
 		sessionFactory.getCurrentSession().merge(user);
 		sessionFactory.getCurrentSession().merge(toFollow);
 		
		//init lazy-loaded collections
		user.getFollowing().size();
		toFollow.getFollowers().size();
		
 		if (log.isDebugEnabled()) log.debug(String.format("user = %s", user));
 		if (log.isDebugEnabled()) log.debug(String.format("toFollow = %s", toFollow));
 				
 		saveOrUpdateUserFollow(new UserFollow(user, toFollow));
 	}
 	
 	public void unfollowUser(User user, User toUnfollow) {
 		//reattach objects
 		sessionFactory.getCurrentSession().merge(user);
 		sessionFactory.getCurrentSession().merge(toUnfollow);
 
		//init lazy-loaded collections
		user.getFollowing().size();
		toUnfollow.getFollowers().size();
				
 		if (log.isDebugEnabled()) log.debug(String.format("unfollowing %s", toUnfollow.getFullName()));
 		
 		Iterator<UserFollow> iter = user.getFollowing().iterator(); 
 			
 		while (iter.hasNext()) {
 			UserFollow followee = iter.next();
 			if (followee.getFollow().equals(toUnfollow)) {
 				sessionFactory.getCurrentSession().delete(followee);
 				if (log.isDebugEnabled()) log.debug(String.format("removed %s from list of followees", toUnfollow.getFullName()));
 			}
 		}
 			
 	}
 }
