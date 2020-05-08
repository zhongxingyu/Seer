 package au.org.scoutmaster.dao;
 
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.persistence.metamodel.SingularAttribute;
 
 import au.com.vaadinutils.dao.EntityManagerProvider;
 
 import com.google.gwt.thirdparty.guava.common.base.Preconditions;
 import com.vaadin.addon.jpacontainer.JPAContainer;
 import com.vaadin.addon.jpacontainer.JPAContainerFactory;
 
 public abstract class JpaBaseDao<E, K> implements Dao<E, K>
 {
 	protected Class<E> entityClass;
 
 	protected EntityManager entityManager;
 
 	@SuppressWarnings("unchecked")
 	public JpaBaseDao()
 	{
 		this.entityManager = EntityManagerProvider.INSTANCE.getEntityManager();
 		Preconditions.checkNotNull(this.entityManager);
 		
 		// hack to get the derived classes Class type.
 		ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
 		Preconditions.checkNotNull(genericSuperclass);
 		this.entityClass = (Class<E>) genericSuperclass.getActualTypeArguments()[0];
 		Preconditions.checkNotNull(this.entityClass);
 	}
 
 	@SuppressWarnings("unchecked")
 	public JpaBaseDao(EntityManager em)
 	{
 		this.entityManager = em;
 		ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
 		this.entityClass = (Class<E>) genericSuperclass.getActualTypeArguments()[0];
 	}
 
 	public void persist(E entity)
 	{
 		entityManager.persist(entity);
 	}
 
 	public E merge(E entity)
 	{
 		return entityManager.merge(entity);
 	}
 
 	public void remove(E entity)
 	{
 		entityManager.remove(entity);
 	}
 
 	public E findById(K id)
 	{
 		return entityManager.find(entityClass, id);
 	}
 	
 	protected E findSingleBySingleParameter(String queryName, SingularAttribute<E, String> paramName, String paramValue)
 	{
 		E entity = null;
 		Query query = entityManager.createNamedQuery(queryName);
 		query.setParameter(paramName.getName(), paramValue);
 		@SuppressWarnings("unchecked")
 		List<E> entities = query.getResultList();
 		if (entities.size() > 0)
 			entity = entities.get(0);
 		return entity;
 	}
 
 
 	protected E findSingleBySingleParameter(String queryName, String paramName, String paramValue)
 	{
 		E entity = null;
 		Query query = entityManager.createNamedQuery(queryName);
 		query.setParameter(paramName, paramValue);
 		@SuppressWarnings("unchecked")
 		List<E> entities = query.getResultList();
 		if (entities.size() > 0)
 			entity = entities.get(0);
 		return entity;
 	}
 
 	protected List<E> findListBySingleParameter(String queryName, String paramName, String paramValue)
 	{
 		Query query = entityManager.createNamedQuery(queryName);
 		query.setParameter(paramName, paramValue);
 		@SuppressWarnings("unchecked")
 		List<E> entities = query.getResultList();
 		return entities;
 	}
 
 	protected List<E> findAll(String queryName)
 	{
 		Query query = entityManager.createNamedQuery(queryName);
 		@SuppressWarnings("unchecked")
 		List<E> list = query.getResultList();
 		return list;
 	}
 
 	public void flush()
 	{
 		this.entityManager.flush();
 
 	}
 
 	public JPAContainer<E> makeJPAContainer(Class<E> clazz)
 	{
		JPAContainer<E> container = JPAContainerFactory.make(clazz, EntityManagerProvider.INSTANCE.getEntityManager());
 
 		return container;
 	}
 	
 	abstract public JPAContainer<E> makeJPAContainer();
 
 
 }
