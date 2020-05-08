 package kabbadi.domain.db;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.stereotype.Repository;
 
 import java.util.List;
 
 @Repository
 public class GenericRepository<T> {
     protected SessionFactory sessionFactory;
     private Class<T> type;
 
     private GenericRepository() {
     }
 
     public GenericRepository(SessionFactory sessionFactory, Class<T> type) {
         this.sessionFactory = sessionFactory;
         this.type = type;
     }
 
     public void saveOrUpdate(T o) {
         getSession().saveOrUpdate(o);
     }
 
     public T save(T o) {
         int id = (Integer) getSession().save(o);
         return get(id);
     }
 
     public T get(int id) {
         return (T) getSession().get(type, id);
     }
 
     public List<T> list() {
         return (List<T>) getSession().createCriteria(type).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
     }
 
     public void update(T o) {
         getSession().update(o);
     }
 
     public void delete(T o) {
         getSession().delete(o);
     }
 
     public List<T> findAll(String field, Object param) {
         return getSession().createCriteria(type).add(Restrictions.eq(field, param)).list();
     }
 
     protected Session getSession() {
         return sessionFactory.getCurrentSession();
     }
 
     public T findBy(String propertyName, String value) {
         return (T) this.sessionFactory.getCurrentSession().createCriteria(type).add(
                 Restrictions.eq(propertyName, value)).uniqueResult();
     }
 
     public List<T> findAllNotEqualTo(String field, Object param) {
        return getSession().createCriteria(type)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.not(Restrictions.eq(field, param)))
                .list();
     }
 
     public Criteria scoped() {
         return sessionFactory.getCurrentSession().createCriteria(type);
     }
 }
