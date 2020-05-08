 package pl.psnc.dl.wf4ever.db.dao;
 
 import java.io.Serializable;
 import java.util.List;
 
import org.apache.log4j.Logger;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Criterion;
 
 import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
 
 /**
  * DAO base, with generic methods for loading, saving and deleting objects.
  * 
  * @param <T>
  *            instance type
  * 
  * @author piotrek
  * 
  */
 public abstract class AbstractDAO<T> implements Serializable {
 
     /** id. */
     private static final long serialVersionUID = 258330617173783552L;
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(AbstractDAO.class);
 
 
     /**
      * Load an object.
      * 
      * @param clazz
      *            ActiveRecord class instance
      * @param id
      *            primary key
      * @return an object or null
      */
     @SuppressWarnings("unchecked")
     protected T findByPrimaryKey(Class<T> clazz, Serializable id) {
         Session session = HibernateUtil.getSessionFactory().getCurrentSession();
         if (session.getTransaction().isActive()) {
             T activeRecord = (T) session.get(clazz, id);
             return activeRecord;
         } else {
            LOGGER.warn("Transaction is not active");
             return null;
         }
     }
 
 
     /**
      * Find all objects.
      * 
      * @param clazz
      *            Active Record class instance
      * @return a list of all objects
      */
     protected List<T> findAll(Class<T> clazz) {
         return findByCriteria(clazz);
     }
 
 
     /**
      * Use this inside subclasses as a convenience method.
      * 
      * @param clazz
      *            class instance of DAO to be returned
      * @param criterion
      *            criteria for the search
      * @return list of DAOs
      */
     @SuppressWarnings("unchecked")
     protected List<T> findByCriteria(Class<T> clazz, Criterion... criterion) {
         Session session = HibernateUtil.getSessionFactory().getCurrentSession();
         Criteria crit = session.createCriteria(clazz);
         for (Criterion c : criterion) {
             crit.add(c);
         }
         return crit.list();
     }
 
 
     /**
      * Persist the object in the database.
      * 
      * @param instance
      *            object to save
      */
     public void save(T instance) {
         Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.saveOrUpdate(instance);
     }
 
 
     /**
      * Delete the object from the database.
      * 
      * @param instance
      *            object to delete
      */
     public void delete(T instance) {
         Session session = HibernateUtil.getSessionFactory().getCurrentSession();
         session.delete(instance);
     }
 
 }
