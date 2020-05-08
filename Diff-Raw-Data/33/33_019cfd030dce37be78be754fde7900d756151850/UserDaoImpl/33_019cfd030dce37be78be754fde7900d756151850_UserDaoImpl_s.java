 package com.lucho.dao.impl;
 
 import com.lucho.dao.UserDao;
 import com.lucho.domain.User;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.util.CollectionHelper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.stereotype.Repository;
 
 import java.util.Collections;
 import java.util.List;
 
 @Repository
 public class UserDaoImpl implements UserDao {
 
     private SessionFactory sessionFactory;
 
     @Override
     public User getUser(final Integer id) {
         Session session = this.getSessionFactory().getCurrentSession();
         return (User) session.get(User.class, id);
     }
 
     @Override
     public User getUser(final String username) {
         Session session = this.getSessionFactory().getCurrentSession();
         Query query = session.createQuery("from User where username = :name");
         query.setString("name", username);
         return (User) query.uniqueResult();
     }
 
     @Override
     public boolean userExists(final String username) {
         Session session = this.getSessionFactory().getCurrentSession();
         Query query = session.createQuery("select count(*) from User where username = :name");
         query.setString("name", username);
        Integer count = (Integer) query.uniqueResult();
         return count.intValue() == 1;
     }
 
     @Override
     public User addUser(final String username, final String password) {
         User user = new User();
         user.setUsername(username);
         user.setPassword(password);
         Session session = this.getSessionFactory().getCurrentSession();
         session.persist(user);
        user.getFollowedBy().add(user);
         user = (User) session.merge(user);
         return user;
     }
 
     public SessionFactory getSessionFactory() {
         return sessionFactory;
     }
 
     @Autowired
     public void setSessionFactory(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 
     @Override
     public void followUser(User user, User userToFollow) {
         Session session = this.getSessionFactory().getCurrentSession();
         userToFollow.getFollowedBy().add(user);
         session.merge(userToFollow);
     }
 }
