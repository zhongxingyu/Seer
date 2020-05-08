 package com.aciertoteam.common.repository.impl;
 
 import com.aciertoteam.common.interfaces.IAbstractEntity;
 import com.aciertoteam.common.repository.AbstractRepository;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.lang.reflect.ParameterizedType;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @param <T> IAbstractEntity
  * @author Bogdan Nechyporenko
  */
 @SuppressWarnings("unchecked")
 public abstract class DefaultAbstractRepository<T extends IAbstractEntity> implements AbstractRepository<T> {
 
     @Autowired
     private SessionFactory sessionFactory;
 
     @Override
     @Transactional(readOnly = true)
     public List<T> getAll() {
         return getSession().createQuery("from " + getClazz().getSimpleName()).list();
     }
 
     @Override
     @Transactional(readOnly = true)
     public T get(Long id) {
         return (T) getSession().createCriteria(getClazz()).add(Restrictions.eq("id", id)).uniqueResult();
     }
 
     @Override
     public void saveAll(Collection<T> coll) {
         for (T t : coll) {
             save(t);
         }
     }
 
     @Override
     public T save(T t) {
         getSession().save(t);
         return t;
     }
 
     @Override
     public T findByField(String fieldName, Object value) {
         Criteria criteria = getSession().createCriteria(getClazz());
         return (T) criteria.add(Restrictions.like(fieldName, value)).uniqueResult();
     }
 
     @Override
     public Collection<T> findCollectionByField(String fieldName, Object value) {
         Criteria criteria = getSession().createCriteria(getClazz());
         return criteria.add(Restrictions.like(fieldName, value)).list();
     }
 
     @Override
     public T saveOrUpdate(T t) {
         t.check();
        getSession().saveOrUpdate(t);
        return t;
     }
 
     @Override
     public void markAsDeleted(Long id) {
         T t = get(id);
         t.closeEndPeriod();
         saveOrUpdate(t);
     }
 
     @Override
     public void markAsDeleted(List<T> entities) {
         for (T t : entities) {
             t.closeEndPeriod();
             saveOrUpdate(t);
         }
     }
 
     @Override
     public void markAsDeleted(T entity) {
         entity.closeEndPeriod();
         saveOrUpdate(entity);
     }
 
     @Override
     public void delete(Long id) {
         getSession().delete(get(id));
     }
 
     @Override
     public void delete(List<T> entities) {
         for (T entity : entities) {
             delete(entity);
         }
     }
 
     @Override
     public void delete(T entity) {
         getSession().delete(entity);
     }
 
     @Override
     public void deleteAll() {
         getSession().createQuery("delete from " + getClazz().getSimpleName()).executeUpdate();
     }
 
     @Override
     public List<T> getList(List<Long> ids) {
         return getSession().createCriteria(getClazz()).add(Restrictions.in("id", ids)).list();
     }
 
     @Override
     public Set<T> getSet(List<Long> ids) {
         return new HashSet<T>(getList(ids));
     }
 
     @Override
     public long count() {
         return Long.valueOf(String.valueOf(getSession().createCriteria(getClazz())
                 .setProjection(Projections.rowCount()).uniqueResult()));
     }
 
     @Override
     public boolean isEmpty() {
         return count() == 0;
     }
 
     public Class getClazz() {
         ParameterizedType superClass = (ParameterizedType) this.getClass().getGenericSuperclass();
         return (Class) superClass.getActualTypeArguments()[0];
     }
 
     protected final Session getSession() {
         return sessionFactory.getCurrentSession();
     }
 
 }
