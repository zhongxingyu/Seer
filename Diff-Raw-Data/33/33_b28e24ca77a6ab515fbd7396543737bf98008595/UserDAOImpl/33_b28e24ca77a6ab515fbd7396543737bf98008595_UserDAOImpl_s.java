 package com.poliformas.warehouse.integration.dao.impl;
 
 import java.sql.SQLException;
 
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.orm.hibernate3.HibernateCallback;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 
 import com.poliformas.warehouse.integration.dao.UserDAO;
 import com.poliformas.warehouse.integration.entity.User;
 
 
 public class UserDAOImpl extends HibernateDaoSupport implements UserDAO {
 	
 	
	public void saveUser(User user){
 		getHibernateTemplate().save(user);
 	}
 	
 	public void updateUser(User user){
 		getHibernateTemplate().update(user);
 	}	
 	
 	public User findUser(final String username,final String password){
 		User user = (User)getHibernateTemplate().execute(new HibernateCallback<User>() {			
 			public User doInHibernate(Session session) throws HibernateException, SQLException {				
 				Criteria c = session.createCriteria(User.class);				
 				c.add(Restrictions.eq("userName", username));
 				c.add(Restrictions.eq("password", password));
 				return (User)c.uniqueResult();
 			}			
 		});
 		return user;
 		
 	}
 	
 	
 	public User findByUsername(final String username){
 		User user = (User)getHibernateTemplate().execute(new HibernateCallback<User>() {			
 			public User doInHibernate(Session session) throws HibernateException, SQLException {				
 				Criteria c = session.createCriteria(User.class);				
 				c.add(Restrictions.eq("username", username));				
 				return (User)c.uniqueResult();
 			}			
 		});
 		if(user==null)
 			System.out.println("mmmmmm");
 		return user;
 	}
 	
 	public User getUserByUsername(final String username){
 		System.out.println("recibe username "+username);
 		User user = (User)getHibernateTemplate().execute(new HibernateCallback<User>() {			
 			public User doInHibernate(Session session) throws HibernateException, SQLException {
 				System.out.println("------------------------------------------");
 				Criteria c = session.createCriteria(User.class);
 				System.out.println("------------------------------------------");
 				c.add(Restrictions.eq("username", username));				
 				System.out.println("---------------------------"+c.uniqueResult()+"---------------");
 				return (User)c.uniqueResult();
 			}			
 		});
 		if(user==null){
 			System.out.println("resultado vacio");
 		}else{
 			System.out.println("+++++++++++++++++++++++++++++++" + user.getUsername());
 		}
 		return user;
 	}
 	
 	
 	
 	
 	
 	
 	
 }
