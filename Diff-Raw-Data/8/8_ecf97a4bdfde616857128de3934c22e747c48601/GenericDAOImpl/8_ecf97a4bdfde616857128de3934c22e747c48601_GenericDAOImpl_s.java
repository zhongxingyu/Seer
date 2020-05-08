 package fr.xebia.training.core;
 
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.DetachedCriteria;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.io.Serializable;
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 
 /**
  * GenericDAOImpl -
  *
  * @author spark <gayvallet@fullsix.com>
  */
 public abstract class GenericDAOImpl<K extends Serializable, T> extends HibernateDaoSupport implements GenericDAO<K, T> {
 
     private Class<T> type;
 
     @Autowired
     public void injectSessionFactory(SessionFactory sessionFactory) {
        setSessionFactory(sessionFactory);
     }
 
     public Class<T> getType() {
         return type;
     }
 
     protected DetachedCriteria getDetachedCriteria() {
         return DetachedCriteria.forClass(getType());
     }
 
     public GenericDAOImpl() {
         this.type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
     }
 
     @Transactional(readOnly = true)
     @Override
     public T findById(K id) {
         return (T) getHibernateTemplate().get(getType(), id);
     }
 
     @Transactional
     @Override
     public void delete(T obj) {
       getHibernateTemplate().delete(obj);
     }
 
     @Transactional
     @Override
     public void saveOrUpdate(T obj) {
        getHibernateTemplate().saveOrUpdate(obj);
     }
 
     @Transactional(readOnly = true)
     @Override
     public List<T> findAll() {
         DetachedCriteria criteria = getDetachedCriteria();
         return getHibernateTemplate().findByCriteria(criteria);
     }
 }
