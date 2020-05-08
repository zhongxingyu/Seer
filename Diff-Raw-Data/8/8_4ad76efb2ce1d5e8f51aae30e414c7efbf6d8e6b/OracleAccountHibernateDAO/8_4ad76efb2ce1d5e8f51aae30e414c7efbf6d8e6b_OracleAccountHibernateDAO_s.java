 package com.compiler.dao.hibernate;
 
 import com.compiler.dao.AccountDAO;
 import com.compiler.entity.GenericAccountInfo;
import com.compiler.utils.LoggerUtils;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 public class OracleAccountHibernateDAO implements AccountDAO {
     private static SessionFactory factory;
 
     public OracleAccountHibernateDAO()
     {
         this.factory = DBUtilsHibernate.getConnection();
     }
 
     @Override
     public Collection<GenericAccountInfo> getAccount() {
 
         Session session = factory.openSession();
         Transaction tx = null;
         List<GenericAccountInfo> accountsInfo = new ArrayList<GenericAccountInfo>();
         try {
             tx = session.beginTransaction();
             accountsInfo = session.createCriteria(GenericAccountInfo.class).list();
             tx.commit();
         } catch (HibernateException e) {
             if (tx != null)
                 tx.rollback();
             e.printStackTrace();
         } finally {
             session.close();
         }
         return accountsInfo;
     }
 
     public GenericAccountInfo getUser(String login) {
 
         Session session = factory.openSession();
         Transaction tx = null;
         List<GenericAccountInfo> accountsInfo = new ArrayList<GenericAccountInfo>();
         try {
             tx = session.beginTransaction();
             accountsInfo = session.createQuery("from GenericAccountInfo gai where gai.email= :login ").setParameter("login", login).list();
             tx.commit();
         } catch (HibernateException e) {
             if (tx != null)
                 tx.rollback();
             e.printStackTrace();
         } finally {
             session.close();
         }
        return accountsInfo != null ? accountsInfo.get(0) : null;
     }
 
     @Override
     public void insertUser(GenericAccountInfo users) {
         Session session = factory.openSession();
         Transaction tx = null;
         try {
             tx = session.beginTransaction();
             session.save(users);
             tx.commit();
         } catch (HibernateException e) {
             if (tx != null)
                 tx.rollback();
             e.printStackTrace();
         } finally {
             session.close();
         }
     }
 
     @Override
     public boolean deleteUser(GenericAccountInfo users) {
         Session session = factory.openSession();
         Transaction tx = null;
         try {
             tx = session.beginTransaction();
             session.delete(users);
             tx.commit();
         } catch (HibernateException e) {
             if (tx != null)
                 tx.rollback();
             e.printStackTrace();
         } finally {
             session.close();
         }
         return false;
     }
 
     @Override
     public GenericAccountInfo getUserById(Long id) {
         Session session = factory.openSession();
         Transaction tx = null;
         GenericAccountInfo accountsInfo = null;
         try {
             tx = session.beginTransaction();
             accountsInfo = (GenericAccountInfo) session.load(GenericAccountInfo.class, id);
             tx.commit();
         } catch (HibernateException e) {
             if (tx != null)
                 tx.rollback();
             e.printStackTrace();
         } finally {
             session.close();
         }
         return null;
     }
 
     @Override
     public void updateGenericAccountInfo(GenericAccountInfo users) {
         Session session = factory.openSession();
         Transaction tx = null;
         GenericAccountInfo accountsInfo = null;
         try {
             tx = session.beginTransaction();
             session.update(users);
             tx.commit();
         } catch (HibernateException e) {
             if (tx != null)
                 tx.rollback();
             e.printStackTrace();
         } finally {
             session.close();
         }
     }
 }
