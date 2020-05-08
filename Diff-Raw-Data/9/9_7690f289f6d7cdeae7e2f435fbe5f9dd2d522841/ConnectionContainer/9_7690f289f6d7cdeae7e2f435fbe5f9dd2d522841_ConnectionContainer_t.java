 package org.lightmare.cache;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import javax.persistence.EntityManagerFactory;
 
 import org.apache.log4j.Logger;
 import org.lightmare.jndi.JndiManager;
 import org.lightmare.jpa.JPAManager;
 import org.lightmare.jpa.datasource.Initializer;
 import org.lightmare.jpa.datasource.PoolConfig;
 import org.lightmare.jpa.datasource.PoolConfig.PoolProviderType;
 import org.lightmare.utils.LogUtils;
 import org.lightmare.utils.NamingUtils;
 import org.lightmare.utils.ObjectUtils;
 
 /**
  * Container class to cache connections
  * 
  * @author levan
  * 
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
 
 	boolean check = ObjectUtils.available(unitName);
 
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
 
 	ConnectionSemaphore semaphore = null;
 
 	if (ObjectUtils.available(unitName)) {
 
 	    semaphore = createSemaphore(unitName);
 	    if (ObjectUtils.available(jndiName)) {
 
 		ConnectionSemaphore existent = CONNECTIONS.putIfAbsent(
 			jndiName, semaphore);
 
 		if (existent == null) {
 		    semaphore.setJndiName(jndiName);
 		}
 	    }
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
      * Checks if {@link ConnectionSemaphore#isInProgress()} for appropriated
      * unit name
      * 
      * @param jndiName
      * @return <code>boolean</code>
      */
     public static boolean isInProgress(String jndiName) {
 
 	ConnectionSemaphore semaphore = CONNECTIONS.get(jndiName);
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
 	boolean inProgress = ObjectUtils.notNull(semaphore);
 	if (inProgress) {
 	    inProgress = checkOnProgress(semaphore);
 	    if (inProgress) {
 		awaitConnection(semaphore);
 	    }
 	}
 
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
		LogUtils.error(LOG, ex, NamingUtils.COULD_NOT_UNBIND_NAME,
 			jndiName, ex.getMessage());
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
 	    JPAManager.closeEntityManagerFactory(emf);
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
 
 	if (users < ConnectionSemaphore.MINIMAL_USERS) {
 
 	    EntityManagerFactory emf = semaphore.getEmf();
 	    JPAManager.closeEntityManagerFactory(emf);
 	    unbindConnection(semaphore);
 
 	    synchronized (CONNECTIONS) {
 
 		CONNECTIONS.remove(semaphore.getUnitName());
 		String jndiName = semaphore.getJndiName();
 
 		if (ObjectUtils.available(jndiName)) {
 		    CONNECTIONS.remove(jndiName);
 		    semaphore.setBound(Boolean.FALSE);
 		    semaphore.setCached(Boolean.FALSE);
 		}
 	    }
 	}
     }
 
     /**
      * Removes {@link ConnectionSemaphore} from cache and unbinds name from
      * {@link javax.naming.Context}
      * 
      * @param unitName
      */
     public static void removeConnection(String unitName) {
 
 	ConnectionSemaphore semaphore = CONNECTIONS.get(unitName);
 
 	if (ObjectUtils.notNull(semaphore)) {
 	    awaitConnection(semaphore);
 	    closeConnection(semaphore);
 	}
     }
 
     public static void setPollProviderType(String jndiName,
 	    PoolProviderType type) {
 
 	POOL_CONFIG_TYPES.put(jndiName, type);
     }
 
     public static PoolProviderType getAndRemovePoolProviderType(String jndiName) {
 
 	PoolProviderType type = POOL_CONFIG_TYPES.get(jndiName);
 	if (type == null) {
 	    type = new PoolConfig().getPoolProviderType();
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
