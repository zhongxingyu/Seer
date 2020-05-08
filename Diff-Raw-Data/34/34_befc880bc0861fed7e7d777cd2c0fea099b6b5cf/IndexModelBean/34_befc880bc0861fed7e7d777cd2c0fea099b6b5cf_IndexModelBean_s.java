 package com.bananity.models;
 
 
 // Bananity Classes
 import com.bananity.caches.CacheBean;
 import com.bananity.constants.StorageConstantsBean;
 import com.bananity.storages.IIndexStorage;
 import com.bananity.storages.StoragesFactoryBean;
 import com.bananity.util.StorageItemComparator;
 
 // Google Caches
 import com.google.common.cache.Cache;
 
 // Java utils
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 
 // Bean Setup
 import javax.ejb.EJB;
 import javax.ejb.Startup;
 import javax.ejb.Singleton;
 import javax.annotation.PostConstruct;
 
 // Concurrency Management
 import javax.ejb.Lock;
 import javax.ejb.LockType;
 import javax.ejb.DependsOn;
 import javax.ejb.ConcurrencyManagement;
 import javax.ejb.ConcurrencyManagementType;
 
 // Timeouts
 import javax.ejb.AccessTimeout;
 import javax.ejb.ConcurrentAccessTimeoutException;
 
 // Log4j
 import org.apache.log4j.Logger;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.PropertyConfigurator;
 
 
 /**
  *  This bean is the generic model (in the MVC pattern) to access the indexed collections
  *
  *  @author 	Andreu Correa Casablanca
  *  @version 	0.4
  */
 @Startup
 @Singleton
 @DependsOn({"StorageConstantsBean", "CacheBean", "StoragesFactoryBean"})
 //@AccessTimeout(value=10000)
 @ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
 public class IndexModelBean {
 	// TODO: Pasar a ConcurrencyManagementType.BEAN
 	//       y manejar bloqueos en insert de forma lo más atómica posible
 
 	/**
 	 *  Storage Factory reference
 	 */
 	@EJB
 	private StoragesFactoryBean sfB;
 
 	/**
 	 *  Caches handler reference
 	 */
 	@EJB
 	private CacheBean cB;
 
 	/**
 	 *  Storage Constants reference
 	 */
 	@EJB
 	private StorageConstantsBean scB;
 
 	/**
 	 *  Storage reference
 	 */
 	private IIndexStorage storage;
 
 	/**
 	 *  Log4j reference
 	 */
 	private static Logger log;
 
 	/**
 	 *  Configuration Value
 	 */
 	private static int tokenEntrySize;
 
 	/**
 	 *  This method initializes the logger and storage references
 	 */
 	@Lock(LockType.WRITE)
 	@PostConstruct
 		void init() {
 			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 			PropertyConfigurator.configure(classLoader.getResource("log4j.properties"));
 			log = Logger.getLogger(IndexModelBean.class);
 
 			// Loading storage
 			storage = sfB.getIndexStorage();
 
 			tokenEntrySize = scB.getTokenEntrySize();
 		}
 
 	/**
 	 *  This method looks for indexed content using every subtoken in 'subTokens' in the specified collection (collName)
 	 *
 	 *  @param collName 	Collection Name (where the search will be done)
 	 *  @param subTokens 	Tokens used to do the search
 	 *  @param limit 		(At this moment) unused parameter
 	 *
 	 *  @return 			List of strings (found results in storage)
 	 */
 	@Lock(LockType.READ)
 		public ArrayList<String> find (String collName, Collection<String> subTokens, int limit) throws Exception {
 			Cache<String, ArrayList<String>> cache = cB.getTokensCache(collName);
 
 			if (cache == null) {
 				throw new Exception("¡Cache not foud for collection \""+collName+"\"!");
 			}
 
 			ArrayList<String> subTokenResult;
 			ArrayList<String> result 			= new ArrayList<String>();
 
 			for (String subToken : subTokens) {
 				subTokenResult = cache.getIfPresent(subToken);
 
 				if (subTokenResult == null) {
 					subTokenResult = storage.findSubToken (collName, subToken);
 					subTokenResult.trimToSize();
 					cache.put(subToken, subTokenResult);
 				}
 
 				result.addAll(subTokenResult);
 			}
 
 			return result;
 		}
 
 	/**
 	 *  This method inserts an 'item' into the specified collection (collName) through many 'subTokens'
 	 *
 	 *  @param collName 	Collection Name (where the insert will be done)
 	 *  @param item 		Item to be inserted
 	 *  @param subTokens 	Tokens used to index the item
 	 */
 	@Lock(LockType.READ)
 		public void insert (String collName, String item, Collection<String> subTokens) throws Exception {
 			Cache<String, ArrayList<String>> cache = cB.getTokensCache(collName);
 
 			if (cache == null) {
 				throw new Exception("¡Cache not foud for collection \""+collName+"\"!");
 			}
 
 			boolean addedItem, mustTrim, recoveredFromStorage;
 			String sortingTmpValue;
 
 			for (String subToken : subTokens) {
 				ArrayList<String>  subTokenRelatedItems = cache.getIfPresent(subToken);
 				
 				if (subTokenRelatedItems == null) {
 					subTokenRelatedItems = storage.findSubToken (collName, subToken);
 					recoveredFromStorage = true;
 					mustTrim = true;
 				} else {
 					recoveredFromStorage = false;
 					mustTrim = false;
 				}
 
 				// Este enfoque (más complejo que un simple Collections.sort)
 				// se aplica para evitar copias en memoria, inserciones en mongo,
 				// y ordenaciones inútiles
 				if (!subTokenRelatedItems.contains(item)) {
 					if (subTokenRelatedItems.size() < tokenEntrySize) {
 						subTokenRelatedItems.add(item);
 						addedItem = true;
 						mustTrim = true;
					} else if (subTokenRelatedItems.get(tokenEntrySize-1).compareTo(item) > 0) {
 						subTokenRelatedItems.set(tokenEntrySize-1, item);
 						addedItem = true;
 					} else {
 						addedItem = false;
 					}
 				} else {
 					addedItem = false;
 				}
 
 				if (addedItem) {
 					
					for (int i=subTokenRelatedItems.size()-1; i>0 && subTokenRelatedItems.get(i).compareTo(subTokenRelatedItems.get(i-1)) < 0; i--) {
 						sortingTmpValue = subTokenRelatedItems.get(i);
 						subTokenRelatedItems.set(i, subTokenRelatedItems.get(i-1));
 						subTokenRelatedItems.set(i-1, sortingTmpValue);
 					}
 
 					storage.insert(collName, subToken, subTokenRelatedItems);
 				}
 
 				if (addedItem || recoveredFromStorage) {
 					cache.put(subToken, subTokenRelatedItems);
 				}
 
 				if (mustTrim) {
 					subTokenRelatedItems.trimToSize();
 				}
 			}
 		}
 
 	/**
 	 *  This method removes an 'item' from the specified collection (collName) through many 'subTokens'
 	 *
 	 *  @param collName 	Collection Name (where the remove will be done)
 	 *  @param item 		Item to be removed
 	 *  @param subTokens 	Tokens used to index the item
 	 */
 	@Lock(LockType.READ)
 		public void remove (String collName, String item, Collection<String> subTokens) throws Exception {
 			// TODO
 		}
 }
