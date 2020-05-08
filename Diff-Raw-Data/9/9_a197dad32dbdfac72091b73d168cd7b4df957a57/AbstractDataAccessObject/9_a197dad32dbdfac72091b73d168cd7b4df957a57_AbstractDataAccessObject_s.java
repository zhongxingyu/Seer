 package org.lazydog.data.access;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 
 
 /**
  * Abstract data access object implemented using the Java Persistence API.
  * 
  * @author  Ron Rickard
  */
 public abstract class AbstractDataAccessObject implements DataAccessObject {
 
     protected EntityManager entityManager;
 
     /**
      * Find the entity.
      *
      * @param  id  the ID.
      *
      * @return  the entity.
      */
     @Override
     public <T> T find(Class<T> entityClass, Integer id) {
         return this.entityManager.find(entityClass, id);
     }
       
     /**
      * Find the entity.
      *
      * @param  criteria  the criteria.
      * 
      * @return  the entity.
      */
     @Override
     @SuppressWarnings("unchecked")
     public <T> T find(Criteria<T> criteria) {
 
         // Declare.
         T result;
 
         // Initialize.
         result = null;
         
         try {
 
             // Declare.
             Query query;
 
             // Create the query.
             query = this.entityManager.createQuery(
                     criteria.getQlString());
 
             // Loop through the parameters.
             for(String key : criteria.getParameters().keySet()) {
 
                 // Set the query parameters.
                 query.setParameter(
                         key, criteria.getParameters().get(key));
             }
 
             // Get the query result.
             result = (T)query.getSingleResult();
         }
         catch(NoResultException e) {
             // Ignore.
         }
 
         return result;
     }
 
     /**
      * Find the list of entities.
      *
      * @return  the list of entities.
      */
     @Override
     @SuppressWarnings("unchecked")
     public <T> List<T> findList(Class<T> entityClass) {
 
         // Declare.
         List<T> entities;
         Query query;
 
         // Create the named query.
         query = this.entityManager.createNamedQuery(
             entityClass.getSimpleName() + ".findAll");
 
         // Get the query result.
         entities = query.getResultList();
 
         return entities;
     }
 
     /**
      * Find the list of entities.
      *
      * @param  criteria  the criteria.
      * 
      * @return  the list of entities.
      */
     @Override
     @SuppressWarnings("unchecked")
     public <T> List<T> findList(Criteria<T> criteria) {
 
         // Declare.
         List<T> entities;
         Query query;
         List result;
 
         // Initialize.
         entities = null;
 
         // Create the query.
         query = this.entityManager.createQuery(
                 criteria.getQlString());
 
         // Loop through the parameters.
         for(String key : criteria.getParameters().keySet()) {
 
             // Set the query parameters.
             query.setParameter(
                     key, criteria.getParameters().get(key));
         }
 
         // Get the query result.
         result = query.getResultList();
 
         // Check if the result is not null.
         if (result != null) {
 
             // Convert the result.
             entities = new ArrayList<T>(result);
         }
         
         return entities;
     }
 
     /**
      * Persist the entity.
      *
      * @param  the entity.
      *
      * @return  the persisted entity.
      */
     @Override
     public <T> T persist(T entity) {
         return this.entityManager.merge(entity);
     }
 
     /**
      * Persist the list of entities.
      *
      * @param  the entities.
      *
      * @return  the persisted list of entities.
      */
     @Override
     public <T> List<T> persistList(List<T> entities) {
         
         // Declare.
         List<T> persistedEntities;
         
         // Initialize.
         persistedEntities = new ArrayList<T>();
 
         // Loop through the entities.
         for (T entity : entities) {
 
             // Declare.
             T persistedEntity;
 
             // Persist the entity.
             persistedEntity = this.persist(entity);
 
             // Add the persisted entity.
             persistedEntities.add(persistedEntity);
         }
 
         return persistedEntities;
     }
 
     /**
      * Remove the entity.
      *
      * @param  entity  the entity.
      */
     @Override
     public <T> void remove(T entity) {
         this.entityManager.remove(entity);
     }
 
     /**
      * Remove the entity.
      *
      * @param  id  the ID.
      */
     @Override
     public <T> void remove(Class<T> entityClass, Integer id) {
 
         // Declare.
         T entity;
 
         // Get the entity.
         entity = this.entityManager.getReference(entityClass, id);
 
         // Remove the entity.
         this.entityManager.remove(entity);
     }
 
     /**
      * Remove the list of entities.
      *
     * @param  the entities.
      */
     @Override
     public <T> void removeList(List<T> entities) {
 
         // Loop through the entities.
         for (T entity : entities) {
 
             // Remove the entity.
             this.remove(entity);
         }
     }
 
     /**
      * Set the entity manager.
      *
      * @param  entityManager  the entity manager.
      */
     public abstract void setEntityManager(EntityManager entityManager);
 }
