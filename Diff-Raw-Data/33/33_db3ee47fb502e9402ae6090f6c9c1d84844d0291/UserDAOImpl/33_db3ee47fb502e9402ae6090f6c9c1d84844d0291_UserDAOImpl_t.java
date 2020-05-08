 package org.test.bugtracker.impl.dao;
 
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.test.bugtracker.dao.UserDAO;
 import org.test.bugtracker.model.User;
 
 @Repository
 public class UserDAOImpl implements UserDAO {
     private static final long serialVersionUID = 4612096764084111059L;
 
     @Autowired
     private SessionFactory sessionFactory;
 
     public void save(User entity) {
         sessionFactory.getCurrentSession().save(entity);
     }
 
     public User findById(Long id) {
         return (User) sessionFactory.getCurrentSession().load(
                 UserDAOImpl.class, id);
     }
 
     public void update(User entity) {
         sessionFactory.getCurrentSession().merge(entity);
     }
 
     public void delete(User entity) {
         sessionFactory.getCurrentSession().delete(entity);
     }
 
     public User findByLogin(String login) {
        return (User) sessionFactory.getCurrentSession()
                .createQuery("from User u where u.login=?")
                .setString(0, login).uniqueResult();
     }
 
 }
