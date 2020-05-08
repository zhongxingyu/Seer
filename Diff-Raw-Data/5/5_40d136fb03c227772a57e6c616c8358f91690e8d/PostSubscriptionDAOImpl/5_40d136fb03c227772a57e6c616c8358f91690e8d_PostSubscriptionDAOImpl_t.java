 package com.mycompany.dao.impl;
 
 import com.mycompany.dao.exception.DuplicatedPostSubscriptionException;
 import com.mycompany.dao.PostSubscriptionDAO;
 import com.mycompany.dao.exception.NullPostException;
 import com.mycompany.dao.exception.NullUserException;
 import com.mycompany.dao.exception.PostSubscriptionNotFoundException;
 import com.mycompany.entity.Post;
 import com.mycompany.entity.PostSubscription;
 import com.mycompany.entity.User;
 import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import java.util.List;
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: fermin
  * Date: 8/12/12
  * Time: 17:27
  * To change this template use File | Settings | File Templates.
  */
 @Repository
 public class PostSubscriptionDAOImpl implements PostSubscriptionDAO {
 
     @PersistenceContext
     private EntityManager em;
 
     public PostSubscriptionDAOImpl() {
     }
 
     public PostSubscriptionDAOImpl(EntityManager em) {
         this.em = em;
     }
 
    @Transactional
     public PostSubscription create(Post p, User u, boolean read) throws DuplicatedPostSubscriptionException, NullPostException,
         NullUserException {
 
         if (p == null) {
             throw new NullPostException();
         }
         if (u == null) {
             throw new NullUserException();
         }
 
         PostSubscription ps = new PostSubscription();
         ps.setPost(p);
         ps.setUser(u);
         ps.setIsRead(read);
 
         // TODO: a try-catch need to be done to detect and raise DuplicatedFeedSubscriptionException
         em.persist(ps);
 
         return ps;
 
     }
 
     public PostSubscription load(Post p, User u) throws PostSubscriptionNotFoundException, NullPostException,
         NullUserException {
         if (p == null) {
             throw new NullPostException();
         }
         if (u == null) {
             throw new NullUserException();
         }
 
         Query q = em.createQuery("SELECT ps FROM PostSubscription ps WHERE ps.post.id = ?1 AND ps.user.email = ?2");
         q.setParameter(1,p.getId());
         q.setParameter(2,u.getEmail());
 
         try {
             /* Note that, by construction, as much one result is obtained. If this
                fails, an un-cached error will be raised when invoking getSingleResult */
             return (PostSubscription) q.getSingleResult();
         }
         catch (NoResultException e) {
             throw new PostSubscriptionNotFoundException();
         }
 
     }
 
    @Transactional
     /* We use a similar pattern to the one shown in the JPA tutorial:
        http://docs.oracle.com/javaee/6/tutorial/doc/bnbqw.html#bnbre */
     public void delete(Post p, User u) throws PostSubscriptionNotFoundException, NullPostException, NullUserException {
         PostSubscription ps = load(p, u);
         em.remove(ps);
     }
 
     public List<PostSubscription> findAllByPost(Post p, int limit, int offset) throws NullPostException {
         if (p == null) {
             throw new NullPostException();
         }
 
         if ((limit <= 0) || (offset < 0)) {
             /* Returning empty list in this case */
             return new Vector<PostSubscription>();
         }
 
         Query q = em.createQuery("SELECT ps FROM PostSubscription ps WHERE ps.post.id = ?1 ORDER BY ps.user.email");
         q.setParameter(1,p.getId());
         q.setMaxResults(limit);
         q.setFirstResult(offset);
 
         return q.getResultList();
     }
 
     public int countAllByPost(Post p) throws NullPostException{
 
         if (p == null) {
             throw new NullPostException();
         }
 
         Query q = em.createQuery("SELECT COUNT(*) FROM PostSubscription ps WHERE ps.post.id = ?1");
         q.setParameter(1,p.getId());
 
         return ((Long) q.getSingleResult()).intValue();
     }
 
     public List<PostSubscription> findAllByUser(User u, int limit, int offset) throws NullUserException {
 
         if (u == null) {
             throw new NullUserException();
         }
 
         if ((limit <= 0) || (offset < 0)) {
             /* Returning empty list in this case */
             return new Vector<PostSubscription>();
         }
 
         Query q = em.createQuery("SELECT ps FROM PostSubscription ps WHERE ps.user.email = ?1 ORDER BY ps.post.id");
         q.setParameter(1,u.getEmail());
         q.setMaxResults(limit);
         q.setFirstResult(offset);
 
         return q.getResultList();
     }
 
     public int countAllByUser(User u) throws NullUserException {
 
         if (u == null) {
             throw new NullUserException();
         }
 
         Query q = em.createQuery("SELECT COUNT(*) FROM PostSubscription ps WHERE ps.user.email = ?1");
         q.setParameter(1,u.getEmail());
 
         return ((Long) q.getSingleResult()).intValue();
 
     }
 
 }
