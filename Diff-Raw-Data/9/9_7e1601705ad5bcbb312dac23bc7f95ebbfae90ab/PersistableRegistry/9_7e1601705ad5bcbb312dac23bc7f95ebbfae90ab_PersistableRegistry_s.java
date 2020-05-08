 package edu.uci.lighthouse.core.data;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.Assert;
 
 import edu.uci.lighthouse.model.io.IPersistable;
 import edu.uci.lighthouse.services.LighthouseServiceFactory;
 import edu.uci.lighthouse.services.persistence.IPersistenceService;
 import edu.uci.lighthouse.services.persistence.PersistenceException;
 
 /**
  * Creates and loads <code>IPersistable</code> instances lazily.
  * @author tproenca
  *
  */
 public class PersistableRegistry {
 	
 	private static Map<Class<? extends IPersistable>, IPersistable> registry = new HashMap<Class<? extends IPersistable>, IPersistable>();
 	
 	private static Logger logger = Logger.getLogger(PersistableRegistry.class);
 	
 	/**
 	 * Returns the class instance. If the instance does not exists, one will be created and it will try to load the previous state from the file system.
 	 * @param clazz
 	 */
 	public static IPersistable getInstance(Class<? extends IPersistable> clazz){
 		IPersistable instance = registry.get(clazz);
 		if (instance == null) {
 			IPersistenceService svc = (IPersistenceService) LighthouseServiceFactory.getService("GenericPersistenceService");
 			try {
				instance = clazz.newInstance();
 				instance = svc.load(instance);
 			} catch (Exception e) {
 				logger.error(e,e);
 			} 
 		}
 		Assert.isNotNull(instance);
 		return instance;
 	}
 	
 	/**
 	 * Saves instances from the registry to the file system.
 	 */
 	public static void saveInstances() {
 		IPersistenceService svc = (IPersistenceService) LighthouseServiceFactory.getService("GenericPersistenceService");
 		Collection<IPersistable> values = registry.values();
 		for (IPersistable persistable : values) {
 			try {
 				svc.save(persistable);
 			} catch (PersistenceException e) {
 				logger.error(e,e);
 			}
 		}
 	}
 }
