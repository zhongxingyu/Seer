 // Copyright 2013 Structure Eng Inc.
 package com.structureeng.persistence.dao.impl;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import com.structureeng.persistence.dao.DAO;
 import com.structureeng.persistence.dao.DAOErrorCode;
 import com.structureeng.persistence.dao.DAOException;
 import com.structureeng.persistence.dao.QueryContainer;
 import com.structureeng.persistence.model.AbstractModel;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.LockModeType;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 
 /**
  * Provides the implementation for {@code DAO} interface.
  *
  * @author Edgar Rico (edgar.martinez.rico@gmail.com)
  * @param <T> specifies the {@code Model} of the data access object
  * @param <S> specifies the {@code Serializable} identifier of the {@code Model}
  */
 public final class DAOImpl<T extends AbstractModel, S extends Serializable> implements DAO<T, S> {
 
     private EntityManager entityManager;
     private final Class<T> entityClass;
     private final Class<S> entityIdClass;
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     @Inject
     public DAOImpl(Class<T> entityClass, Class<S> entityIdClass) {
         this.entityClass = checkNotNull(entityClass);
         this.entityIdClass = checkNotNull(entityIdClass);
     }
 
     @Override
     public void persist(T entity) throws DAOException {
         getEntityManager().persist(entity);
     }
 
     @Override
     public T merge(T entity) throws DAOException {
         if (entity.getId() == null) {
             persist(entity);
             return entity;
         } else {
             if (!hasVersionChanged(entity)) {
                return getEntityManager().merge(entity);
             } else {
                 throw DAOException.Builder.build(DAOErrorCode.OPRIMISTIC_LOCKING);
             }
         }
     }
 
     @Override
     public void remove(T entity) throws DAOException {
         getEntityManager().remove(entity);
     }
 
     @Override
     public void refresh(T entity) {
         getEntityManager().refresh(entity);
     }
 
     @Override
     public void refresh(T entity, LockModeType modeType) {
         getEntityManager().refresh(entity, modeType);
     }
 
     @Override
     public T find(S id) {
         try {
             return getEntityManager().find(getEntityClass(), id);
         } catch (NoResultException exc) {
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug(String.format(exc.getMessage().concat(" id [%s]."), id), exc);
             }
             return null;
         }
     }
 
     @Override
     public T find(S id, long version) throws DAOException {
         try {
             T current = getEntityManager().find(getEntityClass(), id);
             if (current.getVersion() == version) {
                 return current;
             } else {
                 throw DAOException.Builder.build(DAOErrorCode.OPRIMISTIC_LOCKING);
             }
         } catch (NoResultException exc) {
             throw DAOException.Builder.build(DAOErrorCode.NO_RESULT);
         }
     }
 
     @Override
     public List<T> findRange(int start, int end) {
         QueryContainer<T, T> qc = QueryContainer.newQueryContainer(getEntityManager(),
                 getEntityClass());
         return qc.getResultList(start, end - start);
     }
 
     @Override
     public long count() {
         QueryContainer<Long, T> qc = QueryContainer.newQueryContainerCount(getEntityManager(),
                 getEntityClass());
         return qc.getSingleResult();
     }
 
     @Override
     public void flush() {
         getEntityManager().flush();
     }
 
     @Override
     public boolean hasVersionChanged(T entity) {
         try {
             find(getEntityIdClass().cast(entity.getId()), entity.getVersion());
             return false;
         } catch (DAOException ex) {
             getLogger().debug(ex.getMessage());
             return true;
         }
     }
 
     /**
      * Provides the class of this dao.
      *
      * @return - the class of the dao
      */
     public Class<T> getEntityClass() {
         return entityClass;
     }
 
     /**
      * Provides the class of the Id.
      *
      * @return - the class of the Id of an entity.
      */
     public Class<S> getEntityIdClass() {
         return entityIdClass;
     }
 
     @PersistenceContext
     public void setEntityManager(EntityManager entityManager) {
         this.entityManager = checkNotNull(entityManager);
     }
 
     /**
      * Provides the {@code EntityManager} that is being use by the dao.
      *
      * @return - the instance
      */
     public EntityManager getEntityManager() {
         return entityManager;
     }
 
     /**
      * Provides the {@code Logger} of the concrete class.
      *
      * @return - the logger instance of the class.
      */
     public Logger getLogger() {
         return logger;
     }
 }
