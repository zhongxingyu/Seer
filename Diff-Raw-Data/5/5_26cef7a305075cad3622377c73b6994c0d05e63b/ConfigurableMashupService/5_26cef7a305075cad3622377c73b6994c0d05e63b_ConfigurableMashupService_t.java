 /*******************************************************************************
  * Copyright (c) 2013 Peter Lachenmaier - Cooperation Systems Center Munich (CSCM).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Peter Lachenmaier - Design and initial implementation
  ******************************************************************************/
 package org.sociotech.communitymashup.configurablemashupservice.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
 import org.osgi.framework.BundleContext;
 import org.osgi.service.log.LogService;
 import org.sociotech.communitymashup.application.ApplicationPackage;
 import org.sociotech.communitymashup.application.Configuration;
 import org.sociotech.communitymashup.application.Interface;
 import org.sociotech.communitymashup.application.Mashup;
 import org.sociotech.communitymashup.application.Property;
 import org.sociotech.communitymashup.application.Source;
 import org.sociotech.communitymashup.application.SourceActiveStates;
 import org.sociotech.communitymashup.application.SourceState;
 import org.sociotech.communitymashup.configurablemashupservice.ConfigurableMashupBundleActivator;
 import org.sociotech.communitymashup.configurablemashupservice.instantiation.servicetrackers.InterfaceFactoryTracker;
 import org.sociotech.communitymashup.configurablemashupservice.instantiation.servicetrackers.SourceFactoryTracker;
 import org.sociotech.communitymashup.configurablemashupservice.update.UpdateThread;
 import org.sociotech.communitymashup.configuration.observer.mashup.MashupChangeObserver;
 import org.sociotech.communitymashup.configuration.observer.mashup.MashupChangedInterface;
 import org.sociotech.communitymashup.data.DataPackage;
 import org.sociotech.communitymashup.data.DataSet;
 import org.sociotech.communitymashup.data.observer.dataset.DataSetChangeObserver;
 import org.sociotech.communitymashup.data.observer.dataset.DataSetChangedInterface;
 import org.sociotech.communitymashup.interfaceprovider.facade.InterfaceServiceFacade;
 import org.sociotech.communitymashup.interfaceprovider.factory.facade.InterfaceFactoryFacade;
 import org.sociotech.communitymashup.mashup.impl.MashupServiceFacadeImpl;
 import org.sociotech.communitymashup.mashup.properties.MashupProperties;
 import org.sociotech.communitymashup.source.facade.SourceServiceFacade;
 import org.sociotech.communitymashup.source.factory.facade.SourceFactoryServiceFacade;
 import org.sociotech.communitymashup.source.factory.facade.callback.AsynchronousSourceInstantiationCallback;
 
 /**
  * @author Peter Lachenmaier
  * 
  * This is a configurable implementation of a mashup service. 
  */
 public class ConfigurableMashupService extends MashupServiceFacadeImpl implements AsynchronousSourceInstantiationCallback, MashupChangedInterface, DataSetChangedInterface {
 
 	/**
 	 * The system specific file separator 
 	 */
 	private static String fileSeparator = System.getProperty("file.separator");
 	
 	/**
 	 * Constant for the data folder
 	 */
 	private static final String DEFAULT_DATA_FOLDER = "data" + fileSeparator;
 
 	/**
 	 * Constant for the data backup folder
 	 */
 	private static final String DEFAULT_DATA_BACKUP_FOLDER = DEFAULT_DATA_FOLDER + "backup" + fileSeparator;;
 
 	/**
 	 * Constant for the attachments cache folder
 	 */
 	private static final String DEFAULT_ATTACHMENT_FOLDER = DEFAULT_DATA_FOLDER + "attachments" + fileSeparator;;
 
 	/**
 	 * Constant for the data set file name
 	 */
 	private static final String DEFAULT_DATASET_FILENAME = "dataSet.xml";
 	
 	/**
 	 * The filename used in this mashup.
 	 */
 	private String dataSetFilename = DEFAULT_DATASET_FILENAME;
 	
 	/**
 	 * Reference to a source factory that will be used for instantiating needed sources.
 	 * The reference will be maintained by {@link #setSourceFactory(SourceFactoryServiceFacade)}
 	 * and {@link #unsetSourceFactory()}
 	 */
 	private SourceFactoryServiceFacade sourceFactory = null;
 
 	/**
 	 * Reference to a interface factory that will be used for instantiating needed interfaces.
 	 * The reference will be maintained by {@link #setInterfaceFactory(InterfaceFactoryFacade)}
 	 * and {@link #unsetInterfaceFactory()}
 	 */
 	private InterfaceFactoryFacade interfaceFactory = null;
 
 	/**
 	 *	Represents if the configuration of this mashup service is completely fullfilled. 
 	 */
 	private boolean configurationFullfilled = false;
 
 	/**
 	 *	Represents if the data enrichment process was started for all sources. 
 	 */
 	private boolean startedEnrichment = false;
 
 	/**
 	 * A map that keeps the assignment of source services to source configurations.
 	 */
 	private Map<Source, SourceServiceFacade> sourceServices = null;
 
 	/**
 	 * This list keeps configurations of sources for which an asynchronous instantiation process is started.
 	 */
 	private List<Source> asynchronousInstantionStarted = null;
 
 	/**
 	 * This list keeps configurations of sources that are waiting for instantiation.
 	 */
 	private List<Source> sourcesWaitingForInstantiation = null;
 
 	/**
 	 * This list keeps configurations of interfaces that are waiting for instantiation.
 	 */
 	private List<Interface> interfacesWaitingForInstantiation = null;
 	
 	/**
 	 * A map that keeps the assignment of interface services to interface configurations.
 	 */
 	private Map<Interface, InterfaceServiceFacade> interfaceServices = null;
 
 	/**
 	 * Reference to the opened interface factory tracker
 	 */
 	private InterfaceFactoryTracker interfaceFactoryTracker;
 
 	/**
 	 * Reference to the opened source factory tracker
 	 */
 	private SourceFactoryTracker sourceFactoryTracker;
 
 	/**
 	 * Thread that cyclic calls the update method
 	 */
 	private UpdateThread updateThread;
 
 	/**
 	 * Working directory, is set when configuration contains valid path
 	 */
 	private File workingDirectory = null;
 	
 	/**
 	 * Directory used for data
 	 */
 	private File dataDirectory = null;
 	
 	/**
 	 * Directory used for backups of the data set
 	 */
 	private File dataBackupDirectory = null;
 	
 	/**
 	 * Directory used as attachments cache
 	 */
 	private File attachmentsCacheDirectory = null;
 
 	/**
 	 * Reference to the observer of the mashup configuration
 	 */
 	private MashupChangeObserver mashupConfigurationObserver = null;
 	
 	/**
 	 * Indicates if the data set should be cached 
 	 */
 	private boolean cacheDataSet = false;
 	
 	/**
 	 * Indicates if the data set should be backuped 
 	 */
 	private boolean backupDataSet = false;
 	
 	/**
 	 * Indicates if their is a data set change to save
 	 */
 	private boolean needSave = false;
 
 	/**
 	 * Indicates if their is a data set change to backup
 	 */
 	private boolean needBackup = false;
 
 	/**
 	 * Indicates if it is possible to save (needs external working directory)
 	 */
 	private boolean canSave = false;
 
 	/**
 	 * Reference to the data set observer
 	 */
 	private DataSetChangeObserver dataSetChangeObserver = null;
 	
 	/**
 	 * Time intervall to backup the data set 
 	 */
 	private long backupInterval;
 	
 	/**
 	 * Time to wait between data set saves.
 	 */
 	private long saveDelay;
 
 	/**
 	 * Reference to the data set backup thread
 	 */
 	private DataSetBackupThread backupThread = null;
 	
 	/**
 	 * Reference to the data set cache thread
 	 */
 	private DataSetCacheThread cacheThread = null;
 	
 	/**
 	 * Reference to the loaded data set resource used for caching
 	 */
 	private Resource dataSetResource = null;
 
 	/**
 	 * Indicates if an configuration is initially loaded
 	 */
 	private boolean configurationLoaded = false;
 	
 	/**
 	 * Default constructor that is just initializing local lists.
 	 */
 	public ConfigurableMashupService() {
 
 		// empty map of used source services
 		sourceServices = new HashMap<Source, SourceServiceFacade>();
 
 		// empty list for started asynchronous instantiation
 		asynchronousInstantionStarted = new LinkedList<Source>();
 
 		// empty list for source configurations waiting for instantiation
 		sourcesWaitingForInstantiation = new LinkedList<Source>();
 
 		// empty list for interface configurations waiting for instantiation
 		interfacesWaitingForInstantiation = new LinkedList<Interface>();
 
 		// empty map of used interface services
 		interfaceServices = new HashMap<Interface, InterfaceServiceFacade>();
 		
 		BundleContext context = ConfigurableMashupBundleActivator.getContext();
 
 		// create and open service trackers for factory services
 		interfaceFactoryTracker = new InterfaceFactoryTracker(context, this);
 		interfaceFactoryTracker.open();
 
 		sourceFactoryTracker = new SourceFactoryTracker(context, this);
 		sourceFactoryTracker.open();
 	}
 
 	/**
 	 * Creates a new and empty data set and returns it
 	 * 
 	 * @return The newly created data set.
 	 */
 	private DataSet initializeNewDataSet()
 	{
 		// create an empty data set
 		DataSet newDataSet = DataPackage.eINSTANCE.getDataFactory().createDataSet();
 
 		// create resource for data set
 		ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("*", new ResourceFactoryImpl());
 		Resource resource = resourceSet.createResource(URI.createURI("ConfigurableCommunityMashup"));
 
 		resource.getContents().add(newDataSet);
 				
 		// set attachment caching
 		boolean shouldCacheAttachments = this.mashup.getCacheAttachments();
 		newDataSet.setCacheFileAttachements(shouldCacheAttachments);
 
 		return newDataSet;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#getDataSet()
 	 */
 	@Override
 	public DataSet getDataSet() {
 		if(this.isInitialized() || this.mashup != null)
 		{
 			return this.mashup.getDataSet();
 		}
 
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.mashup.impl.MashupServiceFacadeImpl#loadConfiguration(org.sociotech.communitymashup.application.Mashup)
 	 */
 	@Override
 	public Mashup loadConfiguration(Mashup configuration) {
 
 		super.loadConfiguration(configuration);
 
 		if(mashup == null)
 		{
 			// could not start mashup service without configuration
 			this.log("No valid mashup configuration: Could not start up mashup service.", LogService.LOG_ERROR);
 			return null;
 		}
 
 		// set bundle context
 		this.setContext(ConfigurableMashupBundleActivator.getContext());
 		
 		// initialize with "source" configuration (opens up log tracker etc.)
 		super.initialize(mashup);
 				
 		// track changes of configuration
 		mashupConfigurationObserver = new MashupChangeObserver(mashup, this);
 
 		if(mashup.getState() != SourceState.ACTIVE)
 		{
 			log("Waiting until mashup gets activated to start!", LogService.LOG_DEBUG);
 			return mashup;
 		}
 		
 		configurationLoaded = true;
 		
 		// mashup is also a source
 		this.source = mashup;
 
 		// prepare working directory
 		// save is possible if working directory is completely prepared
 		this.canSave = prepareWorkingDirectory(mashup.getWorkingDirectory());
 		
 		// interpret mashup configuration attributes
 		interpretMashupConfigurationAttributes();
 		
 		// load or create data set
 		prepareDataSet();
 
 		// interpret again to maybe transfer attributes to data set
 		interpretMashupConfigurationAttributes();
 		
 		// create all sources
 
 		EList<Source> sources = mashup.getSources();
 
 		if(sources == null || sources.isEmpty())
 		{
 			// nothing to do yet
 			configurationFullfilled  = true;
 			return mashup;
 		}
 
 		// first put all source on waiting list
 		sourcesWaitingForInstantiation.addAll(sources);
 		
 		// instantiate and initalize all needed source services
 		for(Source currentSource : sources)
 		{
 			// create a source service for every source configuration
 			createSourceService(currentSource);
 		}
 
 		// create all interfaces
 
 		EList<Interface> interfaces = mashup.getInterfaces();
 
 		for(Interface currentInterface : interfaces)
 		{
 			// create a interface for every interface in the configuration
 			createInterfaceService(currentInterface);
 		}
 
 		return mashup;
 	}
 
 
 	/**
 	 * Interprets the attribute values set in the mashup configuration.
 	 */
 	private void interpretMashupConfigurationAttributes() {
 		// interpret attributes
 		this.cacheDataSet 	= mashup.getCacheDataSet();
 		this.backupDataSet 	= mashup.getBackupDataSet();
 		
 		// transform s to ms
 		this.saveDelay 		= 1000 * mashup.getCacheDelay();
 		if(this.saveDelay < 1000)
 		{
 			// dont save quicker than seconds
 			this.saveDelay = 1000;
 		}
 		
 		this.backupInterval = 1000 * mashup.getBackupIntervall();
 		if(this.backupInterval < 10000)
 		{
 			// dont backup quicker than 10 seconds
 			this.backupInterval = 1000;
 		}
 		
 		logLevel = mashup.getLogLevelIntValue();
 		
 		if(mashup.getDataSet() != null)
 		{
 			// attachements caching is done by the data set
 			mashup.getDataSet().setCacheFileAttachements(mashup.getCacheAttachments());
 			// set mashup log level also for data set
 			mashup.getDataSet().setLogLevel(logLevel);
 		}
 		
 		// TODO check if working directory changes and maybe rename directory
 		
 		if(this.backupDataSet && this.canSave)
 		{
 			createBackupThread();
 		}
 		else if(backupThread != null)
 		{
 			// stop thread
 			backupThread.interrupt();
 			backupThread = null;
 		}
 		
 		if(this.cacheDataSet && this.canSave)
 		{
 			createCacheThread();
 			if(dataSetResource == null)
 			{
 				prepareDataSetResource();
 			}
 		}
 		else if(cacheThread != null)
 		{
 			// stop thread
 			cacheThread.interrupt();
 			cacheThread = null;
 		}
 	}
 
 
 	/**
 	 * Creates and starts a thread to create backups in a configured time interval
 	 */
 	private void createBackupThread() {
 		
 		if(this.backupThread  == null)
 		{
 			// create thread
 			this.backupThread = new DataSetBackupThread(this);
 		}
 		
 		// set backup interval
 		this.backupThread.setBackupInterval(this.backupInterval);
 		
 		if(this.canSave && this.backupDataSet)
 		{
 			// start thread
 			this.backupThread.start();
 		}
 	}
 	
 	/**
 	 * Creates and starts a thread to create backups in a configured time interval
 	 */
 	private void createCacheThread() {
 		
 		if(this.cacheThread == null)
 		{
 			// create thread
 			this.cacheThread = new DataSetCacheThread(this);
 		}
 		
 		// set cache delay to callback
 		this.cacheThread.setCacheInterval(this.saveDelay);
 		
 		if(this.canSave && this.cacheDataSet)
 		{
 			// start thread
 			this.cacheThread.start();
 		}
 	}
 	/**
 	 * If a working directory is set and a previous data set file exists than the data set is loaded.
 	 * If the data set is not yet existing than a new one will be created and the source states will be
 	 * reseted.
 	 */
 	private void prepareDataSet() {
 		DataSet existingDataSet = null;
 		
 		// if data directory exists and caching is switched on, try to load data set
 		if(dataDirectory != null && this.cacheDataSet)
 		{
 			// try to load existing data set
 			existingDataSet = loadExistingDataSet();
 		}
 		
 		if(existingDataSet == null)
 		{
 			// no dataset currently exist
 			// initialize new data set
 			existingDataSet = initializeNewDataSet();
 		}
 		
 		// set it for usage
 		mashup.setDataSet(existingDataSet);
 				
 		// if not loaded prepare save resource
 		if(dataSetResource == null && dataDirectory != null && this.cacheDataSet)
 		{
 			prepareDataSetResource();
 		}	
 	
 		// now data set is loaded or created
 		
 		dataSetChangeObserver = new DataSetChangeObserver(mashup.getDataSet(), this);
 		
 		if(attachmentsCacheDirectory != null)
 		{
 			// set it in the data set to cache attachments in this directory
 			mashup.getDataSet().setCacheFolder(attachmentsCacheDirectory.getAbsolutePath());
 		}
 		
 		// set same log service
 		mashup.getDataSet().setLogService(this.getLogService());
 		
 	}
 
 	private void prepareDataSetResource() {
 		// create resource set and resource 
 		ResourceSet resourceSet = new ResourceSetImpl();
 
 		// Register XML resource factory
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml",  new XMLResourceFactoryImpl());
 
 		File dataSetFile = new File(dataDirectory + fileSeparator + dataSetFilename);
 		
 		if(dataSetFile.exists())
 		{
 			// delete existing resource file
 			dataSetFile.delete();
 		}
 		
 		URI fileUri = URI.createFileURI(dataSetFile.getAbsolutePath());
 		dataSetResource = resourceSet.createResource(fileUri);
 
 		// register package in local resource registry
 		String nsURI = DataPackage.eINSTANCE.getNsURI();
 		resourceSet.getPackageRegistry().put(nsURI, DataPackage.eINSTANCE);
 
 		// add data set to resource
 		dataSetResource.getContents().add(mashup.getDataSet());
 	
 	}
 
 
 	/**
 	 * Loads and returns
 	 * 
 	 * @return The loaded data set
 	 */
 	private DataSet loadExistingDataSet() {
 		// create resource set and resource 
 		ResourceSet resourceSet = new ResourceSetImpl();
 
 		// Register XML resource factory
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml",  new XMLResourceFactoryImpl());
 
 		File dataSetFile = new File(dataDirectory + fileSeparator + dataSetFilename);
 		
 		if(!dataSetFile.exists())
 		{
 			// could not load
 			return null;
 		}
 		
 		URI fileUri = URI.createFileURI(dataSetFile.getAbsolutePath());
 		dataSetResource = resourceSet.createResource(fileUri);
 
 		// register package in local resource registry
 		String nsURI = DataPackage.eINSTANCE.getNsURI();
 		resourceSet.getPackageRegistry().put(nsURI, DataPackage.eINSTANCE);
 
 		// load resource 
 		try 
 		{
 			dataSetResource.load(null);
 		} 
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 
 		TreeIterator<EObject> dataIterator = dataSetResource.getAllContents();
 		
 		// Mashup Data XML contains exactly one DataSet
 		if(dataIterator.hasNext())
 		{
 			EObject workingObject = dataIterator.next();
 
 			if(workingObject instanceof DataSet)
 			{
 				return (DataSet) workingObject;
 			}
 		}
 		
 		return null;
 	}
 
 
 	/**
 	 * Creates a source service if the factory is already available. Otherwise keeps reference to create it when the source
 	 * factory appears.
 	 * 
 	 * @param sourceConfiguration Configuration of the source service to create.
 	 */
 	private void createSourceService(Source sourceConfiguration) {
 		if(sourceConfiguration.getState() != SourceState.ACTIVE)
 		{
 			// don't produce inactive sources
 			return;
 		}
 				
 		// get bundle id from configuration
 		String bundleId = sourceConfiguration.getBundleId();
 
		if(sourceFactory != null && mashup.getState() == SourceState.ACTIVE)
 		{
			// directly produce if source factory available and mashup active
 
 			// note it as open asynchronous instantiation
 			asynchronousInstantionStarted.add(sourceConfiguration);
 
 			// state should be stoped before production
 			sourceConfiguration.setState(SourceState.STOPED);
 						
 			// remove it if it is already waiting
 			if(sourcesWaitingForInstantiation.contains(sourceConfiguration))
 			{
 				sourcesWaitingForInstantiation.remove(sourceConfiguration);
 			}
 			
 			// instantiate service asynchronously and set source configuration as key
 			log("Starting asynchronous production of source " + sourceConfiguration.getName(), LogService.LOG_DEBUG);
 			sourceFactory.produceAsynchronous(bundleId, this, sourceConfiguration);
 		}
 		else
 		{
 			log("Keeping source " + sourceConfiguration.getName() + " for later production.", LogService.LOG_DEBUG);
 
 			// keep it for later creation
 			if(!sourcesWaitingForInstantiation.contains(sourceConfiguration))
 			{
 				sourcesWaitingForInstantiation.add(sourceConfiguration);
 			}
 		}
 	}
 
 	/**
 	 * Creates a interface service if the factory is already available. Otherwise keeps reference to create it when the interface
 	 * factory appears.
 	 * 
 	 * @param interfaceConfiguration Configuration of the interface service to create.
 	 */
 	private void createInterfaceService(Interface interfaceConfiguration) {
 		// only produce when interface factory available and mashup active
 		if(interfaceFactory != null && mashup.getState() == SourceState.ACTIVE)
 		{
 			log("Starting production of interface", LogService.LOG_DEBUG);
 
 			// directly produce if interface factory available
 
 			InterfaceServiceFacade interfaceService = null;
 			// instantiate interface service
 			try {
 				interfaceService = interfaceFactory.produceInterface(interfaceConfiguration, this.getDataSet());
 			} catch (Exception e) {
 				log("Error (" + e.getMessage() + ") while producing interface " + interfaceConfiguration.getName(), LogService.LOG_ERROR);
 				return;
 			}
 			// keep reference
 			interfaceServices.put(interfaceConfiguration, interfaceService);
 		}
 		else
 		{
 			log("Keeping interface for later production.", LogService.LOG_DEBUG);
 
 			// keep it for later creation
 			interfacesWaitingForInstantiation.add(interfaceConfiguration);
 		}
 	}
 	/**
 	 * Sets the source factory used for the creation of source services.
 	 * 
 	 * @param sourceFactoryService Source factory service
 	 */
 	public void setSourceFactory(SourceFactoryServiceFacade sourceFactoryService) {
 		// unset it first
 		this.unsetSourceFactory();
 
 		// keep reference
 		this.sourceFactory = sourceFactoryService;
 
 		// check if sources waiting for instantiation
 		if(sourcesWaitingForInstantiation.isEmpty())
 		{
 			// return if no sources in the list
 			return;
 		}
 
 		// temporary keep all waiting sources
 		List<Source> tmpList = new LinkedList<Source>(sourcesWaitingForInstantiation);
 
 		// clear the list
 		sourcesWaitingForInstantiation.clear();
 
 		for(Source waiting : tmpList)
 		{
 			// maybe puts it on the waiting list again
 			createSourceService(waiting);
 		}
 	}
 
 	/**
 	 * Removes the kept reference to a source factory service.
 	 */
 	public void unsetSourceFactory() {
 		// remove reference to source factory
 		sourceFactory = null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.factory.facade.callback.AsynchronousSourceInstantiationCallback#sourceInstantiated(org.sociotech.communitymashup.source.facade.SourceServiceFacade, java.lang.Object)
 	 */
 	@Override
 	public void sourceInstantiated(SourceServiceFacade sourceService, Object key) {
 		// key object should be a source configuration
 		// otherwise something went wrong.
 		if(!(key instanceof Source))
 		{
 			return;
 		}			
 				
 		// cast it
 		Source configuration = (Source) key;
 
 		if(sourceService == null || key == null)
 		{
 			// invalid callback
 			log("Invalid callback from source instantiation.", LogService.LOG_ERROR);
 			return;
 		}
 
 		if(asynchronousInstantionStarted.contains(configuration))
 		{
 			log("Production of source " + configuration.getName() + " finished.");
 
 			// TODO maintain a global(mashup service) initialization state
 			// now initialize the source service with the configuration
 			boolean initialized = sourceService.initialize(configuration);
 
 			if(initialized)
 			{
 				// set source state to active
 				configuration.setState(SourceState.ACTIVE);
 			}
 			else
 			{
 				//something happened -> set error state
 				configuration.setState(SourceState.ERROR);
 			}
 			
 			// remove it from asynchronous instantiation list
 			asynchronousInstantionStarted.remove(configuration);
 			// and add it to instantiated services
 			sourceServices.put(configuration, sourceService);
 						
 			// now let the source add data
 			// TODO maybe do it after all sources are created 
 			try {
 				sourceService.fill(this.getDataSet());
 				log("Filled data set by " + configuration.getName(), LogService.LOG_INFO);
 			} catch (Exception e) {
 				log("Exception (" + e.getMessage() + ") while filling dataset by source " + sourceService, LogService.LOG_ERROR);
 			}
 			
 			// start update loop after first source is instantiated and filled
 			this.startUpdateLoop();
 		}
 		else
 		{
 			// error, should never happen
 			log("Got callback from source instantiation that was not previously started: " + configuration, LogService.LOG_ERROR);
 			return;
 		}
 
 		// check if mashup configuration is now fulfilled
 		checkConfiguration();
 	}
 
 	/**
 	 * Checks if the complete configuration given to {@link #loadConfiguration(Mashup)} is fulfilled. This
 	 * means that all sources and interfaces are instantiated.
 	 */
 	private void checkConfiguration() {
 		configurationFullfilled = checkInterfaceInstantiation() && checkSourceInstantiation();
 		
 		if(!configurationFullfilled || startedEnrichment)
 		{
 			// nothing to do if not fulfilled or enrichment already started
 			return;
 		}
 		
 		// enrich data set by all source services
 		this.enrich();
 		
 		// TODO What else should happen if the configuration is now fulfilled
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#enrich()
 	 */
 	@Override
 	public void enrich() {
 		// TODO Maintain source active state also for mashup
 		//super.enrich();
 		
 		// get all created source services
 		Collection<SourceServiceFacade> services = sourceServices.values();
 		
 		if(services == null)
 		{
 			log("No valid services created by mashup configuration, could not enrich", LogService.LOG_ERROR);
 			return;
 		}
 				
 		// let all services enrich the data set
 		// do it in the order specified in the configuration
 		for(Source source : mashup.getSources())
 		{
 			SourceServiceFacade service = sourceServices.get(source);
 			if(service == null)
 			{
 				log("Service not found for source " + source, LogService.LOG_WARNING);
 				continue;
 			}
 			
 			enrichSource(source);
 		}
 	}
 
 
 	/**
 	 * Starts a thread that cyclic call the update method of this mashup service
 	 */
 	private void startUpdateLoop() {
 		// refresh interval causes the creation of a new thread
 		refreshUpdateInterval();
 	}
 
 
 	/**
 	 * Gets the update interval from the configuration and sets it at the update thread
 	 */
 	private void refreshUpdateInterval() {
 		
 		if(updateThread != null)
 		{
 			// stop existing thread
 			updateThread.interrupt();
 			updateThread = null;
 		}
 		
 		// create new thread
 		updateThread = new UpdateThread(this);
 		
 		// get update interval
 		String updateIntervalString = mashup.getPropertyValueElseDefault(MashupProperties.UPDATE_CYCLE_TIME_PROPERTY, MashupProperties.UPDATE_CYCLE_TIME_PROPERTY_DEFAULT);
 		
 		int updateInterval = 0;
 		try {
 			updateInterval = new Integer(updateIntervalString);
 			// transform s to ms
 			updateInterval *= 1000;
 			
 			// set time interval in update thread
 			updateThread.setUpdateInterval(updateInterval);
 		} catch (Exception e) {
 			log("Error while refreshing update intervall (" + e.getMessage() + ")", LogService.LOG_WARNING);
 			return;
 		}
 		
 		// start thread
 		updateThread.start();
 	}
 
 
 	/**
 	 * Checks if all sources are instantiated.
 	 * 
 	 * @return True if all sources are instantiated, false otherwise.
 	 */
 	private boolean checkSourceInstantiation() {
 		return asynchronousInstantionStarted.isEmpty() && sourcesWaitingForInstantiation.isEmpty();
 	}
 
 	/**
 	 * Checks if all interfaces are instantiated.
 	 * 
 	 * @return True if all interfaces are instantiated, false otherwise.
 	 */
 	private boolean checkInterfaceInstantiation() {
 		// TODO implement
 		return true;
 	}
 
 	/**
 	 * Stops this mashup service.
 	 */
 	private void stopMashup()
 	{
 		// stop all instantiated interfaces
 		List<Interface> createdInterfaces = new LinkedList<Interface>(interfaceServices.keySet());
 		for(Interface interfaceConfig : createdInterfaces)
 		{
 			destroyInterface(interfaceConfig);
 		}
 		
 		// stop all instantiated sources
 		LinkedList<Source> createdSources = new LinkedList<Source>(sourceServices.keySet());
 		for(Source sourceConfig : createdSources)
 		{
 			destroySource(sourceConfig);
 		}
 		
 		// cache data if needed
 		cacheDataSet();
 
 		// disconnect observers
 		if(mashupConfigurationObserver != null)
 		{
 			mashupConfigurationObserver.disconnect();
 			mashupConfigurationObserver = null;
 		}
 		if(dataSetChangeObserver != null)
 		{
 			dataSetChangeObserver.disconnect();
 			dataSetChangeObserver = null;
 		}
 			
 		// stop update thread
 		if(updateThread != null)
 		{
 			updateThread.interrupt();
 			updateThread = null;
 		}
 		
 		// stop cache thread
 		if(cacheThread != null)
 		{
 			cacheThread.interrupt();
 			cacheThread = null;
 		}
 		
 		// stop backup thread
 		if(backupThread != null)
 		{
 			backupThread.interrupt();
 			backupThread = null;
 		}
 		
 		// close service trackers
 		if(sourceFactoryTracker != null)
 		{
 			sourceFactoryTracker.close();
 		}
 
 		if(interfaceFactoryTracker != null)
 		{
 			interfaceFactoryTracker.close();
 		}
 		
 		// set state as stoped
 		mashup.setState(SourceState.STOPED);
 	}
 
 
 	/**
 	 * Sets the interface factory used for the creation of interface services.
 	 * 
 	 * @param interfaceFactoryService Interface factory service
 	 */
 	public void setInterfaceFactory(InterfaceFactoryFacade interfaceFactoryService)
 	{
 		// unset it first
 		this.unsetInterfaceFactory();
 
 		// keep reference
 		this.interfaceFactory = interfaceFactoryService;
 
 		// check if sources waiting for instantiation
 		if(interfacesWaitingForInstantiation.isEmpty())
 		{
 			// return if no sources in the list
 			return;
 		}
 
 		// temporary keep all waiting sources
 		List<Interface> tmpList = new LinkedList<Interface>(interfacesWaitingForInstantiation);
 
 		// clear the list
 		interfacesWaitingForInstantiation.clear();
 
 		for(Interface waiting : tmpList)
 		{
 			// maybe puts it on the waiting list again
 			createInterfaceService(waiting);
 		}
 	}
 
 	/**
 	 * Removes the kept reference to a interface factory service.
 	 */
 	public void unsetInterfaceFactory()
 	{
 		this.interfaceFactory = null;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#update()
 	 */
 	@Override
 	public void update() {
 		// TODO: Maintain source active state also for mashup
 		//super.update();
 		
 		// only update in active state
 		if(mashup.getState() != SourceState.ACTIVE)
 		{
 			return;
 		}
 		
 		// set active state to updating
 		mashup.setActiveState(SourceActiveStates.UPDATING);
 				
 		// update all source services
 		for(Source source : sourceServices.keySet())
 		{
 			SourceServiceFacade service = sourceServices.get(source);
 			
 			log("Updating data with source service " + source.getName(), LogService.LOG_INFO);
 			try {
 				service.update();
 			} catch (Exception e) {
 				log("Exception (" + e.getMessage() + ") while updating data with source service " + service, LogService.LOG_ERROR);
 				// set source to error state
 				source.setState(SourceState.ERROR);
 			}
 		}		
 		
 		// indicate finished update round
 		mashup.setActiveState(SourceActiveStates.WAITING_FOR_UPDATE);
 	}
 	
 	/**
 	 * Checks and prepares the structure of the working directory.
 	 * 
 	 * @param workingDirectoryPath Configured path of the working directory
 	 * @return True if the working directory structure is valid, false otherwise.
 	 */
 	private boolean prepareWorkingDirectory(String workingDirectoryPath) {
 		
 		if(workingDirectoryPath == null)
 		{
 			// not set, so nothing to do
 			return false;
 		}
 		
 		String directoryPath = workingDirectoryPath;
 		
 		// ensure directory path ends with file separator
 		if(!directoryPath.endsWith(fileSeparator))
 		{
 			directoryPath = directoryPath + fileSeparator;
 		}
 		
 		// prepare base working directory
 		workingDirectory = prepareDirectory(directoryPath);
 		
 		if(workingDirectory == null)
 		{
 			return false;
 		}
 		
 		// prepare sub directories
 		
 		// prepare data directory
 		String dataDirectoryPath = directoryPath + DEFAULT_DATA_FOLDER;
 		dataDirectory = prepareDirectory(dataDirectoryPath);
 		
 		if(dataDirectory == null)
 		{
 			return false;
 		}
 		
 		// prepare data backup directory
 		String dataBackupDirectoryPath = directoryPath + DEFAULT_DATA_BACKUP_FOLDER;
 		dataBackupDirectory = prepareDirectory(dataBackupDirectoryPath);
 		
 		if(dataBackupDirectory == null)
 		{
 			return false;
 		}
 		
 		// prepare attachment cache directory
 		String attachmentDirectoryPath = directoryPath + DEFAULT_ATTACHMENT_FOLDER;
 		attachmentsCacheDirectory = prepareDirectory(attachmentDirectoryPath);
 		
 		if(attachmentsCacheDirectory == null)
 		{
 			return false;
 		}
 		
 		return true;
 		
 	}
 
 	/**
 	 * Checks if the directory with the given path already exists, otherwise creates it.
 	 * 
 	 * @param directoryPath Path of the directory to be prepared
 	 * @return The directory or null if the directory does not exist and can not be created.
 	 */
 	private File prepareDirectory(String directoryPath) {
 		
 		File directory = new File(directoryPath);
 	
 		if(directory.exists())
 		{
 			// nothing to be done if working directory already exists
 			return directory;
 		}
 		else
 		{
 			try {
 				// try to create directory
 				boolean directoryPrepared = directory.mkdirs();
 				if(!directoryPrepared)
 				{
 					log("Directory " + directoryPath + " could not be created.", LogService.LOG_ERROR);
 					return null;
 				}
 				else
 				{
 					log("Created directory " + directoryPath, LogService.LOG_INFO);
 				}
 			} catch (Exception e) {
 				log("Directory " + directoryPath + " could not be created due to exception (" + e.getMessage() + ")", LogService.LOG_ERROR);
 				return null;
 			}
 		}
 		return directory;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.mashup.facade.MashupServiceFacade#stopMashupService()
 	 */
 	@Override
 	public void stopMashupService() {
 		this.stopMashup();
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.configuration.observer.mashup.MashupChangedInterface#mashupConfigurationChanged(org.eclipse.emf.common.notify.Notification)
 	 */
 	@Override
 	public void mashupConfigurationChanged(Notification notification) {
 		
 		// TODO refactor long method
 		
 		Object notifier = notification.getNotifier();
 		
 		// check notifier
 		// handle mashup changes
 		if(notifier instanceof Mashup && notifier == this.mashup)
 		{
 			log("Got change notification at mashup " + mashup.getName(), LogService.LOG_DEBUG);
 			
 			// possible change to mashup attributes -> interpret it
 			interpretMashupConfigurationAttributes();
 			
 			// handle new or deleted source
 			if(notification.getEventType() == Notification.ADD && 
 			   notification.getFeatureID(Mashup.class) == ApplicationPackage.MASHUP__SOURCES &&
 			   notification.getNewValue() instanceof Source)
 				
 			{
 				// new source added
 				log("Source added.", LogService.LOG_DEBUG);
 				
 				// produce the new source
 				createSourceService((Source) notification.getNewValue());
 			}
 			else if(notification.getEventType() == Notification.REMOVE && 
 					notification.getFeatureID(Mashup.class) == ApplicationPackage.MASHUP__SOURCES &&
 					notification.getOldValue() instanceof Source)
 			{
 				// source deleted
 				log("Source removed.", LogService.LOG_DEBUG);
 				
 				// destroy source
 				destroySource((Source) notification.getOldValue());
 			}
 			
 			// handle new or deleted interfaces
 			if(notification.getEventType() == Notification.ADD && 
 			   notification.getFeatureID(Mashup.class) == ApplicationPackage.MASHUP__INTERFACES &&
 			   notification.getNewValue() instanceof Interface)
 				
 			{
 				// new interface added
 				log("Interface added.", LogService.LOG_DEBUG);
 				
 				// produce the new interface
 				createInterfaceService((Interface) notification.getNewValue());
 			}
 			else if(notification.getEventType() == Notification.REMOVE && 
 					notification.getFeatureID(Mashup.class) == ApplicationPackage.MASHUP__INTERFACES &&
 					notification.getOldValue() instanceof Interface)
 			{
 				// interface deleted
 				log("Interface removed.", LogService.LOG_DEBUG);
 				
 				// destroy interface
 				destroyInterface((Interface) notification.getOldValue());
 			}
 			else if(notification.getEventType() == Notification.SET && 
 					notification.getFeatureID(Mashup.class) == ApplicationPackage.MASHUP__STATE &&
 					notification.getOldValue() != SourceState.ACTIVE &&
 					notification.getNewValue() == SourceState.ACTIVE)
 			{
 				// interface deleted
 				log("Activating mashup.", LogService.LOG_DEBUG);
 				
 				// load configuration if not yet done
 				if(!configurationLoaded)
 				{
 					this.loadConfiguration(mashup);
 				}
 				
 				// if previously in pause from the beginning we maybe need to start the update loop
 				startUpdateLoop();
 			}
 		}
 		// handle interface changes that can not be handled by the interfaces themselves
 		else if(notifier instanceof Interface && notification.getEventType() == Notification.SET)
 		{
 			Interface changedInterface = (Interface) notifier;
 			
 			// interface changed
 			log("Interface changed.", LogService.LOG_DEBUG);
 			
 			// get interface service
 			InterfaceServiceFacade interfaceService = interfaceServices.get(changedInterface);
 			
 			if(interfaceService != null && interfaceService.handleChange(notification))
 			{
 				// interface handles change
 				return;
 			}
 			
 			// can not be handled, so
 			// destroy
 			destroyInterface(changedInterface);
 			// and recreate
 			createInterfaceService(changedInterface);
 		}
 		// handle source changes that can not be handled by the source services themselves
 		else if(notifier instanceof Source && notification.getEventType() == Notification.SET)
 		{
 			Source changedSource = (Source) notifier;
 			if(asynchronousInstantionStarted.contains(changedSource))
 			{
 				log("ignoring notification!", LogService.LOG_DEBUG);
 				
 				// currently instantiating this source, so do not handle changes
 				return;
 			}
 			
 			// source changed
 			log("Source changed.", LogService.LOG_DEBUG);
 			
 			// get source service
 			SourceServiceFacade sourceService = sourceServices.get(changedSource);
 			
 			// handle stop of source
 			if(notification.getFeatureID(Source.class) == ApplicationPackage.SOURCE__STATE && 
 			   notification.getNewValue() == SourceState.STOPED)
 			{
 				// destroy source service
 				destroySource(changedSource);
 				return;
 			}
 			// handle resume of source service
 			if(notification.getFeatureID(Source.class) == ApplicationPackage.SOURCE__STATE && 
 			   notification.getNewValue() == SourceState.ACTIVE &&
 			   notification.getOldValue() == SourceState.PAUSED)
 			{
 				// let source enrich after pausing
 				enrichSource(changedSource);
 				return;
 			}
 				
 			if(sourceService != null && sourceService.handleChange(notification))
 			{
 				// interface handles change
 				return;
 			}
 			
 			// can not be handled, so
 			// destroy
 			destroySource(changedSource);
 			// and recreate
 			createSourceService(changedSource);
 		}
 		// handle property changes
 		else if(notifier instanceof Configuration && notification.getEventType() == Notification.ADD)
 		{
 			// new property
 			log("Property added.", LogService.LOG_DEBUG);
 		}
 		else if(notifier instanceof Configuration && notification.getEventType() == Notification.REMOVE)
 		{
 			log("Property removed.", LogService.LOG_DEBUG);
 		}
 		else if(notifier instanceof Property && notification.getEventType() == Notification.SET)
 		{
 			if(notification.getFeatureID(Property.class) == ApplicationPackage.PROPERTY__CHANGEABLE ||
 				notification.getFeatureID(Property.class) == ApplicationPackage.PROPERTY__HIDDEN ||
 				notification.getFeatureID(Property.class) == ApplicationPackage.PROPERTY__REQUIRED ||
 				notification.getFeatureID(Property.class) == ApplicationPackage.PROPERTY__HELP_TEXT ||
 				notification.getFeatureID(Property.class) == ApplicationPackage.PROPERTY__POSSIBLE_VALUES ||
 				notification.getFeatureID(Property.class) == ApplicationPackage.PROPERTY__PROPERTY_TYPE)
 			{
 				// ignore basic ui related properties
 				return;
 			}
 			
 			Property changedProperty = (Property) notifier;
 			if(!(changedProperty.eContainer() instanceof Configuration))
 			{
 				// no configuration => nothing to do
 				return;
 			}
 			
 			Configuration changedConfiguration = (Configuration) changedProperty.eContainer();
 			if(changedConfiguration.eContainer() instanceof Source)
 			{
 				// source property changed
 				Source changedSource = (Source) changedConfiguration.eContainer();
 				
 				// change to this mashup (which is also a source)
 				if(changedSource == mashup)
 				{
 					// handle internally
 					this.handleProperty(notification);
 					return;
 				}
 				
 				SourceServiceFacade sourceService = sourceServices.get(changedSource);
 				// let source service handle change
 				if(sourceService != null && sourceService.handlePropertyChange(notification))
 				{
 					return;
 				}
 				
 				// property change can not be handled
 				// so destroy source
 				destroySource(changedSource);
 				
 				// and recreate it
 				createSourceService(changedSource);
 			}
 		}	
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#handleProperty(org.eclipse.emf.common.notify.Notification)
 	 */
 	@Override
 	protected boolean handleProperty(Notification notification) {
 		
 		// no super call
 		
 		try {
 			if(notification == null || !(notification.getNotifier() instanceof Property) || ((Property)notification.getNotifier()).eContainer().eContainer() != mashup)
 			{
 				return false;
 			}
 		}
 		catch(Exception e){
 			// not complete configuration hierarchy defined 
 			return false;
 		}
 		
 		if(notification.getFeatureID(Property.class) == ApplicationPackage.PROPERTY__KEY)
 		{
 			// key changes can not be handled
 			return false;
 		}
 		
 		Property changedProperty = (Property) notification.getNotifier();
 		
 		if(changedProperty.getKey().equals(MashupProperties.UPDATE_CYCLE_TIME_PROPERTY))
 		{
 			// simply refresh update interval
 			refreshUpdateInterval();
 			return true;
 		}
 		return false;
 	}
 
 
 	/**
 	 * Enriches the data with the service belonging to the given configuration.
 	 * 
 	 * @param sourceConfiguration Configuration identifying a source service.
 	 */
 	private void enrichSource(Source sourceConfiguration) {
 		if(sourceConfiguration == null)
 		{
 			return;
 		}
 		
 		// get the matching source service
 		SourceServiceFacade sourceService = sourceServices.get(sourceConfiguration);
 		
 		if(sourceService != null)
 		
 		log("Enriching data with source service " + sourceService.getConfiguration().getName(), LogService.LOG_INFO);
 		try {
 			sourceService.enrich();
 		} catch (Exception e) {
 			log("Exception while enriching data with source service " + sourceService, LogService.LOG_ERROR);
 		}
 	}
 
 
 	/**
 	 * Stops the interface service belonging to the given configuration and removes local references.
 	 * 
 	 * @param interfaceConfiguration Configuration to destroy the service for.
 	 */
 	private void destroyInterface(Interface interfaceConfiguration) {
 		if(interfaceConfiguration == null)
 		{
 			return;
 		}
 		
 		// get the matching interface service
 		InterfaceServiceFacade interfaceService = interfaceServices.get(interfaceConfiguration);
 		
 		if(interfaceService != null)
 		{
 			// remove it from the interface services list
 			interfaceServices.remove(interfaceConfiguration);
 			
 			interfaceService.stopInterfaceService();
 		}
 		else if(interfacesWaitingForInstantiation.contains(interfaceConfiguration))
 		{
 			// not yet produce so delet it form the waiting list
 			interfacesWaitingForInstantiation.remove(interfaceConfiguration);
 		}
 		
 	}
 
 
 	/**
 	 * Stops the source service belonging to the configuration and removes local references.
 	 * 
 	 * @param sourceConfiguration The source configuration to destroy the service for.
 	 */
 	private void destroySource(Source sourceConfiguration) {
 		if(sourceConfiguration == null)
 		{
 			return;
 		}
 		
 		// get the matching source service
 		SourceServiceFacade sourceService = sourceServices.get(sourceConfiguration);
 		
 		if(sourceService != null)
 		{
 			// remove it from the source services list
 			sourceServices.remove(sourceConfiguration);
 			
 			// keep old source state
 			SourceState oldSourceState = sourceConfiguration.getState();
 			
 			sourceService.stopSourceService();
 			
 			// state should only change if mashup is active
 			if(mashup.getState() != SourceState.ACTIVE)
 			{
 				// so reset it
 				sourceConfiguration.setState(oldSourceState);
 			}
 		}
 		else if(sourcesWaitingForInstantiation.contains(sourceConfiguration))
 		{
 			// not yet produced so delete it from the waiting list
 			sourcesWaitingForInstantiation.remove(sourceConfiguration);
 		}
 		
 	}
 
 
 	/**
 	 * Backups the data set if there are pending changes.
 	 */
 	public synchronized void backupDataSet() {
 		if(!this.needBackup || !this.canSave || !this.backupDataSet)
 		{
 			// nothing changed
 			return;
 		}
 		
 		log("Creating backup of data set", LogService.LOG_DEBUG);
 		
 		Date now = new Date();
 		SimpleDateFormat suffix = new SimpleDateFormat("_yyyyMMddHHmmssZ");
 	
 		// create file name with date as suffix
 		String backupFileName = dataSetFilename.replace(".xml", suffix.format(now) + ".xml");
 		
 		// save backup
 		saveDataSet(dataBackupDirectory + fileSeparator + backupFileName);
 		
 		// backuped, so set need backup to false
 		this.needBackup = false;
 	}
 	
 	/**
 	 * Saves the data set if there are pending changes.
 	 */
 	public synchronized void cacheDataSet() {
 		if(!this.needSave || !this.canSave || !this.cacheDataSet)
 		{
 			// nothing changed
 			return;
 		}
 		
 		log("Caching data set", LogService.LOG_DEBUG);
 		
 		// save data set
 		saveDataSet(null);
 		
 		// saved, so set need save to false
 		this.needSave = false;
 	}
 	
 	/**
 	 * Saves the configuration at the given path. If path is null the original loaded resource
 	 * will be saved.
 	 * 
 	 * @param dataSetPath Path where the configuration will be saved
 	 */
 	private void saveDataSet(String dataSetPath) {
 		String savePath = dataSetPath;
 		
 		if(savePath == null && dataSetResource != null)
 		{
 			log("Saving data set.", LogService.LOG_DEBUG);
 			if(!dataSetResource.getContents().contains(mashup.getDataSet()))
 			{
 				// clear list
 				dataSetResource.getContents().clear();
 				// add the data set
 				dataSetResource.getContents().add(mashup.getDataSet());
 			}
 			try {
 				dataSetResource.save(null);
 			} catch (IOException e) {
 				log("Could not save data set due to exception (" + e.getMessage() + ")", LogService.LOG_WARNING);
 				return;
 			}
 		}
 		else
 		{
 			// create resource set and resource 
 	        ResourceSet resourceSet = new ResourceSetImpl();
 
 	        // Register XML resource factory
 	        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml",  new XMLResourceFactoryImpl());
 
 	        // configuration directory set, so create URI for external configuration file
  			File configurationFile = new File(dataSetPath);
  			
  			if(configurationFile.exists())
  			{
  				log("Data set backup file already exist, could not backup.", LogService.LOG_WARNING);
  				return;
  			}
  			
  			URI backupFileURI;
  			
  			try
  			{
  				backupFileURI = URI.createFileURI(configurationFile.getAbsolutePath());
  			}
  			catch (Exception e) {
  				log("Could not create uri for data set backup file " + configurationFile.getAbsolutePath(), LogService.LOG_ERROR);
  				return;
  			}
 	        Resource saveResource = resourceSet.createResource(backupFileURI);
 			
 			// register package in local resource registry
 	        String nsURI = ApplicationPackage.eINSTANCE.getNsURI();
 			resourceSet.getPackageRegistry().put(nsURI, ApplicationPackage.eINSTANCE);
 			
 			// add data set copy to resource
 			saveResource.getContents().add(EcoreUtil.copy(mashup.getDataSet()));
 			
 			// save resource
 			try {
 				saveResource.save(null);
 			} catch (IOException e) {
 				log("Could not save data set to " + dataSetPath + " due to exception (" + e.getMessage() + ")", LogService.LOG_WARNING);
 				return;
 			}
 			
 			log("Saved data set to " + dataSetPath, LogService.LOG_DEBUG);
 		}
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.data.observer.dataset.DataSetChangedInterface#dataSetChanged(org.eclipse.emf.common.notify.Notification)
 	 */
 	@Override
 	public void dataSetChanged(Notification notification) {
 		// set attributes to indicate change
 		this.needBackup = true;
 		this.needSave = true;
 	}
 }
