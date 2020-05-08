 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package be.kdg.groepi.dao;
 
 import be.kdg.groepi.model.User;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import org.springframework.dao.DataAccessException;
 import org.springframework.stereotype.Repository;
 
 /**
  * @author: Ben Oeyen
  * @date: 7-mrt-2013
  */
 @Repository
 public class UserDaoImpl implements UserDao {
 
     protected EntityManager entityManager;
 
     public EntityManager getEntityManager() {
         return entityManager;
     }
 
     @PersistenceContext
     public void setEntityManager(EntityManager entityManager) {
         this.entityManager = entityManager;
     }
 
     @Override
     public void createUser(User user) throws DataAccessException {
         getEntityManager().persist(user);
     }
 
     @Override
     public void deleteUser(User user) throws DataAccessException {
         getEntityManager().remove(user);
     }
 
     @Override
     public void updateUser(User user) throws DataAccessException {
         getEntityManager().merge(user);
     }
 
     @Override
     public User getUserById(Long id) throws DataAccessException {
         return getEntityManager().find(User.class, id);
     }
 
     @Override
     public User getUserByResetString(String resetString) throws DataAccessException {
         Query query = getEntityManager().createQuery("from User u Where u.fPasswordResetString = :resetString");
         query.setParameter("resetString", resetString);
         List<User> result = query.getResultList();
         return result.get(0);
     }
 
     @Override
     public User getUserByEmail(String email) throws DataAccessException {
         Query query = getEntityManager().createQuery("from User u Where u.fEmail = :email");
         query.setParameter("email", email);
         List<User> result = query.getResultList();
         return result.get(0);
     }
 
     @Override
     public User getUserByFbUserId(String fbUserId) throws DataAccessException {
         Query query = getEntityManager().createQuery("select u from User u Where u.fFBUserID = :fbUserId");
         query.setParameter("fbUserId", fbUserId);
         List<User> result = query.getResultList();
        return result.get(0);
     }
 
     @Override
     public List<User> getAllUsers() throws DataAccessException {
         Query query = getEntityManager().createQuery("select u from User u");
         List<User> result = query.getResultList();
         return result;
     }
 }
