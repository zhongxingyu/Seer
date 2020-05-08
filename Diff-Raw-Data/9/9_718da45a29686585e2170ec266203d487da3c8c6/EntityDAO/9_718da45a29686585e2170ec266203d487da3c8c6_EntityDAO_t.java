package sse.dao.hibernate;
 
 import java.io.Serializable;
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 
 import src.sse.dao.IEntityDAO;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Projections;
 
 /**
  * JPA implementation of the EntityDAO. Note that this implementation
  * also expects Hibernate as JPA implementation. That's because we like the
  * Criteria API.
  * 
  * @author Andrea Fueresz
  * 
  * @param <T>
  *            The persistent type
  * @param <ID>
  *            The primary key type
  */
 public class EntityDAO<T, ID extends Serializable> implements IEntityDAO<T, ID> {
 	
 	private Class<T> persistentClass;
 	private EntityManager em;
 	
 	@SuppressWarnings("unchecked")
 	public EntityDAO() {
 		this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
 				.getGenericSuperclass()).getActualTypeArguments()[0];
 	}
 
 	public EntityDAO(Class<T> persistentClass) {
 		super();
 		this.persistentClass = persistentClass;
 	}
 	
 	/**
 	 * @see src.sse.dao.IEntityDAO#countAll()
 	 */
 	@Override
 	public int countAll() {
 		return countByCriteria();
 	}
 
 	/**
 	 * @see src.sse.dao.IEntityDAO#delete(java.lang.Object)
 	 */
 	@Override
 	public void delete(T entity) {
 		getEntityManager().remove(entity);
 	}
 
 	/**
 	 * @see src.sse.dao.IEntityDAO#findAll()
 	 */
 	@Override
 	public List<T> findAll() {
 		return findByCriteria();
 	}
 
 	/**
 	 * @see src.sse.dao.IEntityDAO#findById(java.io.Serializable)
 	 */
 	@Override
 	public T findById(ID id) {
 		T result = getEntityManager().find(persistentClass, id);
 		return result;
 	}
 
 	/**
 	 * @see src.sse.dao.IEntityDAO#findByNamedQuery(java.lang.String, java.lang.Object[])
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<T> findByNamedQuery(String name, Object... params) {
 		Query query = getEntityManager().createNamedQuery(name);
 
 		for (int i = 0; i < params.length; i++) {
 			query.setParameter(i + 1, params[i]);
 		}
 
 		List<T> result = (List<T>) query.getResultList();
 		return result;
 	}
 
 	/**
 	 * @see src.sse.dao.IEntityDAO#getEntityClass()
 	 */
 	@Override
 	public Class<T> getEntityClass() {
 		return persistentClass;
 	}
 
 	/**
 	 * @see src.sse.dao.IEntityDAO#save(java.lang.Object)
 	 */
 	@Override
 	public T save(T entity) {
 		T savedEntity = getEntityManager().merge(entity);
 		return savedEntity;
 	}
 	
 	/**
 	 * set the JPA entity manager to use.
 	 *
 	 * @param entityManager
 	 */
 	@PersistenceContext
 	public void setEntityManager(EntityManager entityManager) {
 		this.em = entityManager;
 	}
 	
 	public EntityManager getEntityManager() {
 		return em;
 	}
 	
 	protected int countByCriteria(Criterion... criterion) {
 		Session session = (Session) getEntityManager().getDelegate();
 		Criteria crit = session.createCriteria(getEntityClass());
 		crit.setProjection(Projections.rowCount());
 
 		for (Criterion c : criterion) {
 			crit.add(c);
 		}
 
 		return (Integer) crit.list().get(0);
 	}
 
 	/**
      * Use this inside subclasses as a convenience method.
      */
     @SuppressWarnings("unchecked")
     protected List<T> findByCriteria(Criterion... criterion) {
         Criteria crit = getSession().createCriteria(getPersistentClass());
         for (Criterion c : criterion) {
             crit.add(c);
         }
         return crit.list();
    }
 }
