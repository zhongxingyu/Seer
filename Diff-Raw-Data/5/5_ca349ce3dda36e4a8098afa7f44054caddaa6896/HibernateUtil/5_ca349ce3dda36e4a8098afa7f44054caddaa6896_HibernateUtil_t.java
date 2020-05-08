 package net.sf.gilead.core.hibernate;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.Map.Entry;
 
 import net.sf.beanlib.hibernate.UnEnhancer;
 import net.sf.gilead.core.IPersistenceUtil;
 import net.sf.gilead.core.serialization.SerializableId;
 import net.sf.gilead.exception.ComponentTypeException;
 import net.sf.gilead.exception.NotPersistentObjectException;
 import net.sf.gilead.exception.TransientObjectException;
 import net.sf.gilead.pojo.base.IUserType;
 import net.sf.gilead.util.IntrospectionHelper;
 
 import org.hibernate.EntityMode;
 import org.hibernate.Hibernate;
 import org.hibernate.HibernateException;
 import org.hibernate.ObjectNotFoundException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.collection.AbstractPersistentCollection;
 import org.hibernate.collection.PersistentBag;
 import org.hibernate.collection.PersistentCollection;
 import org.hibernate.collection.PersistentList;
 import org.hibernate.collection.PersistentMap;
 import org.hibernate.collection.PersistentSet;
 import org.hibernate.collection.PersistentSortedMap;
 import org.hibernate.collection.PersistentSortedSet;
 import org.hibernate.impl.SessionFactoryImpl;
 import org.hibernate.impl.SessionImpl;
 import org.hibernate.metadata.ClassMetadata;
 import org.hibernate.persister.collection.CollectionPersister;
 import org.hibernate.persister.entity.EntityPersister;
 import org.hibernate.proxy.HibernateProxy;
 import org.hibernate.tuple.IdentifierProperty;
 import org.hibernate.tuple.entity.EntityMetamodel;
 import org.hibernate.type.AbstractComponentType;
 import org.hibernate.type.CollectionType;
 import org.hibernate.type.Type;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Persistent helper for Hibernate implementation
  * Centralizes the SessionFactory and add some needed methods.
  * Not really a singleton, since there can be as many HibernateUtil instance as different sessionFactories
  * @author BMARCHESSON
  */
 public class HibernateUtil implements IPersistenceUtil
 {
 	//----
 	// Serialized proxy informations map constants
 	//----
 	/**
 	 * Proxy id
 	 */
 	private static final String ID="id";
 	
 	/**
 	 * Persistent collection class name
 	 */
 	private static final String CLASS_NAME="class";
 	
 	/**
 	 * Persistent collection role
 	 */
 	private static final String ROLE="role";
 	
 	/**
 	 * Persistent collection PK ids
 	 */
 	private static final String KEY="key";
 	
 	/**
 	 * Persistent collection ids list
 	 */
 	private static final String ID_LIST="idList";
 	
 	/**
 	 * Persistent map values list
 	 */
 	private static final String VALUE_LIST="valueList";
 	
 	//----
 	// Attributes
 	//----	
 	/**
 	 * Logger channel
 	 */
 	private static Logger _log = LoggerFactory.getLogger(HibernateUtil.class);
 	
 	/**
 	 * The pseudo unique instance of the singleton
 	 */
 	private static HibernateUtil _instance = null;
 	
 	/**
 	 * The Hibernate session factory
 	 */
 	private SessionFactoryImpl _sessionFactory;	
 	
 	/**
 	 * The persistance map, with persistance status of all classes
 	 * including persistent component classes
 	 */
 	private Map<Class<?>, Boolean> _persistenceMap;
 	
 	/**
 	 * The unenhancement map, used for performance purpose
 	 */
 	private Map<Class<?>, Class<?>> _unehancementMap;
 	
 	/**
 	 * The current opened session
 	 */
 	private ThreadLocal<HibernateSession> _session;
 	
 	//----
 	// Properties
 	//----
 	/**
 	 * @return the unique instance of the singleton
 	 */
 	public static HibernateUtil getInstance()
 	{
 		if (_instance == null)
 		{
 			_instance = new HibernateUtil();
 		}
 		return _instance;
 	}
 	
 	/**
 	 * @return the hibernate session Factory
 	 */
 	public SessionFactory getSessionFactory()
 	{
 		return _sessionFactory;
 	}
 
 	/**
 	 * @param sessionFactory the factory to set
 	 */
 	public void setSessionFactory(SessionFactory sessionFactory)
 	{
 		if ((sessionFactory != null) &&
 			(sessionFactory instanceof SessionFactoryImpl == false))
 		{
 		//	Probably a Spring injected session factory
 		//
 			sessionFactory = (SessionFactory) IntrospectionHelper.searchMember(SessionFactoryImpl.class, sessionFactory);
 			if (sessionFactory == null)
 			{
 				throw new IllegalArgumentException("Cannot find Hibernate session factory implementation !");
 			}
 		}
 		_sessionFactory = (SessionFactoryImpl) sessionFactory;
 	}
 	
 	
 	//-------------------------------------------------------------------------
 	//
 	// Constructor
 	//
 	//-------------------------------------------------------------------------
 	/**
 	 * Complete constructor
 	 */
 	public HibernateUtil(SessionFactory sessionFactory)
 	{
 		setSessionFactory(sessionFactory);
 		_session = new ThreadLocal<HibernateSession>();
 		_persistenceMap = new HashMap<Class<?>, Boolean>();
 		_unehancementMap = new HashMap<Class<?>, Class<?>>();
 		
 		// Filling persistence map with primitive types
 		_persistenceMap.put(Byte.class, false);
 		_persistenceMap.put(Short.class, false);
 		_persistenceMap.put(Integer.class, false);
 		_persistenceMap.put(Long.class, false);
 		_persistenceMap.put(Float.class, false);
 		_persistenceMap.put(Double.class, false);
 		_persistenceMap.put(Boolean.class, false);
 		_persistenceMap.put(String.class, false);
 	}
 	
 	/**
 	 * Empty constructor
 	 */
 	public HibernateUtil()
 	{
 		this(null);
 	}
 	
 	//-------------------------------------------------------------------------
 	//
 	// Public interface
 	//
 	//-------------------------------------------------------------------------
 	/* (non-Javadoc)
 	 * @see net.sf.gilead.core.hibernate.IPersistenceUtil#getId(java.lang.Object)
 	 */
 	public Serializable getId(Object pojo)
 	{
 		return getId(pojo, getPersistentClass(pojo));
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.sf.gilead.core.hibernate.IPersistenceUtil#getId(java.lang.Object, java.lang.Class)
 	 */
 	public Serializable getId(Object pojo, Class<?> hibernateClass)
 	{
 	//	Precondition checking
 	//
 		if (_sessionFactory == null)
 		{
 			throw new NullPointerException("No Hibernate Session Factory defined !");
 		}
 		
 	//	Persistence checking
 	//
 		if (isPersistentClass(hibernateClass) == false)
 		{
 		//	Not an hibernate Class !
 		//
 			if (_log.isTraceEnabled())
 			{
 				_log.trace(hibernateClass + " is not persistent");
 				dumpPersistenceMap();
 			}
 			throw new NotPersistentObjectException(pojo);			
 		}
 		
 	//	Retrieve Class<?> hibernate metadata
 	//
 		ClassMetadata hibernateMetadata = _sessionFactory.getClassMetadata(getEntityName(hibernateClass, pojo));
 		if (hibernateMetadata == null)
 		{
 		//	Component class (persistent but not metadata) : no associated id
 		//	So must be considered as transient
 		//
 			throw new ComponentTypeException(pojo);
 		}
 		
 	//	Retrieve ID
 	//
 		Serializable id = null;
 		Class<?> pojoClass = getPersistentClass(pojo);
 		if (hibernateClass.equals(pojoClass))
 		{
 		//	Same class for pojo and hibernate class
 		//
 			if (pojo instanceof HibernateProxy)
 			{
 			//	To prevent LazyInitialisationException
 			//
 				id = ((HibernateProxy)pojo).getHibernateLazyInitializer().getIdentifier();
 			}
 			else
 			{
 			//	Otherwise : use metada
 			//
 				id = hibernateMetadata.getIdentifier(pojo, EntityMode.POJO);
 			}
 		}
 		else
 		{
 		//	DTO case : invoke the method with the same name
 		//
 			String property = hibernateMetadata.getIdentifierPropertyName();
 			
 			try
 			{
 				// compute getter method name
 				property = property.substring(0,1).toUpperCase() + 
 						   property.substring(1);
 				String getter = "get" + property;
 				
 				// Find getter method
 				Method method = pojoClass.getMethod(getter, (Class[])null);
 				if (method == null)
 				{
 					throw new RuntimeException("Cannot find method " + getter + " for Class<?> " + pojoClass);
 				}
 				id = (Serializable) method.invoke(pojo,(Object[]) null);
 			}
 			catch (Exception ex)
 			{
 				throw new RuntimeException("Invocation exception ", ex);
 			}
 		}
 		
 	//	Post condition checking
 	//
 		if (isUnsavedValue(pojo, id, hibernateClass))
 		{
 			throw new TransientObjectException(pojo);
 		}
 		return id;
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.sf.gilead.core.hibernate.IPersistenceUtil#isHibernatePojo(java.lang.Object)
 	 */
 	public boolean isPersistentPojo(Object pojo)
 	{
 	//	Precondition checking
 	//
 		if (pojo == null)
 		{
 			return false;
 		}
 		
 	//	Try to get the ID : if an exception is thrown
 	//	the pojo is not persistent...
 	//
 		try
 		{
 			getId(pojo);
 			return true;
 		}
 		catch(TransientObjectException ex)
 		{
 			return false;
 		}
 		catch(NotPersistentObjectException ex)
 		{
 			return false;
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.sf.gilead.core.hibernate.IPersistenceUtil#isHibernateClass(java.lang.Class)
 	 */
 	public boolean isPersistentClass(Class<?> clazz)
 	{
 	//	Precondition checking
 	//
 		if (_sessionFactory == null)
 		{
 			throw new NullPointerException("No Hibernate Session Factory defined !");
 		}
 		
 	//	Check proxy (based on beanlib Unenhancer class)
 	//
 		clazz = getUnenhancedClass(clazz);
 		
 	//	Look into the persistence map
 	//
 		synchronized (_persistenceMap)
 		{
 			Boolean persistent = _persistenceMap.get(clazz);
 			if (persistent != null)
 			{
 				return persistent.booleanValue();
 			}
 		}
 		
 	//	First clall for this Class<?> : compute persistence class
 	//
 		computePersistenceForClass(clazz);
 		return _persistenceMap.get(clazz).booleanValue();
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.sf.gilead.core.hibernate.IPersistenceUtil#getPersistentClass(java.lang.Class)
 	 */
 	public Class<?> getUnenhancedClass(Class<?> clazz)
 	{
 	//	Map checking
 	//
 		Class<?> unenhancedClass = _unehancementMap.get(clazz);
 		if (unenhancedClass == null)
 		{
 		//	Based on beanlib unEnhancer class
 		//
 			unenhancedClass = UnEnhancer.unenhanceClass(clazz);
 			_unehancementMap.put(clazz, unenhancedClass);
 		}
 		return unenhancedClass;
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.sf.gilead.core.hibernate.IPersistenceUtil#isEnhanced(java.lang.Class)
 	 */
 	public boolean isEnhanced(Class<?> clazz)
 	{
 	//	Compare class to unenhanced class
 	//
 		return (clazz != getUnenhancedClass(clazz));
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.sf.gilead.core.hibernate.IPersistenceUtil#openSession()
 	 */
 	public void openSession()
 	{
 	//	Precondition checking
 	//
 		if (_sessionFactory == null)
 		{
 			throw new NullPointerException("No Hibernate Session Factory defined !");
 		}
 		
 	//	Open a the existing session
 	//
 		Session session = null;
 		boolean created = false;
 		try
 		{
 			session = _sessionFactory.getCurrentSession();
 			if (session.isConnected() == false)
 			{
 				session = _sessionFactory.openSession();
 				created = true;
 			}
 		}
 		catch (HibernateException ex)
 		{
 			_log.debug("No current session, opening a new one", ex);
 			session = _sessionFactory.openSession();
 			created = true;
 		}
 		
 	//	Store the session in ThreadLocal
 	//
 		_session.set(new HibernateSession(session, created));
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.sf.gilead.core.hibernate.IPersistenceUtil#closeSession(java.lang.Object)
 	 */
 	public void closeCurrentSession()
 	{
 		HibernateSession hSession = _session.get();
 		if (hSession != null)
 		{
 			// Only close session that we created
 			if (hSession.created == true)
 			{
 				hSession.session.close();
 			}
 			_session.remove();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#load(java.io.Serializable, java.lang.Class)
 	 */
 	public Object load(Serializable id, Class<?> persistentClass)
 	{
 	//	Unenhance persistent class if needed
 	//
 		persistentClass = getUnenhancedClass(persistentClass);
 		
 	//	Load the entity
 	//
 		return getSession().get(persistentClass, id);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#serializeEntityProxy(java.lang.Object)
 	 */
 	public Map<String, Serializable> serializeEntityProxy(Object proxy)
 	{
 	//	Precondition checking
 	//
 		if (proxy == null)
 		{
 			return null;
 		}
 		
 	//	Serialize needed proxy informations
 	//
 		Map<String, Serializable> result = new HashMap<String, Serializable>();
 		result.put(CLASS_NAME, getUnenhancedClass(proxy.getClass()).getName());
 		result.put(ID, getId(proxy));
 		
 		return result;
 	}
 	
 	/**
 	 * Create a proxy for the argument class and id
 	 */
 	public Object createEntityProxy(Map<String, Serializable> proxyInformations)
 	{
 	//	Get needed proxy inforamtions
 	//
 		Serializable id = proxyInformations.get(ID);
 		String entityName = (String) proxyInformations.get(CLASS_NAME);
 
 	//	Create the associated proxy
 	//
 		return getSession().load(entityName, id);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#serializePersistentCollection(java.lang.Object)
 	 */
 	public Map<String, Serializable> serializePersistentCollection(Collection<?> persistentCollection)
 	{
 	//	Create serialization map
 	//
 		Map<String, Serializable> result = new HashMap<String, Serializable>();
 		
 	//	Get parameters
 	//
 		AbstractPersistentCollection collection = (AbstractPersistentCollection) persistentCollection;
 		result.put(CLASS_NAME, collection.getClass().getName());
 		result.put(ROLE, collection.getRole());
 		result.put(KEY, collection.getKey());
 		
 	//	Store ids
 	//
 		if (isInitialized(collection) == true)
 		{
 			result.put(ID_LIST, createIdList((Collection)collection));
 		}
 		
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#serializePersistentMap(java.util.Map)
 	 */
 	public Map<String, Serializable> serializePersistentMap(Map<?,?> persistentMap)
 	{
 	//	Create serialization map
 	//
 		Map<String, Serializable> result = new HashMap<String, Serializable>();
 		
 	//	Get parameters
 	//
 		AbstractPersistentCollection collection = (AbstractPersistentCollection) persistentMap;
 		result.put(CLASS_NAME, collection.getClass().getName());
 		result.put(ROLE, collection.getRole());
 		result.put(KEY, collection.getKey());
 		
 	//	Store ids
 	//
 		if (isInitialized(collection) == true)
 		{
 		//	Store keys
 		//
 			ArrayList<SerializableId> keyList = createIdList(persistentMap.keySet());
 			if (keyList != null)
 			{
 				result.put(ID_LIST, keyList);
 				
 			//	Store values (only if keys are persistents)
 			//
 				ArrayList<SerializableId> valueList = createIdList(persistentMap.values());
 				if (keyList != null)
 				{
 					result.put(VALUE_LIST, valueList);
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Create a persistent collection
 	 * @param proxyInformations serialized proxy informations 
 	 * @param underlyingCollection the filled underlying collection
 	 * @return
 	 */
 	public Map<?,?> createPersistentMap(Object parent,
 										Map<String, Serializable> proxyInformations,
 										Map<?,?> underlyingMap)
 	{
 	//	Create original map
 	//
 		Map<?,?> originalMap = createOriginalMap(proxyInformations, underlyingMap);
 		
 	//	Create collection for the class name
 	//
 		String className = (String) proxyInformations.get(CLASS_NAME);
 
 		Session session = getSession();
 		PersistentCollection collection = null;
 		if (PersistentMap.class.getName().equals(className))
 		{
 		//	Persistent map creation
 		//
 			if (originalMap== null)
 			{
 				collection = new PersistentMap((SessionImpl) session);
 			}
 			else
 			{
 				collection = new PersistentMap((SessionImpl) session,
 						 				 	   underlyingMap);
 			}
 		}
 		else if (PersistentSortedMap.class.getName().equals(className))
 		{
 		//	Persistent map creation
 		//
 			if (originalMap== null)
 			{
 				collection = new PersistentSortedMap((SessionImpl) session);
 			}
 			else
 			{
 				collection = new PersistentSortedMap((SessionImpl) session,
 						 				 	   		 (SortedMap<?, ?>) underlyingMap);
 			}
 		}
 		else
 		{
 			throw new RuntimeException("Unknown persistent map class name : " + className);
 		}
 		
 	//	Fill with serialized parameters
 	//
 		String role = (String) proxyInformations.get(ROLE);
 		Serializable snapshot = null;
 		if (originalMap  != null)
 		{
 		//	Create snapshot
 		//
 			CollectionPersister collectionPersister = _sessionFactory.getCollectionPersister(role);
 			snapshot = collection.getSnapshot(collectionPersister);
 		}
 		
 		collection.setSnapshot(proxyInformations.get(KEY), 
 							   role, snapshot);
 		
 	//	Owner
 	//
 		collection.setOwner(parent);
 		
 	//	Update persistent collection
 	//
 		if (areDifferent(originalMap, underlyingMap))
 		{
 			if (originalMap != null)
 			{
 				((Map)collection).clear();
 			}
 			
 			
 			if (underlyingMap != null)
 			{
 				((Map)collection).putAll(underlyingMap);
 			}
 			
 			collection.dirty();
 		}
 		
 		return (Map<?,?>)collection;
 	}
 	
 	/**
 	 * Create a persistent collection
 	 * @param proxyInformations serialized proxy informations 
 	 * @param underlyingCollection the filled underlying collection
 	 * @return
 	 */
 	public Collection<?> createPersistentCollection(Object parent,
 											 		Map<String, Serializable> proxyInformations,
 											 		Collection<?> underlyingCollection)
 	{
 	//	Re-create original collection
 	//
 		Collection<?> originalCollection = createOriginalCollection(proxyInformations, underlyingCollection);
 		
 	//	Create Persistent collection for the class name
 	//
 		String className = (String) proxyInformations.get(CLASS_NAME);
 
 		Session session = getSession();
 		PersistentCollection collection = null;
 		if (PersistentBag.class.getName().equals(className))
 		{
 		//	Persistent bag creation
 		//
 			if (originalCollection == null)
 			{
 				collection = new PersistentBag((SessionImpl) session);
 			}
 			else
 			{
 				collection =  new PersistentBag((SessionImpl) session,
 										 		(Collection<?>) originalCollection);
 			}
 		}
 		else if (PersistentList.class.getName().equals(className))
 		{
 		//	Persistent list creation
 		//
 			if (originalCollection == null)
 			{
 				collection = new PersistentList((SessionImpl) session);
 			}
 			else
 			{
 				collection = new PersistentList((SessionImpl) session,
 										  		(List<?>) originalCollection);
 			}
 		}
 		else if (PersistentSet.class.getName().equals(className))
 		{
 		//	Persistent set creation
 		//
 			if (originalCollection == null)
 			{
 				collection = new PersistentSet((SessionImpl) session);
 			}
 			else
 			{
 				collection = new PersistentSet((SessionImpl) session,
 						 				 	   (Set<?>) originalCollection);
 			}
 		}
 		else if (PersistentSortedSet.class.getName().equals(className))
 		{
 		//	Persistent sorted set creation
 		//
 			if (originalCollection == null)
 			{
 				collection = new PersistentSortedSet((SessionImpl) session);
 			}
 			else
 			{
 				collection = new PersistentSortedSet((SessionImpl) session,
 						 				 	   		 (SortedSet<?>) originalCollection);
 			}
 		}
 		else
 		{
 			throw new RuntimeException("Unknown persistent collection class name : " + className);
 		}
 		
 	//	Fill with serialized parameters
 	//
 		String role = (String) proxyInformations.get(ROLE);
 		Serializable snapshot = null;
 		if (originalCollection != null)
 		{
 		//	Create snapshot
 		//
 			CollectionPersister collectionPersister = _sessionFactory.getCollectionPersister(role);
 			snapshot = collection.getSnapshot(collectionPersister);
 		}
 		
 		collection.setSnapshot(proxyInformations.get(KEY), 
 							   role, snapshot);
 		
 	//	Owner
 	//
 		collection.setOwner(parent);
 		
 	//	Update persistent collection
 	//
 		if (areDifferent(originalCollection, underlyingCollection))
 		{
 			if (originalCollection != null)
 			{
 				((Collection)collection).removeAll(originalCollection);
 			}
 			
 			
 			if (underlyingCollection != null)
 			{
 				for (Object item : underlyingCollection)
 				{
 					((Collection)collection).add(item);
 				}
 			}
 			
 			collection.dirty();
 		}
 		return (Collection<?>) collection;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#isPersistentCollection(java.lang.Class)
 	 */
 	public boolean isPersistentCollection(Class<?> collectionClass)
 	{
 		return (PersistentCollection.class.isAssignableFrom(collectionClass));
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#isPersistentCollection(java.lang.Class)
 	 */
 	public boolean isPersistentMap(Class<?> collectionClass)
 	{
 		return (PersistentMap.class.isAssignableFrom(collectionClass));
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#isProxy(java.lang.Object)
 	 */
 	public boolean isInitialized(Object proxy)
 	{
 		return Hibernate.isInitialized(proxy);
 	}
 	
 	/**
 	 * Flush pending modifications if needed
 	 */
 	public void flushIfNeeded()
 	{
 		Session session = getCurrentSession();
 		if (session != null)
 		{
 			_log.debug("Flushing session !");
 			session.flush();
 		}
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#initialize(java.lang.Object)
 	 */
 	public void initialize(Object proxy)
 	{
 		Hibernate.initialize(proxy);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#loadAssociation(java.lang.Class, java.io.Serializable, java.lang.String)
 	 */
 	public Object loadAssociation(Class<?> parentClass, Serializable parentId,
 								  String propertyName)
 	{
 	//	Create query
 	//
 		StringBuilder queryString = new StringBuilder();
 		queryString.append("SELECT item FROM ");
 		queryString.append(parentClass.getSimpleName());
 		queryString.append(" item JOIN FETCH item.");
 		queryString.append(propertyName);
 		queryString.append(" WHERE item.id = :id");
 		if (_log.isDebugEnabled())
 		{
 			_log.debug("Query is '" +queryString.toString() + "'");
 		}
 		
 	//	Fill query
 	//
 		Session session = getSession();
 		Query query = session.createQuery(queryString.toString());
 		query.setParameter("id", parentId);
 		
 	//	Execute query
 	//
 		return query.uniqueResult();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#executeQuery(java.lang.String, java.util.List)
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Object> executeQuery(String query, List<Object> parameters)
 	{
 		if (_log.isDebugEnabled())
 		{
 			_log.debug("Executing query '" +query+ "'");
 		}
 		
 	//	Fill query
 	//
 		Session session = getSession();
 		Query hqlQuery = session.createQuery(query);
 		
 	//	Fill parameters
 	//
 		if (parameters != null)
 		{
 			for (int index = 0; index < parameters.size() ; index ++)
 			{
 				hqlQuery.setParameter(index, parameters.get(index));
 			}
 		}
 		
 	//	Execute query
 	//
 		return (List<Object>) hqlQuery.list();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#executeQuery(java.lang.String, java.util.Map)
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Object> executeQuery(String query, Map<String, Object> parameters)
 	{
 		if (_log.isDebugEnabled())
 		{
 			_log.debug("Executing query '" +query+ "'");
 		}
 		
 	//	Fill query
 	//
 		Session session = getSession();
 		Query hqlQuery = session.createQuery(query);
 		
 	//	Fill parameters
 	//
 		if (parameters != null)
 		{
 			for (Map.Entry<String, Object> parameter : parameters.entrySet())
 			{
 				hqlQuery.setParameter(parameter.getKey(), parameter.getValue());
 			}
 		}
 		
 	//	Execute query
 	//
 		return (List<Object>) hqlQuery.list();
 	}
 	
 	//-------------------------------------------------------------------------
 	//
 	// Internal methods
 	//
 	//-------------------------------------------------------------------------
 	/**
 	 * Compute embedded persistence (Component, UserType) for argument class
 	 */
 	private void computePersistenceForClass(Class<?> clazz)
 	{
 	//	Precondition checking
 	//
 		synchronized (_persistenceMap)
 		{
 			if (_persistenceMap.get(clazz) != null)
 			{
 			//	already computed
 			//
 				return;
 			}
 		}
 		
 	//	Get associated metadata
 	//
 		List<String> entityNames = getEntityNamesFor(clazz);
 		if ((entityNames == null) ||
 			(entityNames.isEmpty() == true))
 		{
 		//	Not persistent : check implemented interfaces (they can be declared as persistent !!)
 		//
 			Class<?>[] interfaces = clazz.getInterfaces();
 			if (interfaces != null)
 			{
 				for (int index = 0; index < interfaces.length ; index ++)
 				{
 					if (isPersistentClass(interfaces[index]))
 					{
 						markClassAsPersistent(clazz, true);
 						return;
 					}
 						
 				}
 			}
 			
 		//	Not persistent and no persistent interface!
 		//
 			markClassAsPersistent(clazz, false);
 			return;
 		}
 
 	//	Persistent class
 	//
 		markClassAsPersistent(clazz, true);
 		
 	//	Look for component classes
 	//
 		for (String entityName : entityNames)
 		{
 			Type[] types = _sessionFactory.getClassMetadata(entityName).getPropertyTypes();
 			for (int index = 0; index < types.length; index++)
 			{
 				Type type = types[index];
 				if (_log.isDebugEnabled())
 				{
 					_log.debug("Scanning type " + type.getName() + " from " + clazz);
 				}
 				computePersistentForType(type);
 			}
 		}
 	}
 	
 	/**
 	 * Mark class as persistent or not
 	 * @param clazz
 	 * @param persistent
 	 */
 	private void markClassAsPersistent(Class<?> clazz, boolean persistent)
 	{
 		if (_log.isDebugEnabled())
 		{
 			if (persistent)
 			{
 				_log.debug("Marking " + clazz + " as persistent");
 			}
 			else
 			{
 				_log.debug("Marking " + clazz + " as not persistent");
 			}
 		}
 		synchronized (_persistenceMap)
 		{
 		//	Debug check
 		//
 			if (_persistenceMap.get(clazz) == null)
 			{
 				_persistenceMap.put(clazz, persistent);
 			}
 			else
 			{
 			//	Check persistence information
 			//
 				if (persistent != _persistenceMap.get(clazz).booleanValue())
 				{
 					throw new RuntimeException("Invalid persistence state for " + clazz);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Compute persistent for Hibernate type
 	 * @param type
 	 */
 	private void computePersistentForType(Type type)
 	{
 	//	Precondition checking
 	//
 		synchronized (_persistenceMap)
 		{
 			if (_persistenceMap.get(type.getReturnedClass()) != null)
 			{
 			//	already computed
 			//
 				return;
 			}
 		}
 		
 		if (_log.isDebugEnabled())
 		{
 			_log.debug("Scanning type " + type.getName());
 		}
 		
 		if (type.isComponentType())
 		{
 		//	Add the Class to the persistent map
 		//
 			if (_log.isDebugEnabled())
 			{
 				_log.debug("Type " + type.getName() + " is component type");
 			}
 			
 			markClassAsPersistent(type.getReturnedClass(), true);
 			
 			Type[] subtypes = ((AbstractComponentType) type).getSubtypes();
 			for (int index = 0; index < subtypes.length; index++)
 			{
 				computePersistentForType(subtypes[index]);
 			}
 		}	
 		else if (IUserType.class.isAssignableFrom(type.getReturnedClass()))
 		{
 		//	Add the Class to the persistent map
 		//
 			if (_log.isDebugEnabled())
 			{
 				_log.debug("Type " + type.getName() + " is user type");
 			}
 			
 			markClassAsPersistent(type.getReturnedClass(), true);
 		}
 		else if (type.isCollectionType())
 		{
 		//	Collection handling
 		//
 			if (_log.isDebugEnabled())
 			{
 				_log.debug("Type " + type.getName() + " is collection type");
 			}
 			computePersistentForType(((CollectionType) type).getElementType(_sessionFactory));
 		}
 		else if (type.isEntityType())
 		{
 			if (_log.isDebugEnabled())
 			{
 				_log.debug("Type " + type.getName() + " is entity type");
 			}
 			computePersistenceForClass(type.getReturnedClass());
  		}
 	}
 	
 	/**
 	 * Debug method : dump persistence map for checking
 	 */
 	private void dumpPersistenceMap()
 	{
 		synchronized (_persistenceMap)
 		{
 		// 	Dump every entry
 		//
 			_log.trace("-- Start of persistence map --");
 			for (Entry<Class<?>, Boolean> persistenceEntry : _persistenceMap.entrySet())
 			{
 				_log.trace(persistenceEntry.getKey() + " persistence is " + persistenceEntry.getValue());
 			}
 			_log.trace("-- End of persistence map --");
 		}
 	}
 	
 	/**
 	 * Create a list of serializable ID for the argument collection
 	 * @param collection
 	 * @return
 	 */
 	private ArrayList<SerializableId> createIdList(Collection collection)
 	{
 	//	Precondition checking
 	//
 		if (collection == null)
 		{
 			return null;
 		}
 		
 		int size = collection.size();
 		ArrayList<SerializableId> idList = new ArrayList<SerializableId>(size);
 		
 		Iterator<Object> iterator = ((Collection) collection).iterator();
 		while(iterator.hasNext())
 		{
 			Object item = iterator.next();
 			if (item != null)
 			{
 				SerializableId id = new SerializableId();
 				
 				if (isPersistentPojo(item))
 				{
 					id.setEntityName(getEntityName(getPersistentClass(item), item));
 					id.setId(getId(item));
 				}
 				else
 				{
 					id.setEntityName(item.getClass().getName());
 					id.setHashCode(item.hashCode());
 				}
 				
 				idList.add(id);
 			}
 		}
 		
 		if (idList.isEmpty())
 		{
 			return null;
 		}
 		else
 		{
 			return idList;
 		}
 	}
 	
 	
 	/**
 	 * Check if the id equals the unsaved value or not
 	 * @param entity
 	 * @return
 	 */
 	private boolean isUnsavedValue(Object pojo, Serializable id, Class<?> persistentClass)
 	{
 	//	Precondition checking
 	//
 		if (id == null)
 		{
 			return true;
 		}
 		
 	//	Get unsaved value from entity metamodel
 	//
 		EntityPersister entityPersister = _sessionFactory.getEntityPersister(getEntityName(persistentClass, pojo));
 		EntityMetamodel metamodel = entityPersister.getEntityMetamodel();
 		IdentifierProperty idProperty = metamodel.getIdentifierProperty();
 		Boolean result = idProperty.getUnsavedValue().isUnsaved(id);
 		
 		if (result == null)
 		{
 			// Unsaved value undefined
 			return false;
 		}
 		else
 		{
 			return result.booleanValue();
 		}
 	}
 	
 	/**
 	 * Return the underlying persistent class
 	 * @param pojo
 	 * @return
 	 */
 	private Class<?> getPersistentClass(Object pojo)
 	{
 		if (pojo instanceof HibernateProxy)
 		{
 			return ((HibernateProxy)pojo).getHibernateLazyInitializer().getPersistentClass();
 		}
 		else
 		{
 			return pojo.getClass();
 		}
 	}
 	
 	/**
 	 * (Re)create the original collection
 	 * @param proxyInformations
 	 * @param underlyingCollection
 	 */
 	private <T> Collection<T> createOriginalCollection(Map<String, Serializable> proxyInformations,
 										  		   	   Collection<T> collection)
 	{
 		if (collection == null)
 		{
 			return null;
 		}
 		
 		try
 		{
 			Collection<T> original = (Collection<T>) collection.getClass().newInstance();
 			
 			ArrayList<SerializableId> idList = (ArrayList<SerializableId>) proxyInformations.get(ID_LIST);
 			if (idList != null)
 			{
 			//	Create map(ID -> entity)
 			//
 				Map<Serializable, T> collectionMap = createCollectionMap(collection);
 				
 			//	Fill snapshot
 			//
 				for (SerializableId sid : idList)
 				{
 					original.add(createOriginalEntity(sid, collectionMap));
 				}
 				
 			}
 			return original;
 			
 		}
 		catch(Exception ex)
 		{
 			throw new RuntimeException(ex);
 		}
 	}
 	
 	/**
 	 * (Re)create the original map
 	 * @param proxyInformations
 	 * @param underlyingCollection
 	 */
 	private <K,V> Map<K,V> createOriginalMap(Map<String, Serializable> proxyInformations,
 										     Map<K,V> map)
 	{
 		try
 		{
 			ArrayList<SerializableId> keyList = (ArrayList<SerializableId>) proxyInformations.get(ID_LIST);
 			if (keyList != null)
 			{
 				ArrayList<SerializableId> valueList = (ArrayList<SerializableId>) proxyInformations.get(VALUE_LIST);
 				
 			//	Create maps(ID -> entity)
 			//
 				Map<Serializable, K> keyMap = createCollectionMap(map.keySet());
 				Map<Serializable, V> valueMap = createCollectionMap(map.values());
 				
 			//	Fill snapshot map
 			//
 				Map<K,V> snapshot = (Map<K,V>) new HashMap<K, V>();
 				for (SerializableId sid : keyList)
 				{
 					snapshot.put(createOriginalEntity(sid, keyMap),
 								 createOriginalEntity(valueList.get(keyList.indexOf(sid)),
 										 			  valueMap));
 				}
 				
 				return snapshot;
 			}
 			else
 			{
 				return null;
 			}
 		}
 		catch(Exception ex)
 		{
 			throw new RuntimeException(ex);
 		}
 	}
 	
 	/**
 	 * Test if the two argument collection are the same or not
 	 * @param coll1
 	 * @param coll2
 	 * @return
 	 */
 	private boolean areDifferent(Collection coll1, Collection coll2)
 	{
 	//	Precondition checking
 	//
 		if (coll1 == null)
 		{
 			return (coll2 != null && !coll2.isEmpty());
 		}
 		
 		if (coll2 == null)
 		{
 			return !coll1.isEmpty();
 		}
 		
 	//	Size comparison
 	//
 		if (coll1.size() != coll2.size())
 		{
 			return true;
 		}
 		
 	//	Item comparison
 	//
 		if ((coll1 instanceof List) ||
 			(coll1 instanceof SortedSet))
 		{
 		//	Compare content *and* order
 		//
 			Object[] array1 = coll1.toArray();
 			Object[] array2 = coll2.toArray();
 			
 			for (int index = 0 ; index < array1.length ; index ++)
 			{
 				if (array1[index] != array2[index]) 
 				{
 					return true;
 				}
 			}
 		}
 		else
 		{
 			// No order : just compare contents
 			for (Object item : coll1)
 			{
 				if (coll2.contains(item) == false)
 				{
 					return true;
 				}
 			}
 		}
 	//	Same collections
 	//
 		return false;
 	}
 	
 	/**
 	 * Test if the two argument collection are the same or not
 	 * @param coll1
 	 * @param coll2
 	 * @return
 	 */
 	private boolean areDifferent(Map map1, Map map2)
 	{
 	//	Precondition checking
 	//
 		if (map1 == null)
 		{
 			return (map2 != null && !map2.isEmpty());
 		}
 		
 		if (map2 == null)
 		{
 			return !map1.isEmpty();
 		}
 		
 	//	Size comparison
 	//
 		if (map1.size() != map2.size())
 		{
 			return true;
 		}
 		
 	//	Item comparison
 	//
 		// No order : just compare contents
 		for (Object key : map1.keySet())
 		{
 			if (map2.containsKey(key) == false)
 			{
 				return true;
 			}
 			
 			// Compare values
 			Object value1 = map1.get(key);
 			Object value2 = map2.get(key);
 			if (value1 != value2)
 			{
 				return true;
 			}
 		}
 		
 	//	Same maps
 	//
 		return false;
 	}
 	
 	/**
 	 * Create an entity from its serializable id.
 	 * The entity is taken from the argument map in priority.
 	 * @param sid
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private <T> T createOriginalEntity(SerializableId sid, 
 									   Map<Serializable, T> collectionMap)
 	{
 	//	Precondition checking
 	//
 		T entity = null;
 		if (sid.getId() != null)
 		{
 			// Is the entity still present ?
 			entity = collectionMap.get(sid.getId());
 			if (entity == null)
 			{
 			//	deleted item
 			//
 				try
 				{
 					entity = (T) getSession().load(sid.getEntityName(), sid.getId());
 				}
 				catch(ObjectNotFoundException ex)
 				{
 				//	The data has already been deleted, just remove it from the collection
 				//
 					_log.warn("Deleted entity : " + sid + " cannot be retrieved from DB and thus added to snapshot", ex);
 				}
 			}
 		}
 		else // if (sid.getHashCode() != null)
 		{
 			entity = collectionMap.get(sid.getHashCode());
 			if (entity == null)
 			{
 			//	deleted item
 			//
 				try
 				{
 					entity = (T) Class.forName(sid.getEntityName()).newInstance();
 				}
 				catch(Exception ex)
 				{
 					throw new RuntimeException(ex);
 				}
 			}
 		}
 		
 		return entity;
 	}
 	
 	/**
 	 * Create a collection map
 	 * @param <T>
 	 * @param collection
 	 * @return
 	 */
 	private <T> Map<Serializable, T> createCollectionMap(Collection<T> collection)
 	{
 		Map<Serializable, T> collectionMap = new HashMap<Serializable, T>();
 		for (T item : collection)
 		{
 			try
 			{
 				collectionMap.put(getId(item), item);
 			}
			catch(NotPersistentObjectException ex)
			{
				// not hibernate entity : use hashcode instead
				collectionMap.put(item.hashCode(), item);
			}
 			catch(TransientObjectException ex)
 			{
 				// transient entity : use hashcode instead
 				collectionMap.put(item.hashCode(), item);
 			}
 		}
 		
 		return collectionMap;
 	}
 	
 	/**
 	 * @return the current session (open a new one if needed)
 	 */
 	private Session getSession()
 	{
 		HibernateSession hSession = _session.get();
 		if (hSession == null)
 		{
 			openSession();
 			hSession = _session.get();
 		}
 		return hSession.session;
 	}
 	
 	/**
 	 * Return the already opened session (returns null if none is opened)
 	 */
 	protected Session getCurrentSession()
 	{
 	//	Precondition checking
 	//
 		if (_sessionFactory == null)
 		{
 			throw new NullPointerException("No Hibernate Session Factory defined !");
 		}
 		
 	//	Open the existing session
 	//
 		Session session = null;
 		try
 		{
 			session = _sessionFactory.getCurrentSession();
 			if (session.isConnected() == false)
 			{
 				return null;
 			}
 		}
 		catch (HibernateException ex)
 		{
 			_log.debug("Exception during getCurrentSession", ex);
 			return null;
 		}
 		
 		return session;
 	}
 	
 	/**
 	 * Get Hibernate class metadata
 	 * @param clazz
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private String getEntityName(Class<?> clazz, Object pojo)
 	{
 	//	Direct metadata search
 	//
 		ClassMetadata metadata = _sessionFactory.getClassMetadata(clazz);
 		if (metadata != null)
 		{
 			return metadata.getEntityName();
 		}
 		
 	//	Iterate over all metadata to prevent entity name bug
 	//	(if entity-name is redefined in mapping file, it is not found with
 	//	_sessionFatory.getClassMetada(clazz); !)
 	//
 		List<String> entityNames = getEntityNamesFor(clazz);
 		
 	//	check entity names
 	//
 		if (entityNames.isEmpty())
 		{
 		//	Not found
 		//
 			return getUnenhancedClass(clazz).getName();
 		}
 		else if (entityNames.size() == 1)
 		{
 		//	Only one entity name
 		//
 			return entityNames.get(0);
 		}
 		
 	//	More than one entity name : need pojo to know which one is the right one
 	//
 		if (pojo != null)
 		{
 			// Get entity name
 			return ((SessionImpl)getSession()).bestGuessEntityName(pojo);
 		}
 		else
 		{
 			throw new NullPointerException("Missing pojo for entity name retrieving !");
 		}
 	}
 	
 	/**
 	 * @return all possible entity names for the argument class.
 	 */
 	@SuppressWarnings("unchecked")
 	private List<String> getEntityNamesFor(Class<?> clazz)
 	{
 		List<String> entityNames = new ArrayList<String>();
 		Map<String, ClassMetadata> allMetadata = _sessionFactory.getAllClassMetadata();
 		for (ClassMetadata classMetadata : allMetadata.values())
 		{
 			if (clazz.equals(classMetadata.getMappedClass(EntityMode.POJO)))
 			{
 				entityNames.add(classMetadata.getEntityName());
 			}
 		}
 		
 		return entityNames;
 	}
 }
 
 /**
  * Structure for Hibernate session management
  * @author bruno.marchesson
  *
  */
 class HibernateSession
 {
 	public Session session;
 	public boolean created;
 	
 	public HibernateSession(Session session, boolean created)
 	{
 		this.session = session;
 		this.created = created;
 	}
 }
