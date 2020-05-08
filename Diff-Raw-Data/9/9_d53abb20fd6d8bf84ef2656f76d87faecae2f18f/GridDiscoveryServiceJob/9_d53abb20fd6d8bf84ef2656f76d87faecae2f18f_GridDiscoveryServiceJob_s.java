 package gov.nih.nci.gss.scheduler;
 
 import gov.nih.nci.gss.domain.DataService;
 import gov.nih.nci.gss.domain.DataServiceGroup;
 import gov.nih.nci.gss.domain.DomainAttribute;
 import gov.nih.nci.gss.domain.DomainClass;
 import gov.nih.nci.gss.domain.DomainModel;
 import gov.nih.nci.gss.domain.GridService;
 import gov.nih.nci.gss.domain.HostingCenter;
 import gov.nih.nci.gss.domain.PointOfContact;
 import gov.nih.nci.gss.grid.DataServiceObjectCounter;
 import gov.nih.nci.gss.grid.GSSCredentials;
 import gov.nih.nci.gss.grid.GridAutoDiscoveryException;
 import gov.nih.nci.gss.grid.GridIndexService;
 import gov.nih.nci.gss.grid.GridServiceVerifier;
 import gov.nih.nci.gss.support.LastRefresh;
 import gov.nih.nci.gss.util.Cab2bAPI;
 import gov.nih.nci.gss.util.Cab2bTranslator;
 import gov.nih.nci.gss.util.GSSUtil;
 import gov.nih.nci.gss.util.GridServiceDAO;
 import gov.nih.nci.gss.util.NamingUtil;
 import gov.nih.nci.gss.util.Cab2bAPI.Cab2bService;
 import gov.nih.nci.system.applicationservice.ApplicationException;
 
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.exception.ConstraintViolationException;
 
 /**
  * Scheduled job for updating the GSS database periodically. Reads from the 
  * caGrid Index Service and updates corresponding objects in GSS. Everything
  * happens in a single atomic transaction. 
  * 
  * @author sahnih
  * Modified by piepenbringc for use in GSS
  * - Retrieve all grid services from an index service and update the local database of
  *   grid services accordingly.
  */
 public class GridDiscoveryServiceJob {
     
 	private static Logger logger = Logger.getLogger(GridDiscoveryServiceJob.class);
 
 	private static final int NUM_QUERY_THREADS = 20;
 
 	private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
     
 	private static final int MAX_COUNT_ERROR_LEN = 5000;
     private static final int MAX_COUNT_STACKTRACE_LEN = 50000;
 	
     private static final String STATUS_CHANGE_ACTIVE   = "ACTIVE";
     private static final String STATUS_CHANGE_INACTIVE = "INACTIVE";
     
     private Cab2bTranslator xlateUtil = null;
     private NamingUtil namingUtil = null;
     private Map<String,Cab2bService> cab2bServices = null;
 
     /** Cache for JSON responses */
     private Map cache;
     
     private SessionFactory sessionFactory;
 
     private Session hibernateSession;
     
 	public GridDiscoveryServiceJob() {
 		logger.info("Creating GridDiscoveryServiceJob");
 	}
 	
 	public void setCache(Map cache) {
 		this.cache = cache;
 	}
 
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		logger.info("Setting session factory: "+sessionFactory);
 		this.sessionFactory = sessionFactory;
 	}
 
 	public void execute() throws Exception {
 
 	    // Initialize helper classes
         this.xlateUtil = new Cab2bTranslator(sessionFactory);
         this.namingUtil = new NamingUtil(sessionFactory);
         Cab2bAPI cab2bAPI = new Cab2bAPI(xlateUtil);
         this.cab2bServices = cab2bAPI.getServices();
         
         Map<String,GridService> gridNodes = null;
         
         try {
             logger.info("Logged into Globus: "+GSSCredentials.getCredential());
             // Get services from Grid Index Service
             gridNodes = populateRemoteServices();
         }
         catch (GridAutoDiscoveryException e) {
         	Throwable root = GSSUtil.getRootException(e);
 			if (root instanceof SocketException || 
 					root instanceof SocketTimeoutException) {
 				logger.warn("Could not connect to index service.");
 				return;
 			}
 			else {
 				throw e;
 			}
         }
         
         try {
             hibernateSession = sessionFactory.openSession();
             
             // Merge with our database to get a complete list of all services we know about
             Map<String,GridService> allServices = mergeWithGss(gridNodes);
             
             if (allServices != null) {
                 // Verify accessibility
                 verifyAccessibility(allServices);
                 
                 // Update counts
                 updateCounts(allServices);
                 
                 // Update services as necessary or add new ones
                 saveServices(allServices, gridNodes.size());
 
                 // Clear the JSON cache
                 cache.clear();
             }
         }
         finally {
             hibernateSession.close();
             hibernateSession = null;
         }
         
 	}
 	
 	/**
 	 * @return List<GridNodeBean>
 	 */
 	private Map<String,GridService> populateRemoteServices() 
 	        throws GridAutoDiscoveryException {
 
 		// Build a hash on URL for GridServices
 		HashMap<String,GridService> serviceMap = new HashMap<String,GridService>();
 
 		logger.info("Discovering grid services");
 
 		// auto-discover grid nodes and save in session
 		
 		List<GridService> list = GridIndexService.discoverGridServices();
 
 		if (list != null) {
 			for (GridService service : list) {
 			    if (serviceMap.containsKey(service.getUrl())) {
 			        logger.warn("Index Service returned duplicate service URL: "+
 			            service.getUrl());
 			    }
 				serviceMap.put(service.getUrl(), service);
 			}
 		}
 		
 		return serviceMap;
 	}
 
 	/**
 	 * Merge the services reported by the Index Service with what is 
 	 * currently in the GSS database.
 	 * @param gridNodes
 	 * @return
 	 */
     private HashMap<String,GridService> mergeWithGss(Map<String,GridService> gridNodes) {
 
         HashMap<String,GridService> allServices = new HashMap<String,GridService>();
         int countNew = 0;
         int countUpdated = 0;
         int countInactive = 0;
         
         logger.info("Merging service metadata...");
         
         Collection<GridService> currentServices = null;
         Collection<HostingCenter> currentHosts = null;
         HashMap<String,GridService> serviceMap = null;
         HashMap<String,HostingCenter> hostMap = null;
         
         try {
             currentServices = GridServiceDAO.getServices(null,hibernateSession);
             // Build a hash on URL for GridServices
             serviceMap = new HashMap<String,GridService>();
             for (GridService service : currentServices) {
                 serviceMap.put(service.getUrl(), service);
             }
             
             currentHosts = GridServiceDAO.getHosts(null,hibernateSession);
             // Build a hash on hosting center long name for HostingCenters
             hostMap = new HashMap<String,HostingCenter>();
             for (HostingCenter host : currentHosts) {
                 hostMap.put(host.getLongName(), host);
             }
         }
         catch (ApplicationException e) {
             logger.error("Error getting service metadata from GSS database",e);
             return null;
         }
             
         // Walk the list of gridNodes and update the current services and hosting centers where necessary
         for (GridService service : gridNodes.values()) {
 
             logger.info("-------------------------------------------------");
             logger.info("Name: "+service.getName());
             logger.info("URL: "+service.getUrl());
             
             // Standardize the host long name
             HostingCenter thisHC = service.getHostingCenter();
             String hostLongName = null;
             if (thisHC != null) {
                 hostLongName = namingUtil.getSimpleHostName(thisHC.getLongName());
                 
                 // The trim is important because MySQL will consider two
                 // strings equal if the only difference is trailing whitespace
                 hostLongName = hostLongName.trim();
                 
                 if (!thisHC.getLongName().equals(hostLongName)) {
                     logger.info("Host name: "+hostLongName+" (was "+thisHC.getLongName()+")");
                     thisHC.setLongName(hostLongName);
                 }
                 else {
                     logger.info("Host name: "+thisHC.getLongName());
                 }
                 
                 // Create persistent identifier based on the long name
                 thisHC.setIdentifier(GSSUtil.generateHostIdentifier(thisHC));
                 
                 // Hide this host?
                 thisHC.setHiddenDefault(namingUtil.isHidden(thisHC.getLongName()));
             }
             
             // Check to see if the hosting center already exists.
             if (thisHC != null) {
                 if (hostMap.containsKey(hostLongName)) {
                     HostingCenter matchingHost = hostMap.get(hostLongName);
                     matchingHost = updateHostData(matchingHost, thisHC);
                     service.setHostingCenter(matchingHost);
                     logger.info("Using existing host with id: "+matchingHost.getId());
                 }
                 else {
                     hostMap.put(hostLongName, thisHC);
                 }
             }
             
             if (serviceMap.containsKey(service.getUrl())) {
                 logger.info("Service already exists, updating...");
                 countUpdated++;
                 
                 // This service is already in the list of current services
                 GridService matchingSvc = serviceMap.get(service.getUrl());
                 
                 // Update any new data about this service
                 matchingSvc = updateServiceData(matchingSvc, service);

                // Check to see if this service is active once again
                if (STATUS_CHANGE_INACTIVE.equals(matchingSvc.getLastStatus())) {
                    // Service was marked as inactive, need to make it active now
                    service.setLastStatus(createStatus(true));
                } 

                 matchingSvc.setLastUpdate(new Date());
                 allServices.put(matchingSvc.getUrl(),matchingSvc);
             } 
             else {
                 logger.info("Creating new service...");
                 countNew++;
                 
                 // Mark this service as published/discovered now.  Also, give it a default status change of "up".
                 // TODO: Is there a better "publish date" in the service metadata?
                 service.setPublishDate(new Date());
 
                 // Set up service simple name and linkage to correct caB2B model group
                 service.setSimpleName(namingUtil.getSimpleServiceName(service.getName()));
                 
                 // Hide some core infrastructure services
                 service.setHiddenDefault(namingUtil.isHidden(service.getName()));
                 
                 // Create a persistent identifier based on the URL
                 service.setIdentifier(GSSUtil.generateServiceIdentifier(service));
 
                 if (service instanceof DataService) {
                     DataService dataService = (DataService)service;
                     dataService = updateCab2bData(dataService);
                 }
 
                 service.setLastUpdate(new Date());
                 service.setLastStatus(createStatus(true));
                 allServices.put(service.getUrl(),service);
             }
             
         }
         
         // Mark the services we didn't see as inactive
         for (GridService service : currentServices) {
             if (!gridNodes.containsKey(service.getUrl())) {
                 countInactive++;
                 logger.info("-------------------------------------------------");
                 logger.info("Name: "+service.getName());
                 logger.info("URL: "+service.getUrl());
                 logger.info("Not found in index service metadata.");
                 service.setLastStatus(createStatus(false));
                 allServices.put(service.getUrl(),service);
             }
         }
         
         logger.info("Database will be updated as follows:");
         logger.info("New services found: "+countNew);
         logger.info("Existing services updated: "+countUpdated);
         logger.info("Existing services marked inactive: "+countInactive);
         
         return allServices;
     }
     
     private void updateCounts(Map<String,GridService> gridNodes)  {
 
         ExecutorService parallelExecutor = Executors.newFixedThreadPool(NUM_QUERY_THREADS);
         
         List<DataServiceObjectCounter> counters = new ArrayList<DataServiceObjectCounter>();
                 
         logger.info("Updating counts...");
         for (GridService service : gridNodes.values()) {
             if (service instanceof DataService) {
                 
                 DataService dataService = (DataService)service;
                 DomainModel model = dataService.getDomainModel();
                 if (model == null) continue;
 
                 // clear everything so that there's no stale data if we give up early
                 for(DomainClass domainClass : model.getClasses()) {
                     domainClass.setCount(null);
                     domainClass.setCountDate(null);
                     domainClass.setCountError(null);
                     domainClass.setCountStacktrace(null);
                 }
                 
                 // Avoid services which didn't respond to a WSDL query
                 if (!service.getAccessible()) {
                     logger.info("Not attempting to count for inaccessible service: "+
                         service.getUrl());
                     continue;
                 }
                 
                 DataServiceObjectCounter counter = 
                     new DataServiceObjectCounter(dataService);
                 counters.add(counter);
                 parallelExecutor.submit(counter);
             }
         }
         
         try {
             parallelExecutor.shutdown();
             logger.info("Awaiting completion of object counting...");
             if (!parallelExecutor.awaitTermination(60*60, TimeUnit.SECONDS)) {
                 logger.info("Timed out waiting for counts to finish, disregarding remaining counts.");
                 // timed out, cancel the tasks
                 for(DataServiceObjectCounter counter : counters) {
                     synchronized (counter) {
                         counter.disregard();
                     }
                 }
             }
             logger.info("Object counting completed.");
         }
         catch (InterruptedException e) {
             logger.error("Could not update object counts",e);
         }
     }
 
 
     private void verifyAccessibility(Map<String,GridService> gridNodes)  {
 
         ExecutorService parallelExecutor = Executors.newFixedThreadPool(NUM_QUERY_THREADS);
         
         List<GridServiceVerifier> verifiers = new ArrayList<GridServiceVerifier>();
                 
         logger.info("Verifying accessibility...");
         for (GridService service : gridNodes.values()) {
             GridServiceVerifier verifier = 
                 new GridServiceVerifier(service);
             verifiers.add(verifier);
             parallelExecutor.submit(verifier);
         }
         
         try {
             parallelExecutor.shutdown();
             logger.info("Awaiting completion of service verification...");
             if (!parallelExecutor.awaitTermination(60*60, TimeUnit.SECONDS)) {
                 logger.info("Timed out waiting for counts to finish, disregarding remaining counts.");
                 // timed out, cancel the tasks
                 for(GridServiceVerifier verifier : verifiers) {
                     synchronized (verifier) {
                         verifier.disregard();
                     }
                 }
             }
             logger.info("Service verification completed.");
         }
         catch (InterruptedException e) {
             logger.error("Could not verify services",e);
         }
     }
 
     /**
      * Actually save all the changes made to the GSS object model.
      * @param services
      * @param numGridNodes
      */
     private void saveServices(Map<String,GridService> services, int numGridNodes) {
 
         logger.info("Updating GSS database...");
         
         Transaction tx = null;
 
         Date nowDate = new Date();
         long now = nowDate.getTime();
         
         try {
             tx = hibernateSession.beginTransaction();
             
             for(GridService service : services.values()) {
 
                 long diff = now - service.getLastUpdate().getTime();
                 // Last seen 1 day ago, and not accessible OR last seen a week ago but still accessible
                 if ((diff > DAY_IN_MILLIS && !service.getAccessible()) || (diff > 7*DAY_IN_MILLIS)) {
                     logger.info("Service defunct, deleting: "+service.getUrl());
                     deleteService(service);
                 }
                 else {
                     if ("INACTIVE".equals(service.getLastStatus())) {
                         logger.info("Service defunct, but not yet ready for deletion: "+service.getUrl());
                     }
                     saveService(service);
                 }
             }
             
             // Note that the update completed
             LastRefresh lastRefresh = GridServiceDAO.getLastRefreshObject(hibernateSession);
             lastRefresh.setCompletionDate(nowDate);
             lastRefresh.setNumServices(new Long(numGridNodes));
             hibernateSession.save(lastRefresh);
             
             logger.info("Commiting changes to GSS database...");
             tx.commit();
             logger.info("Commit complete.");
             
         } 
         catch (Exception e) {
             if (tx != null) {
                 tx.rollback();
             }
             logger.error("Error updating GSS database",e);
         }
     }
     
 	private void saveService(GridService service) {
 
 		try {
 			// Domain classes are saved in reverse referencing order 
 
 			// 1) All POCs
 			for (PointOfContact POC : service.getPointOfContacts()) {
                 logger.debug("Saving Service POC "+POC.getName());
                 POC.setId((Long)hibernateSession.save(POC));
 			}
 			HostingCenter hc = service.getHostingCenter();
 			if (hc != null) {
 				for (PointOfContact POC : hc.getPointOfContacts()) {
 	                logger.debug("Saving Host POC "+POC.getName());
 	                POC.setId((Long)hibernateSession.save(POC));
 				}
 
                 // 2) Hosting Center
 				if (hc.getId() == null) {
 				    logger.debug("Saving Host: "+hc.getLongName());
 				    // Hosting center has not been saved yet
 				    hc.setId((Long)hibernateSession.save(hc));
 				}
 			}
 	
 			// 3) Domain Model
 			if (service instanceof DataService) {
 			    DomainModel model = ((DataService)service).getDomainModel();
 			    if (model != null) {
     	            logger.info("Saving Domain Model: "+model.getLongName());
     				model.setId((Long)hibernateSession.save(model));
     				
                     // 4) Domain Classes 
                     logger.debug("Saving "+model.getClasses().size()+" Domain Classes");
                     for(DomainClass domainClass : model.getClasses()) {
 
                         // truncate values that are too long to fit in the DB
                         
                         if (domainClass.getCountError() != null) {
                             if (domainClass.getCountError().length() > MAX_COUNT_ERROR_LEN) {
                                 logger.warn("Truncating long count error for: "+service.getUrl());
                                 domainClass.setCountError(
                                     domainClass.getCountError().substring(
                                     0, MAX_COUNT_ERROR_LEN-3)+"...");
                             }
                         }
 
                         if (domainClass.getCountStacktrace() != null) {
                             if (domainClass.getCountStacktrace().length() > MAX_COUNT_STACKTRACE_LEN) {
                                 logger.warn("Truncating long count stacktrace for: "+service.getUrl());
                                 domainClass.setCountStacktrace(
                                     domainClass.getCountStacktrace().substring(
                                     0, MAX_COUNT_STACKTRACE_LEN-3)+"...");
                             }
                         }
 
                         logger.debug("Saving Class: "+domainClass.getClassName());
                         domainClass.setId((Long)hibernateSession.save(domainClass));
                         
                         // 5) Domain Attributes
                         if (domainClass.getAttributes() != null) {
                             logger.debug("Saving "+domainClass.getAttributes().size()+" Domain Attributes");
                             for(DomainAttribute domainAttr : domainClass.getAttributes()) {
                                 domainAttr.setId((Long)hibernateSession.save(domainAttr));
                             }
                         }
                         else {
                             logger.debug("Null Domain Attributes List");
                         
                         }
                     }
 			    }
 			}
 			
 			// 5) Grid Service
             logger.debug("Saving Service: "+service.getName());
             service.setId((Long)hibernateSession.save(service));
 			
 		} 
 		catch (ConstraintViolationException e) {
 			logger.warn("Duplicate object for: " + service.getUrl(),e);
 		} 
 		catch (RuntimeException e) {
 			logger.warn("Unable to save GridService",e);
 		}
 	}
 
     private void deleteService(GridService service) {
 
         try {
             // Domain classes are deleted in referencing order 
 
             // 1) Grid Service
             logger.info("Deleting Service: "+service.getName());
             hibernateSession.delete(service);
             
             if (service instanceof DataService) {
                 DomainModel model = ((DataService)service).getDomainModel();
                 if (model != null) {
                     
                     for(DomainClass domainClass : model.getClasses()) {
 
                         // 2) Domain Attributes
                         if (domainClass.getAttributes() != null) {
                             logger.debug("Deleting "+domainClass.getAttributes().size()+" Domain Attributes");
                             for(DomainAttribute domainAttr : domainClass.getAttributes()) {
                                 hibernateSession.delete(domainAttr);
                             }
                         }
                         
                         // 3) Domain Classes 
                         hibernateSession.delete(domainClass);
                     }
                     
                     logger.debug("Deleting "+model.getClasses().size()+" Domain Classes");
                     
                     // 4) Domain Model
                     logger.debug("Deleting Domain Model: "+model.getLongName());
                     hibernateSession.delete(model);
                 }
             }
             
             // 1) All POCs
             for (PointOfContact POC : service.getPointOfContacts()) {
                 logger.info("Deleting Service POC "+POC.getName());
                 hibernateSession.delete(POC);
             }
 
             // Don't delete the hosting center in case other services are there,
             // or if services are added at a later date
     
         } 
         catch (ConstraintViolationException e) {
             logger.warn("Violated constraint deleting: " + service.getUrl(),e);
         } 
         catch (RuntimeException e) {
             logger.warn("Unable to delete GridService",e);
         }
     }
     
     private String createStatus(Boolean isActive) {
         return isActive ? STATUS_CHANGE_ACTIVE : STATUS_CHANGE_INACTIVE;
     }
 
 	private HostingCenter updateHostData(HostingCenter matchingHost,
 			HostingCenter host) {
 
 		// Copy over data from the new host data
 		// - Do not overwrite: long name (unique key), id (db primary key)
         matchingHost.setHiddenDefault(host.getHiddenDefault());
 		matchingHost.setCountryCode(host.getCountryCode());
 		matchingHost.setLocality(host.getLocality());
 		matchingHost.setPostalCode(host.getPostalCode());
 		matchingHost.setShortName(host.getShortName());
 		matchingHost.setStateProvince(host.getStateProvince());
 		matchingHost.setStreet(host.getStreet());
 		
 		return matchingHost;
 	}
 
 	private DataService updateCab2bData(DataService dataService) {
         
         Cab2bService cab2bService = cab2bServices.get(dataService.getUrl());
         if (cab2bService != null) {
             
             // Translate the caB2B model group to a service group
             DataServiceGroup group = xlateUtil.getServiceGroupForModelGroup(
                     cab2bService.getModelGroupName());
             // Populate service attributes
             dataService.setGroup(group);
             dataService.setSearchDefault(cab2bService.isSearchDefault());
             
             if (group == null) {
                 logger.info("Found service in caB2B but could not " +
                 		"translate group "+cab2bService.getModelGroupName());
             }
             else {
                 logger.info("Found service in caB2B under group "+
                     group.getName()+" with searchDefault="+
                     cab2bService.isSearchDefault());
             }
         }
         else {
             dataService.setSearchDefault(false);
         }
         
         return dataService;
 	}
 	
 	private GridService updateServiceData(GridService matchingSvc,
 			GridService service) {
         
 		// Copy over data from the new service
 		// - Do not overwrite: url (unique keys), id (db primary key), publish date (should stay the original value)
 		matchingSvc.setName(service.getName());
 		matchingSvc.setSimpleName(namingUtil.getSimpleServiceName(service.getName()));
 
         // Hide this service?
 		matchingSvc.setHiddenDefault(namingUtil.isHidden(service.getName()));
         
 		matchingSvc.setVersion(service.getVersion());
 		matchingSvc.setDescription(service.getDescription());
 		matchingSvc.setHostingCenter(service.getHostingCenter());
 		
 		if (matchingSvc instanceof DataService && service instanceof DataService) {
             DataService dataService = (DataService)service;
 		    DataService matchingDataSvc = (DataService)matchingSvc;
 
 	        // We are consciously overwriting things here that likely will not change,
 	        // since they are based on the URL, which is guaranteed to be the same if we
 	        // call this function.  However, on the off chance that the DB lookup tables or
 	        // caB2B content has changed, we need to overwrite here to be sure.
 		    updateCab2bData(matchingDataSvc);
 		    
 		    // Update domain model
 		    DomainModel model = dataService.getDomainModel();
             DomainModel matchingModel = matchingDataSvc.getDomainModel();
             
             if (matchingModel == null) {
                 logger.warn("Existing data service has no model: "+service.getUrl());
                 matchingDataSvc.setDomainModel(model);
                 return matchingSvc;
             }
             
             if (model == null) {
                 logger.warn("Data service has no model: "+service.getUrl());
                 return matchingSvc;
             }
             
             matchingModel.setDescription(model.getDescription());
             matchingModel.setLongName(model.getLongName());
             matchingModel.setVersion(model.getVersion());
 
             // Add existing classes to a map
             Map<String,DomainClass> existingClasses = new HashMap<String,DomainClass>();
             for(DomainClass domainClass : matchingModel.getClasses()) {
                 String fullClass = domainClass.getDomainPackage()+"."+domainClass.getClassName();
                 existingClasses.put(fullClass,domainClass);
             }
             
             // Go through new classes and see if they exist already
 		    for(DomainClass domainClass : model.getClasses()) {
 
                 String fullClass = domainClass.getDomainPackage()+"."+domainClass.getClassName();
                 logger.debug("  Existing class: "+fullClass);
                 
 		        if (existingClasses.containsKey(fullClass)) {
 		            // Update existing class with new metadata
                     DomainClass matchingClass = existingClasses.get(fullClass);
                     matchingClass.setDescription(domainClass.getDescription());
                     
                     // Add existing attributes to a map
                     Map<String,DomainAttribute> existingAttrs = new HashMap<String,DomainAttribute>();
                     for(DomainAttribute domainAttr : matchingClass.getAttributes()) {
                         existingAttrs.put(domainAttr.getAttributeName(),domainAttr);
                     }
 
                     // Go through new attributes
                     for(DomainAttribute domainAttr : matchingClass.getAttributes()) {
                         if (existingAttrs.containsKey(domainAttr.getAttributeName())) {
                             // Update existing attr with new metadata
                             logger.debug("    Existing attr: "+domainAttr.getAttributeName());
                             DomainAttribute matchingAttr = existingAttrs.get(domainAttr.getAttributeName());
                             matchingAttr.setCdePublicId(domainAttr.getCdePublicId());
                             matchingAttr.setDataTypeName(domainAttr.getDataTypeName());
                         }
                         else {
                             // Add new class
                             logger.debug("    New attr: "+domainAttr.getAttributeName());
                             domainAttr.setDomainClass(matchingClass);
                             matchingClass.getAttributes().add(domainAttr);
                         }
                     }
 		        }
 		        else {
 		            // Add new class
 	                logger.debug("  New class: "+fullClass);
 	                domainClass.setModel(matchingModel);
                     matchingModel.getClasses().add(domainClass);
 		        }
 		    }
 		    
 		    // TODO: handle domain class and attribute deletions
 		}
 		
 		return matchingSvc;
 	}
 }
