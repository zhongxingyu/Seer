 package org.kalibro.core.persistence;
 
 import java.util.Collection;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.persistence.StoredProcedureQuery;
 import javax.persistence.TypedQuery;
 
 /**
  * Facilitates interaction with {@link EntityManager}, from the Java Persistence API.
  * 
  * @author Carlos Morais
  */
 class RecordManager {
 
 	private EntityManager entityManager;
 
 	RecordManager(EntityManager entityManager) {
 		this.entityManager = entityManager;
 	}
 
 	Query createQuery(String queryString) {
 		clear();
 		return entityManager.createQuery(queryString);
 	}
 
 	<T> TypedQuery<T> createQuery(String queryString, Class<T> resultClass) {
 		clear();
 		return entityManager.createQuery(queryString, resultClass);
 	}
 
 	StoredProcedureQuery createProcedureQuery(String procedureName) {
 		clear();
		return entityManager.createStoredProcedureQuery(procedureName);
 	}
 
 	<T> T getById(Long id, Class<T> recordClass) {
 		clear();
 		return entityManager.find(recordClass, id);
 	}
 
 	<T> T save(T record) {
 		clear();
 		beginTransaction();
 		T merged = entityManager.merge(record);
 		entityManager.persist(merged);
 		commitTransaction();
 		return merged;
 	}
 
 	<T> void saveAll(Collection<T> records) {
 		if (records.isEmpty())
 			return;
 		clear();
 		beginTransaction();
 		for (T record : records)
 			entityManager.persist(entityManager.merge(record));
 		commitTransaction();
 	}
 
 	void removeById(Long id, Class<?> recordClass) {
 		beginTransaction();
 		entityManager.remove(getById(id, recordClass));
 		commitTransaction();
 	}
 
 	private void clear() {
 		entityManager.clear();
 	}
 
 	private void beginTransaction() {
 		entityManager.getTransaction().begin();
 	}
 
 	private void commitTransaction() {
 		entityManager.getTransaction().commit();
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		super.finalize();
 		if (entityManager.isOpen())
 			entityManager.close();
 	}
 }
