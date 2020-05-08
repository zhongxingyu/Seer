 package org.vpac.grisu.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.model.info.ApplicationInformation;
 import org.vpac.grisu.model.info.ApplicationInformationImpl;
 import org.vpac.grisu.model.info.ResourceInformation;
 import org.vpac.grisu.model.info.ResourceInformationImpl;
 import org.vpac.grisu.model.info.UserApplicationInformation;
 import org.vpac.grisu.model.info.UserApplicationInformationImpl;
 import org.vpac.grisu.settings.Environment;
 import org.vpac.historyRepeater.DummyHistoryManager;
 import org.vpac.historyRepeater.HistoryManager;
 import org.vpac.historyRepeater.SimpleHistoryManager;
 
 /**
  * The GrisuRegistry provides access to all kinds of information via an easy-to-use api.
  * 
  * You can access the following information objects via the registry:
  * 
  * {@link UserEnvironmentManager}: to find out what resources the current user can access <br/>
  * {@link UserApplicationInformation}: information about the applications/versions of applications this user has got access to<br/>
  * {@link ApplicationInformation}: information about the applications that are provided grid-wide<br/>
  * {@link ResourceInformation}: general information about the available resources like queues, submissionlocations, stagingfilesystems<br/>
 * {@link HistoryManager}: can be used to store / retrieve data that the user used in past jobs<br/>
 * {@link FileManager}: to do file transfer and such<br/>
  *  
  * @author Markus Binsteiner
  *
  */
 public class GrisuRegistry {
 	
 
 	// caching the registries for different serviceinterfaces. for a desktop application this most
 	// likely only ever contains only one registry object. But for webapplications it can hold more 
 	// than that. Advantage is that several users can share for example the ResourceInformation object
 	// but can have (or must have) seperate UserApplicationInformation objects
 	private static Map<ServiceInterface, GrisuRegistry> cachedRegistries = new HashMap<ServiceInterface, GrisuRegistry>();
 	
 	public static GrisuRegistry getDefault(ServiceInterface serviceInterface) {
 		
 		if ( serviceInterface == null ) {
 			throw new RuntimeException("ServiceInterface not initialized yet. Can't get default registry...");	
 		}
 		
 		if ( cachedRegistries.get(serviceInterface) == null ) {
 			GrisuRegistry temp = new GrisuRegistry(serviceInterface);
 			cachedRegistries.put(serviceInterface, temp);
 		}
 		
 		return cachedRegistries.get(serviceInterface);
 	}
 	
 
 	private final ServiceInterface serviceInterface;
 
 	public static final String GRISU_HISTORY_FILENAME = "grisu.history";
 	
 	private HistoryManager historyManager = null;
 	private Map<String, ApplicationInformation> cachedApplicationInformationObjects = new HashMap<String, ApplicationInformation>();
 	private Map<String, UserApplicationInformation> cachedUserInformationObjects = new HashMap<String, UserApplicationInformation>();
 	private UserEnvironmentManager cachedUserInformation;
 	private ResourceInformation cachedResourceInformation;
 	private FileManager cachedFileHelper;
 	
 	public GrisuRegistry(ServiceInterface serviceInterface) {
 		this.serviceInterface = serviceInterface;
 	}
 	
 	/**
 	 * Sets the {@link UserEnvironmentManager} for this registry object.
 	 * 
 	 * @param ui the UserEnvironmentManager
 	 */
 	public void setUserEnvironmentManager(UserEnvironmentManager ui) {
 		this.cachedUserInformation = ui;
 	}
 	
 	/**
 	 * Gets the UserApplicationInformationObject for the specified application.
 	 * 
 	 * If an UserApplicationInformationObject for this application was already specified, 
 	 * a cached version will be returned.
 	 * 
 	 * @param applicationName the name of the application
 	 * @return the information object for this application and user
 	 */
 	public UserApplicationInformation getUserApplicationInformation(String applicationName) {
 		
 		if ( cachedUserInformationObjects.get(applicationName) == null ) {
 			UserApplicationInformation temp = new UserApplicationInformationImpl(serviceInterface, getUserEnvironmentManager(), applicationName);
 			cachedUserInformationObjects.put(applicationName, temp);
 		}
 		return cachedUserInformationObjects.get(applicationName);
 	}
 	
 	/**
 	 * Gets the ApplicationInformationObject for the specified application.
 	 * 
 	 * If an ApplicationInformationObject for this application was already specified, 
 	 * a cached version will be returned.
 	 * 
 	 * @param applicationName the name of the application
 	 * @return the information object for this application
 	 */
 	public ApplicationInformation getApplicationInformation(String applicationName) {
 		
 		if ( cachedApplicationInformationObjects.get(applicationName) == null ) {
 			ApplicationInformation temp = new ApplicationInformationImpl(serviceInterface, applicationName);
 			cachedApplicationInformationObjects.put(applicationName, temp);
 		}
 		return cachedApplicationInformationObjects.get(applicationName);
 	}
 	
 	/**
 	 * Returns the management object for this users enironment.
 	 *  
 	 * @return the UserEnvironmentManager object for this user
 	 */
 	public UserEnvironmentManager getUserEnvironmentManager() {
 		
 		if ( cachedUserInformation == null ) {
 			this.cachedUserInformation = new UserEnvironmentManagerImpl(serviceInterface);
 		}
 		return cachedUserInformation;
 	}
 	
 	/**
 	 * Returns the resource information object that can be used to get information about the
 	 * resources in this grid.
 	 * 
 	 * @return the resource information object
 	 */
 	public ResourceInformation getResourceInformation() {
 		if ( cachedResourceInformation == null ) {
 			cachedResourceInformation = new ResourceInformationImpl(serviceInterface);
 		}
 		return cachedResourceInformation;
 	}
 	
 	/**
 	 * Returns the history manager object for this user. By default it returns an object of
 	 * the {@link SimpleHistoryManager} class which uses the grisu.history file in the grisu 
 	 * directory to store the users history.
 	 * 
 	 * @return the history manager object
 	 */
 	public HistoryManager getHistoryManager() {
 		if ( historyManager == null ) {
 			File historyFile = new File(Environment.GRISU_DIRECTORY,
 					GRISU_HISTORY_FILENAME);
 			if (!historyFile.exists()) {
 				try {
 					historyFile.createNewFile();
 
 				} catch (IOException e) {
 					// well
 				}
 			}
 			if (!historyFile.exists()) {
 				historyManager = new DummyHistoryManager();
 			} else {
 				historyManager = new SimpleHistoryManager(historyFile);
 			}
 		}
 		return historyManager;
 	}
 	
 	/**
 	 * Returns an object to help with file(-transfer) related things.
 	 * 
 	 * @return the file manager
 	 */
 	public FileManager getFileManager() {
 		if ( cachedFileHelper == null ) {
 			cachedFileHelper = new FileManager(serviceInterface);
 		}
 		return cachedFileHelper;
 	}
 
 }
