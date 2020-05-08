 package com.fauxwerd.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import com.fauxwerd.model.Activity;
 import com.fauxwerd.model.User;
 import com.fauxwerd.model.UserFollow;
 
 @Repository
 public class ActivityDAOImpl implements ActivityDAO {
 	
 	private static final Logger log = LoggerFactory.getLogger(ActivityDAO.class);
 	
     @Autowired
     private SessionFactory sessionFactory;
 
 	@Override
 	public void saveOrUpdateActivity(Activity activity) {		
 		sessionFactory.getCurrentSession().saveOrUpdate(activity);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Activity> listAllActivity() {
		return sessionFactory.getCurrentSession().createQuery("from Activity as activity order by activity.dateAdded desc").list();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Activity> listActivityOfUsersFollowedByUser(User user) {
 		List<Activity> activityFeed = new ArrayList<Activity>();
 		List<User> following = new ArrayList<User>();
 		if(user.getFollowing() != null && !user.getFollowing().isEmpty()) {
 			for (UserFollow userFollow : user.getFollowing()) {
 				following.add(userFollow.getFollow());
 			}						
			Query q = sessionFactory.getCurrentSession().createQuery("from Activity as activity where activity.user in (:following) order by activity.dateAdded desc");
 			q.setParameterList("following", following);
 			activityFeed = q.list();
 		}
 		return activityFeed;
 	}
 
 }
