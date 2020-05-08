 package org.lazydog.repository.jpa;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.TypedQuery;
 import org.lazydog.repository.Criteria;
 import org.lazydog.repository.Repository;
 import org.lazydog.repository.jpa.internal.CriteriaImpl;
 
 
 /**
  * Abstract repository implemented using the Java Persistence API.
  * 
  * @author  Ron Rickard
  */
 public abstract class AbstractRepository implements Repository {
 
     private EntityManager entityManager;
 
     /**
      * Create the query.
      *
      * @param  entityClass          the entity class.
      * @param  queryLanguageString  the query language string.
      * @param  queryParameters      the query parameter map.
      * @param  queryHints           the query hint map.
      * 
      * @return  the query.
      *
      * @throws  IllegalArgumentException  if the entity class or query language string are invalid.
      */
     private <T> TypedQuery<T> createQuery(final Class<T> entityClass, final String queryLanguageString, final Map<String, Object> queryParameters, final Map<Object, String> queryHints) {
 
         // Check if the entity class is null.
         if (entityClass == null) {
             throw new IllegalArgumentException("The entity class is invalid.");
         }
 
         // Check if the query language string is null.
         if (queryLanguageString == null) {
             throw new IllegalArgumentException("The query language string is invalid.");
         }
 
         // Create the query using the query language string.
         TypedQuery<T> query = this.entityManager.createQuery(queryLanguageString, entityClass);
 
         // Loop through the hints.
        for(Object key : queryHints.keySet()) {
 
             // Set the query hints.
             query.setHint(queryHints.get(key), key);
         }
 
         // Loop through the parameters.
        for(String key : queryParameters.keySet()) {
 
             // Set the query parameters.
             query.setParameter(key, queryParameters.get(key));
         }
 
         return query;
     }
 
     /**
      * Find the entity.
      *
      * @param  entityClass  the entity class.
      * @param  id           the ID.
      * 
      * @return  the entity.
      */
     @Override
     public <T,U> T find(final Class<T> entityClass, final U id) {
         return this.entityManager.find(entityClass, id);
     }
       
     /**
      * Find the entity.
      *
      * @param  entityClass  the entity class.
      * @param  criteria     the criteria.
      * 
      * @return  the entity.
      */
     @Override
     public <T> T find(final Class<T> entityClass, final Criteria<T> criteria) {
         return this.find(entityClass, ((CriteriaImpl<T>)criteria).getQueryLanguageString(), 
                 ((CriteriaImpl<T>)criteria).getQueryParameters(), ((CriteriaImpl<T>)criteria).getQueryHints());
     }
 
     /**
      * Find the entity.
      * 
      * @param  entityClass          the entity class.
      * @param  queryLanguageString  the query language string.
      * @param  queryParameters      the query parameter map.
      * @param  queryHints           the query hint map.
      * 
      * @return  the entity.
      */
     protected <T> T find(final Class<T> entityClass, final String queryLanguageString, final Map<String, Object> queryParameters, final Map<Object, String> queryHints) {
         
         // Initialize.
         T entity = null;
         
         try {
 
             // Get the entity.
             entity = this.createQuery(entityClass, queryLanguageString, queryParameters, queryHints).getSingleResult();
         }
         catch(NoResultException e) {
             // Ignore.
         }
 
         return entity;
     }
     
     /**
      * Find the list of entities.
      *
      * @param  entityClass  the entity class.
      * 
      * @return  the list of entities.
      */
     @Override
     public <T> List<T> findList(final Class<T> entityClass) {
         return this.findList(entityClass, this.getCriteria(entityClass));
     }
 
     /**
      * Find the list of entities.
      *
      * @param  entityClass  the entity class.
      * @param  criteria     the criteria.
      *
      * @return  the list of entities.
      */
     @Override
     public <T> List<T> findList(final Class<T> entityClass, final Criteria<T> criteria) {
         return this.findList(entityClass, ((CriteriaImpl<T>)criteria).getQueryLanguageString(), 
                 ((CriteriaImpl<T>)criteria).getQueryParameters(), ((CriteriaImpl<T>)criteria).getQueryHints());
     }
 
     /**
      * Find the list of entities.
      * 
      * @param  entityClass          the entity class.
      * @param  queryLanguageString  the query language string.
      * @param  queryParameters      the query parameter map.
      * @param  queryHints           the query hint map.
      * 
      * @return  the list of entities.
      */
     protected <T> List<T> findList(final Class<T> entityClass, final String queryLanguageString, final Map<String, Object> queryParameters, final Map<Object, String> queryHints) {
         return this.createQuery(entityClass, queryLanguageString, queryParameters, queryHints).getResultList();
     }
     
     /**
      * Get the criteria.
      *
      * @param  entityClass  the entity class.
      *
      * @return  the criteria.
      */
     @Override
     public <T> Criteria<T> getCriteria(final Class<T> entityClass) {
         return new CriteriaImpl<T>(entityClass);
     }
 
     /**
      * Get the entity manager.
      * 
      * @return  the entity manager.
      */
     protected EntityManager getEntityManager() {
         return this.entityManager;
     }
 
     /**
      * Persist the entity.
      *
      * @param  entity  the entity.
      *
      * @return  the persisted entity.
      */
     @Override
     public <T> T persist(final T entity) {
 
         // Persist the entity.
         T persistedEntity = this.entityManager.merge(entity);
         this.entityManager.flush();
 
         return persistedEntity;
     }
 
     /**
      * Persist the list of entities.
      *
      * @param  entities  the entities.
      *
      * @return  the persisted list of entities.
      */
     @Override
     public <T> List<T> persistList(final List<T> entities) {
 
         // Initialize.
         List<T> persistedEntities = new ArrayList<T>();
 
         // Loop through the entities.
         for (T entity : entities) {
 
             // Persist the entity.
             T persistedEntity = this.persist(entity);
 
             // Add the persisted entity.
             persistedEntities.add(persistedEntity);
         }
 
         // Clear the entity manager.
         this.entityManager.clear();
         
         return persistedEntities;
     }
 
     /**
      * Remove the entity specified by ID.
      *
      * @param  entityClass  the entity class.
      * @param  id           the ID.
      */
     @Override
     public <T,U> void remove(final Class<T> entityClass, final U id) {
 
         // Get the entity.
         T entity = this.entityManager.getReference(entityClass, id);
 
         // Remove the entity.
         this.entityManager.remove(entity);
     }
 
     /**
      * Remove the entities specified by the list of IDs.
      *
      * @param  entityClass  the entity class.
      * @param  ids          the IDs.
      */
     @Override
     public <T,U> void removeList(final Class<T> entityClass, final List<U> ids) {
 
         // Loop through the IDs.
         for (U id: ids) {
 
             // Remove the entity.
             this.remove(entityClass, id);
         }
     }
 
     /**
      * Set the entity manager.
      *
      * @param  entityManager  the entity manager.
      */
     protected void setEntityManager(final EntityManager entityManager) {
         this.entityManager = entityManager;
     }
 }
