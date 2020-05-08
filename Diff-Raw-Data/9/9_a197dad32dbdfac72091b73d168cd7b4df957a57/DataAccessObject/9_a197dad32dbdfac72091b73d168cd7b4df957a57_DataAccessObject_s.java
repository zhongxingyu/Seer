 package org.lazydog.data.access;
 
 import java.util.List;
 
 
 /**
  * Data access object.
  *
  * @author  Ron Rickard
  */
 public interface DataAccessObject {
 
     /**
      * Find the entity.
      *
      * @param  id  the ID.
      *
      * @return  the entity.
      */
     public <T> T find(Class<T> entityClass, Integer id);
 
     /**
      * Find the entity.
      *
      * @param  criteria  the criteria.
      *
      * @return  the entity.
      */
     public <T> T find(Criteria<T> criteria);
 
     /**
      * Find the list of entities.
      *
      * @return  the list of entities.
      */
     public <T> List<T> findList(Class<T> entityClass);
 
     /**
      * Find the list of entities.
      *
      * @param  criteria  the criteria.
      *
      * @return  the list of entities.
      */
     public <T> List<T> findList(Criteria<T> criteria);
 
     /**
      * Persist the entity.
      *
      * @param  the entity.
      *
      * @return  the persisted entity.
      */
     public <T> T persist(T entity);
 
     /**
      * Persist the list of entities.
      *
      * @param  the entities.
      *
      * @return  the persisted list of entities.
      */
     public <T> List<T> persistList(List<T> entities);
 
     /**
      * Remove the entity.
      *
      * @param  entity  the entity.
      */
     public <T> void remove(T entity);
 
     /**
      * Remove the entity.
      *
      * @param  id  the ID.
      */
     public <T> void remove(Class<T> entityClass, Integer id);
 
     /**
      * Remove the list of entities.
      *
     * @param  the entities.
      */
     public <T> void removeList(List<T> entities);
 }
