 package com.pk.cwierkacz.model.service;
 
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Restrictions;
 
 import com.pk.cwierkacz.model.dao.SessionDao;
 import com.pk.cwierkacz.model.dao.UserDao;
 
 public class SessionService extends AbstractService<SessionDao>
 {
 
     public SessionService( SessionFactory sessionFactory ) {
         super(sessionFactory);
     }
 
     public SessionDao getByUser( UserDao userDao ) {
         Criteria criteria = getCriteria(UserDao.class);
         criteria.add(Restrictions.eq("id", userDao.getId()));
         UserDao dao = (UserDao) criteria.uniqueResult();
         return dao.getSession();
     }
 
     @SuppressWarnings( "unchecked" )
     public List<SessionDao> getAll( ) {
         Criteria criteria = getCriteria(SessionDao.class);
        return criteria.list();
     }
 
     public void deleteSession( SessionDao sessionDao ) {
         Session session = sessionFactory.getCurrentSession();
         session.beginTransaction();
 
         session.delete(sessionDao);
         session.getTransaction().commit();
     }
 
     public SessionDao getByToken( Long token ) {
         Criteria criteria = getCriteria(SessionDao.class);
         criteria.add(Restrictions.eq("currentToken", token));
         SessionDao sessionDao = (SessionDao) criteria.uniqueResult();
         commit();
         return sessionDao;
     }
 
 }
