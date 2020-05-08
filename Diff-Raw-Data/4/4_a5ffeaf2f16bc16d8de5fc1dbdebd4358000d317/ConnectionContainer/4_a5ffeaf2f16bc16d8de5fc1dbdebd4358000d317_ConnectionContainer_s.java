 package org.lightmare.cache;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import javax.persistence.EntityManagerFactory;
 
 import org.apache.log4j.Logger;
 import org.lightmare.jndi.JndiManager;
 import org.lightmare.jpa.JpaManager;
 import org.lightmare.jpa.datasource.Initializer;
 import org.lightmare.jpa.datasource.PoolConfig;
 import org.lightmare.jpa.datasource.PoolConfig.PoolProviderType;
 import org.lightmare.utils.LogUtils;
 import org.lightmare.utils.NamingUtils;
 import org.lightmare.utils.ObjectUtils;
 import org.lightmare.utils.StringUtils;
 
 /**
  * Container class to cache connections and connection types
  * 
  * @author Levan Tsinadze
  * @since 0.0.65-SNAPSHOT
  * @see org.lightmare.deploy.BeanLoader#initializeDatasource(org.lightmare.deploy.BeanLoader.DataSourceParameters)
  * @see org.lightmare.deploy.BeanLoader#loadBean(org.lightmare.deploy.BeanLoader.BeanParameters)
  * @see org.lightmare.ejb.EjbConnector
  */
 public class ConnectionContainer {
 
     // Keeps unique EntityManagerFactories builded by unit names
     private static final ConcurrentMap<String, ConnectionSemaphore> CONNECTIONS = new ConcurrentHashMap<String, ConnectionSemaphore>();
 
     // Keeps unique PoolConfigs builded by unit names
     private static final ConcurrentMap<String, PoolProviderType> POOL_CONFIG_TYPES = new ConcurrentHashMap<String, PoolProviderType>();
 
     private static final Logger LOG = Logger
 	    .getLogger(ConnectionContainer.class);
 
     /**
      * Checks if connection with passed unit name is cached
      * 
      * @param unitName
      * @return <code>boolean</code>
      */
     public static boolean checkForEmf(String unitName) {
 
 	boolean check = StringUtils.valid(unitName);
 
 	if (check) {
 	    check = CONNECTIONS.containsKey(unitName);
 	}
 
 	return check;
     }
 
     /**
      * Gets {@link ConnectionSemaphore} from cache without waiting for lock
      * 
      * @param unitName
      * @return {@link ConnectionSemaphore}
      */
     public static ConnectionSemaphore getSemaphore(String unitName) {
 	return CONNECTIONS.get(unitName);
     }
 
     /**
      * Checks if deployed {@link ConnectionSemaphore} componnents
      * 
      * @param semaphore
      * @return <code>boolean</code>
      */
     private static boolean checkOnProgress(ConnectionSemaphore semaphore) {
 	return semaphore.isInProgress()
 		&& ObjectUtils.notTrue(semaphore.isBound());
     }
 
     /**
      * Creates and locks {@link ConnectionSemaphore} instance
      * 
      * @param unitName
      * @return {@link ConnectionSemaphore}
      */
     private static ConnectionSemaphore createSemaphore(String unitName) {
 
 	ConnectionSemaphore semaphore = CONNECTIONS.get(unitName);
 	ConnectionSemaphore current = null;
 
 	if (semaphore == null) {
 	    semaphore = new ConnectionSemaphore();
 	    semaphore.setUnitName(unitName);
 	    semaphore.setInProgress(Boolean.TRUE);
 	    semaphore.setCached(Boolean.TRUE);
 	    current = CONNECTIONS.putIfAbsent(unitName, semaphore);
 	}
 
 	if (current == null) {
 	    current = semaphore;
 	}
 
 	current.incrementUser();
 
 	return current;
     }
 
     /**
      * Caches {@link ConnectionSemaphore} with lock
      * 
      * @param unitName
      * @param jndiName
      * @return {@link ConnectionSemaphore}
      */
     public static ConnectionSemaphore cacheSemaphore(String unitName,
 	    String jndiName) {
 
 	ConnectionSemaphore semaphore;
 
 	// Creates and caches ConnectionSemaphore instance for passed unit and
 	// JNDI names
 	if (StringUtils.valid(unitName)) {
 	    semaphore = createSemaphore(unitName);
 	    if (StringUtils.valid(jndiName)) {
 		ConnectionSemaphore existent = CONNECTIONS.putIfAbsent(
 			jndiName, semaphore);
 
 		if (existent == null) {
 		    semaphore.setJndiName(jndiName);
 		}
 	    }
 	} else {
 	    semaphore = null;
 	}
 
 	return semaphore;
     }
 
     /**
      * Waits until {@link ConnectionSemaphore} is in progress (locked)
      * 
      * @param semaphore
      */
     private static void awaitConnection(ConnectionSemaphore semaphore) {
 
 	synchronized (semaphore) {
 	    boolean inProgress = checkOnProgress(semaphore);
 	    while (inProgress) {
 		try {
 		    semaphore.wait();
 		    inProgress = checkOnProgress(semaphore);
 		} catch (InterruptedException ex) {
 		    inProgress = Boolean.FALSE;
 		    LOG.error(ex.getMessage(), ex);
 		}
 	    }
 	}
     }
 
     /**
      * Checks if {@link ConnectionSemaphore} is in progress and if it is waits
      * while lock is released
      * 
      * @param semaphore
      * @return <code>boolean</code>
      */
     private static boolean isInProgress(ConnectionSemaphore semaphore) {
 
 	boolean inProgress = ObjectUtils.notNull(semaphore);
 
 	if (inProgress) {
 	    inProgress = checkOnProgress(semaphore);
 	    if (inProgress) {
 		awaitConnection(semaphore);
 	    }
 	}
 
 	return inProgress;
     }
 
     /**
      * Checks if {@link ConnectionSemaphore#isInProgress()} for appropriated
      * unit name
      * 
      * @param jndiName
      * @return <code>boolean</code>
      */
     public static boolean isInProgress(String jndiName) {
 
 	boolean inProgress;
 
 	ConnectionSemaphore semaphore = CONNECTIONS.get(jndiName);
 	inProgress = isInProgress(semaphore);
 
 	return inProgress;
     }
 
     /**
      * Gets {@link ConnectionSemaphore} from cache, awaits if connection
      * instantiation is in progress
      * 
      * @param unitName
      * @return {@link ConnectionSemaphore}
      * @throws IOException
      */
     public static ConnectionSemaphore getConnection(String unitName)
 	    throws IOException {
 
 	ConnectionSemaphore semaphore = CONNECTIONS.get(unitName);
 
 	isInProgress(semaphore);
 
 	return semaphore;
     }
 
     /**
      * Gets {@link EntityManagerFactory} from {@link ConnectionSemaphore},
      * awaits if connection
      * 
      * @param unitName
      * @return {@link EntityManagerFactory}
      * @throws IOException
      */
     public static EntityManagerFactory getEntityManagerFactory(String unitName)
 	    throws IOException {
 
 	EntityManagerFactory emf;
 
 	ConnectionSemaphore semaphore = CONNECTIONS.get(unitName);
 	boolean inProgress = ObjectUtils.notNull(semaphore);
 
 	if (inProgress) {
 	    inProgress = checkOnProgress(semaphore);
 	    if (inProgress) {
 		awaitConnection(semaphore);
 	    }
 
 	    emf = semaphore.getEmf();
 	} else {
 	    emf = null;
 	}
 
 	return emf;
     }
 
     /**
      * Removes connection from {@link javax.naming.Context} cache
      * 
      * @param semaphore
      */
     private static void unbindConnection(ConnectionSemaphore semaphore) {
 
 	String jndiName = semaphore.getJndiName();
 	if (ObjectUtils.notNull(jndiName) && semaphore.isBound()) {
 	    JndiManager jndiManager = new JndiManager();
 	    try {
 		String fullJndiName = NamingUtils.createJpaJndiName(jndiName);
 		Object boundData = jndiManager.lookup(fullJndiName);
 
 		if (ObjectUtils.notNull(boundData)) {
 		    jndiManager.unbind(fullJndiName);
 		}
 	    } catch (IOException ex) {
 		LogUtils.error(LOG, ex,
 			NamingUtils.COULD_NOT_UNBIND_NAME_ERROR, jndiName,
 			ex.getMessage());
 	    }
 	}
     }
 
     /**
      * Closes all existing {@link EntityManagerFactory} instances kept in cache
      */
     public static void closeEntityManagerFactories() {
 
 	Collection<ConnectionSemaphore> semaphores = CONNECTIONS.values();
 	EntityManagerFactory emf;
 
 	for (ConnectionSemaphore semaphore : semaphores) {
 	    emf = semaphore.getEmf();
 	    JpaManager.closeEntityManagerFactory(emf);
 	}
 
	synchronized (CONNECTIONS) {
	    CONNECTIONS.clear();
	}
     }
 
     /**
      * Closes all {@link javax.persistence.EntityManagerFactory} cached
      * instances
      * 
      * @throws IOException
      */
     public static void closeConnections() throws IOException {
 
 	ConnectionContainer.closeEntityManagerFactories();
 	Initializer.closeAll();
     }
 
     /**
      * Closes connection ({@link EntityManagerFactory}) in passed
      * {@link ConnectionSemaphore}
      * 
      * @param semaphore
      */
     private static void closeConnection(ConnectionSemaphore semaphore) {
 
 	int users = semaphore.decrementUser();
 
 	// Checks if users (EJB beans) for appropriated ConnectionSemaphore is
 	// less or equals minimal amount to close appropriated connection
 	if (users < ConnectionSemaphore.MINIMAL_USERS) {
 
 	    EntityManagerFactory emf = semaphore.getEmf();
 	    JpaManager.closeEntityManagerFactory(emf);
 	    unbindConnection(semaphore);
 
 	    synchronized (CONNECTIONS) {
 		CONNECTIONS.remove(semaphore.getUnitName());
 		String jndiName = semaphore.getJndiName();
 
 		if (StringUtils.valid(jndiName)) {
 		    CONNECTIONS.remove(jndiName);
 		    semaphore.setBound(Boolean.FALSE);
 		    semaphore.setCached(Boolean.FALSE);
 		}
 	    }
 	}
     }
 
     /**
      * Removes {@link ConnectionSemaphore} from cache and removes bindings of
      * JNDI name from {@link javax.naming.Context} lookups
      * 
      * @param unitName
      */
     public static void removeConnection(String unitName) {
 
 	// Removes appropriate connection from cache and JNDI lookup
 	ConnectionSemaphore semaphore = CONNECTIONS.get(unitName);
 	if (ObjectUtils.notNull(semaphore)) {
 	    awaitConnection(semaphore);
 	    closeConnection(semaphore);
 	}
     }
 
     /**
      * Caches {@link PoolProviderType} to use for data source deployment
      * 
      * @param jndiName
      * @param type
      */
     public static void setPollProviderType(String jndiName,
 	    PoolProviderType type) {
 	POOL_CONFIG_TYPES.put(jndiName, type);
     }
 
     /**
      * Gets configured {@link PoolProviderType} for data sources deployment
      * 
      * @param jndiName
      * @return {@link PoolProviderType}
      */
     public static PoolProviderType getAndRemovePoolProviderType(String jndiName) {
 
 	PoolProviderType type = POOL_CONFIG_TYPES.get(jndiName);
 
 	if (type == null) {
 	    type = new PoolConfig().getPoolProviderType();
 	    POOL_CONFIG_TYPES.put(jndiName, type);
 	}
 
 	POOL_CONFIG_TYPES.remove(jndiName);
 
 	return type;
     }
 
     /**
      * Closes all connections and data sources and clears all cached data
      * 
      * @throws IOException
      */
     public static void clear() throws IOException {
 
 	closeConnections();
 	CONNECTIONS.clear();
 	POOL_CONFIG_TYPES.clear();
     }
 }
