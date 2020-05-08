 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.tgm.data.dao;
 
 import com.tgm.data.entity.EntityInterface;
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceContextType;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.dao.DataAccessException;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.Assert;
 
 /**
  *
  * @author christopher
  */
 public abstract class AbstractDao<E extends EntityInterface, I extends Serializable> {
 
     private Class<E> entityClass;
     private EntityInterface entityInterface;
     private EntityManager entityManager;
     @Autowired
     private ApplicationContext applicationContext;
     private String findAllQuery;
 
     protected AbstractDao(Class<E> entityClass) {
         Assert.isAssignable(EntityInterface.class, entityClass);
         this.entityClass = entityClass;
     }
 
     /*public void afterPropertiesSet() throws Exception {
      buildEntity();
      }*/
     public void buildEntity() {
         if (entityInterface == null) {
             entityInterface = (EntityInterface) createInstance();
             findAllQuery = "select o from " + entityInterface.getTable() + " o";
         }
     }
 
     public E createInstance() {
         return (E) getApplicationContext().getAutowireCapableBeanFactory().createBean(entityClass);
     }
 
     public E findById(I id) {
         return (E) getEntityManager().find(entityClass, id);
     }
 
     @Transactional(readOnly = true)
     public List<E> findAll() {
         buildEntity();
         return getEntityManager().createQuery(findAllQuery, entityClass).getResultList();
     }
 
     @Transactional(readOnly = true)
     public List<E> findAllAndFetch() {
         buildEntity();
         return fetch(getEntityManager().createQuery(findAllQuery, entityClass).getResultList());
     }
 
     @Transactional
     public E saveOrUpdate(E e) {
         return getEntityManager().merge(e);
     }
 
     @Transactional
     public void delete(E e) {
         getEntityManager().remove(e);
     }
 
     @Transactional(readOnly = true)
     public List<E> fetch(List<E> entities) throws DataAccessException {
         for (E entity : entities) {
             ((EntityInterface) entity).fetch();
         }
         return entities;
     }
 
     /**
      * @return the applicationContext
      */
     public ApplicationContext getApplicationContext() {
         return applicationContext;
     }
 
     /**
      * @param applicationContext the applicationContext to set
      */
     public void setApplicationContext(ApplicationContext applicationContext) {
         this.applicationContext = applicationContext;
     }
 
     /**
      * @return the entityManager
      */
     public EntityManager getEntityManager() {
         return entityManager;
     }
 
     /**
      * @param entityManager the entityManager to set
      */
     @PersistenceContext(type = PersistenceContextType.TRANSACTION)
     public void setEntityManager(EntityManager entityManager) {
         this.entityManager = entityManager;
     }
 }
