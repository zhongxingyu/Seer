 package com.jweekend.data.dao.interfaces;
 
 import java.io.Serializable;
 import java.util.List;
 
 import com.jweekend.data.dataobjects.DomainObject;
 
 /**
  * Interface for managing a repository of objects of type T.
  * 
  * @author Richard Wilkinson - richard.wilkinson@jweekend.com
  * @param <T> The type of the objects
  * @param <K> The type of the primary key.
  */
 public interface Dao<T extends DomainObject, K extends Serializable> {
 	
 	/**
 	 * Delete object o from repository.
 	 * 
 	 * @param o
 	 */
 	public void delete(T o);
 
 	/**
 	 * Loads element of type T whose key is id.
 	 *  
 	 * @param id The primary key.
 	 * @return Object whose key is id.
 	 */
 	public T load(K id);
 
 	/**
 	 *  Persists object o into repository.
 	 *  
 	 * @param o
	 * @return
 	 */
 	public T save(T o);
 
 	/**
 	 * @return All element on repository of type T.
 	 */
 	public List<T> findAll();
 
 	/**
 	 * @return The number of elements of type T persisted on repository, 
 	 */
 	public int countAll();
 }
