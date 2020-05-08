 package com.brightinteractive.common.dao;
 
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * <p>An interface shared by all data access objects.</p>
  *
  * <p>
  * All CRUD (create, read, update, delete) basic data access operations are
  * isolated in this interface and shared across all DAO implementations.
  * The current design is for a state-management oriented persistence layer
  * (for example, there is no UPDATE statement function) that provides
  * automatic transactional dirty checking of business objects in persistent
  * state.
  * </p>
  *
  * @author Christian Bauer
  */
 public interface GenericDao<T, ID extends Serializable> {
 
    T findById(ID id);

    public T findByIdAndLock(ID id);
 
     List<T> findAll();
 
     List<T> findByExample(T exampleInstance, String... excludeProperty);
 
     T makePersistent(T entity);
 
     void makeTransient(T entity);
 
     /**
      * Affects every managed instance in the current persistence context!
      */
     void flush();
 
     /**
      * Affects every managed instance in the current persistence context!
      */
     void clear();
 }
