 package com.madrone.lms.dao.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.stereotype.Repository;
 
 import com.madrone.lms.dao.UserDao;
 import com.madrone.lms.entity.User;
 
 @Repository("userDao")
 public class UserDaoImpl extends AbstractDaoImpl<User, Long> 
 											implements UserDao {
 
 	protected UserDaoImpl() {
 		super(User.class);
 	}
 
 	@Override
 	public void saveUser(User user) {
 		saveOrUpdate(user);		
 	}
 
 	@Override
 	public User findByUserName(String userName) {
 		 List<Criterion> criterionList = new ArrayList<Criterion>();
          criterionList.add(Restrictions.eq("userName", userName));
          List<User> users = findByCriteria(criterionList);
 				
 		if(users.size() > 1) {
			throw new AssertionError("Duplicate user records with same " +
 					"userName.");
 		}
 		
 		return users.isEmpty() ? null : users.get(0) ;
 	}
 
 	@Override
     public boolean authenticateUser(String userName, String password) {
             List<Criterion> criterionList = new ArrayList<Criterion>();
             criterionList.add(Restrictions.eq("userName", userName));
             criterionList.add(Restrictions.eq("password", password));
             
             List<User> users = findByCriteria(criterionList);
             return (users == null || users.size()==0) ? false:true;
     }
 }
