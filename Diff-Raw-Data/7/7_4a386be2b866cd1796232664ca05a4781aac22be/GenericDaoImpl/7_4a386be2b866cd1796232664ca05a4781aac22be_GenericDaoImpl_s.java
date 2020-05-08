 package oshop.dao;
 
 import org.hibernate.Criteria;
 import org.hibernate.FlushMode;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.springframework.context.MessageSource;
 import org.springframework.context.i18n.LocaleContextHolder;
 import oshop.dao.exception.NotFoundException;
 import oshop.model.BaseEntity;
 
 import javax.annotation.Resource;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 
 public class GenericDaoImpl<T extends BaseEntity<ID>, ID extends Serializable> implements GenericDao<T, ID> {
 
     @Resource
     private SessionFactory sessionFactory;
 
     @Resource
     private MessageSource messageSource;
 
     private Class<T> entityClass;
     private Integer listLimit = 1000;
 
     public GenericDaoImpl() {
     }
 
     public void setEntityClass(Class<T> entityClass) {
         this.entityClass = entityClass;
     }
 
     public void setListLimit(Integer listLimit) {
         this.listLimit = listLimit;
     }
 
     private Session getSession() {
         Session currentSession = sessionFactory.getCurrentSession();
         currentSession.setFlushMode(FlushMode.AUTO);
         return currentSession;
     }
 
     private Session getReadOnlySession() {
         Session currentSession = sessionFactory.getCurrentSession();
         currentSession.setFlushMode(FlushMode.MANUAL);
         return currentSession;
     }
 
     public Criteria createCriteria() {
         return getReadOnlySession().createCriteria(entityClass);
     }
 
     @SuppressWarnings("unchecked")
     public List<T> list(Criteria criteria) {
         return criteria.list();
     }
 
     @SuppressWarnings("unchecked")
     public T get(ID id) {
         T entity = (T) getReadOnlySession().get(entityClass, id);
         if (entity != null) {
             return entity;
         } else {
             throw new NotFoundException(
                     messageSource.getMessage("error.entity.by.id.not.found",
                             new Object[]{id}, LocaleContextHolder.getLocale()));
         }
     }
 
     @SuppressWarnings("unchecked")
     public T get(Criteria criteria) {
         T entity = (T)criteria.uniqueResult();
         if (entity != null) {
             return entity;
         } else {
             throw new NotFoundException(
                     messageSource.getMessage("error.entity.not.found",
                             new Object[]{entityClass}, LocaleContextHolder.getLocale()));
         }
     }
 
     public List<T> list(Integer page, Integer limit) {
         return list(null, page, limit);
     }
 
     public List<T> list(Criteria criteria, Integer page, Integer limit) {
         if (criteria == null) {
             criteria = createCriteria();
         }
 
         int safePageNumber = page != null ? page : 0;
         int safeLimitNumber = (limit != null && limit > 0) ? limit : listLimit;
 
         criteria.setMaxResults(safeLimitNumber).setFirstResult(safePageNumber * safeLimitNumber);
 
         return list(criteria);
     }
 
     @SuppressWarnings("unchecked")
     public ID add(T entity) {
         if (entity.getId() != null) {
             throw new IllegalStateException("Id should be empty for new object");
         }
 
         entity.setLastUpdate(new Date());
         return (ID)getSession().save(entity);
     }
 
     public void update(T entity) {
         entity.setLastUpdate(new Date());
         getSession().update(getSession().merge(entity));
     }
 
     public void remove(ID id) {
         T entity = get(id);
         if (entity == null) {
             throw new NotFoundException(
                     messageSource.getMessage("error.entity.by.id.not.found",
                             new Object[]{id}, LocaleContextHolder.getLocale()));
         }
 
         remove(entity);
     }
 
     public void remove(T entity) {
         getSession().delete(entity);
     }
 }
