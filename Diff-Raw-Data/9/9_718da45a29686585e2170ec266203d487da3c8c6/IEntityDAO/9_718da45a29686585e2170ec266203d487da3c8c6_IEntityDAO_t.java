package sse.dao;
 
 import java.io.Serializable;
 
 import java.util.List;
 
 /**
  * Generic EntityDAO, providing basic CRUD operations
  *
  * @author Andrea Fueresz
  * 
  * @param <T> the entity type
  * @param <ID> the primary key type
  */
 public interface IEntityDAO<T, ID extends Serializable> {
 	
 	/**
      * Get the Class of the entity.
      *
      * @return the class
      */
     Class<T> getEntityClass();
     
     
     /**
      * Find an entity by its primary key
      *
      * @param id the primary key
      * @return the entity
      */
     T findById(ID id);
 
     /**
      * Load all entities.
      *
      * @return the list of entities
      */
     List<T> findAll();
 
     /**
      * Find using a named query.
      *
      * @param queryName the name of the query
      * @param params the query parameters
      *
      * @return the list of entities
      */
     List<T> findByNamedQuery(String queryName,Object... params);
 
     /**
      * Count all entities.
      *
      * @return the number of entities
      */
     int countAll();
     
     /**
      * save an entity. This can be either a INSERT or UPDATE in the database.
      * 
      * @param entity the entity to save
      * 
      * @return the saved entity
      */
     T save(T entity);
 
     /**
      * delete an entity from the database.
      * 
      * @param entity the entity to delete
      */
     void delete(T entity);
 	
 }
