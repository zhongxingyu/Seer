 package org.lightmare.deploy;
 
 import static org.lightmare.cache.MetaContainer.closeConnections;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import javax.ejb.Local;
 import javax.ejb.Remote;
 import javax.ejb.Stateless;
 import javax.persistence.Entity;
 
 import org.apache.log4j.Logger;
 import org.lightmare.annotations.UnitName;
 import org.lightmare.cache.ArchiveData;
 import org.lightmare.cache.DeployData;
 import org.lightmare.cache.DeploymentDirectory;
 import org.lightmare.cache.MetaContainer;
 import org.lightmare.cache.TmpResources;
 import org.lightmare.config.Configuration;
 import org.lightmare.deploy.fs.Watcher;
 import org.lightmare.jpa.JPAManager;
 import org.lightmare.jpa.datasource.DataSourceInitializer;
 import org.lightmare.jpa.datasource.PoolConfig;
 import org.lightmare.jpa.datasource.PoolConfig.PoolProviderType;
 import org.lightmare.libraries.LibraryLoader;
 import org.lightmare.remote.rpc.RPCall;
 import org.lightmare.remote.rpc.RpcListener;
 import org.lightmare.rest.utils.RestUtils;
 import org.lightmare.scannotation.AnnotationDB;
 import org.lightmare.utils.AbstractIOUtils;
 import org.lightmare.utils.ObjectUtils;
 import org.lightmare.utils.fs.FileUtils;
 import org.lightmare.utils.fs.WatchUtils;
 import org.lightmare.utils.reflect.MetaUtils;
 import org.lightmare.utils.shutdown.ShutDown;
 
 /**
  * Determines and saves in cache ejb beans {@link org.lightmare.cache.MetaData}
  * on startup
  * 
  * @author Levan
  * 
  */
 public class MetaCreator {
 
     private static AnnotationDB annotationDB;
 
     private TmpResources tmpResources;
 
     private boolean await;
 
     // Blocker for deployments
     private CountDownLatch conn;
 
     // Data for cache at deploy time
     private Map<String, AbstractIOUtils> aggregateds = new HashMap<String, AbstractIOUtils>();
 
     private Map<URL, ArchiveData> archivesURLs;
 
     private Map<String, URL> classOwnersURL;
 
     private Map<URL, DeployData> realURL;
 
     private ClassLoader current;
 
     private Configuration config;
 
     private static final Logger LOG = Logger.getLogger(MetaCreator.class);
 
     private MetaCreator() {
 	tmpResources = new TmpResources();
 	ShutDown.setHook(tmpResources);
     }
 
     private static MetaCreator get() {
 
 	MetaCreator creator = MetaContainer.getCreator();
 	if (creator == null) {
 	    synchronized (MetaCreator.class) {
 		if (creator == null) {
 		    creator = new MetaCreator();
 		    MetaContainer.setCreator(creator);
 		}
 	    }
 	}
 
 	return creator;
     }
 
     public AnnotationDB getAnnotationDB() {
 	return annotationDB;
     }
 
     /**
      * Checks weather {@link javax.persistence.Entity} annotated classes is need
      * to be filtered by {@link org.lightmare.annotations.UnitName} value
      * 
      * @param className
      * @return boolean
      * @throws IOException
      */
     private boolean checkForUnitName(String className, Configuration cloneConfig)
 	    throws IOException {
 	boolean isValid = Boolean.FALSE;
 	Class<?> entityClass;
 	entityClass = MetaUtils.initClassForName(className);
 	UnitName annotation = entityClass.getAnnotation(UnitName.class);
 	isValid = annotation.value().equals(cloneConfig.getAnnotatedUnitName());
 
 	return isValid;
     }
 
     private List<String> translateToList(Set<String> classSet) {
 
 	String[] classArray = new String[classSet.size()];
 	classArray = ObjectUtils.toArray(classSet, String.class);
 	List<String> classList = Arrays.asList(classArray);
 
 	return classList;
     }
 
     /**
      * Defines belonginess of {@link javax.persistence.Entity} annotated classes
      * to jar file
      * 
      * @param classSet
      * @return {@link List}<String>
      */
     private void filterEntitiesForJar(Set<String> classSet,
 	    String fileNameForBean) {
 
 	Map<String, String> classOwnersFiles = annotationDB
 		.getClassOwnersFiles();
 
 	String fileNameForEntity;
 	for (String entityName : classSet) {
 	    fileNameForEntity = classOwnersFiles.get(entityName);
 	    if (ObjectUtils.notNull(fileNameForEntity)
 		    && ObjectUtils.notNull(fileNameForBean)
 		    && !fileNameForEntity.equals(fileNameForBean)) {
 		classSet.remove(entityName);
 	    }
 	}
     }
 
     /**
      * Filters {@link javax.persistence.Entity} annotated classes by name or by
      * {@link org.lightmare.annotations.UnitName} by configuration
      * 
      * @param classSet
      * @return {@link List}<String>
      * @throws IOException
      */
     private List<String> filterEntities(Set<String> classSet,
 	    Configuration cloneConfig) throws IOException {
 	List<String> classes;
 	if (config.getAnnotatedUnitName() == null) {
 	    classes = translateToList(classSet);
 	} else {
 	    Set<String> filtereds = new HashSet<String>();
 	    for (String className : classSet) {
 		if (checkForUnitName(className, cloneConfig)) {
 		    filtereds.add(className);
 		}
 	    }
 	    classes = translateToList(filtereds);
 	}
 
 	return classes;
     }
 
     /**
      * Creates connection associated with unit name if such does not exists
      * 
      * @param unitName
      * @param beanName
      * @throws IOException
      */
     protected void configureConnection(String unitName, String beanName,
 	    ClassLoader loader, Configuration cloneConfig) throws IOException {
 
 	JPAManager.Builder builder = new JPAManager.Builder();
 	Map<String, String> classOwnersFiles = annotationDB
 		.getClassOwnersFiles();
 	AbstractIOUtils ioUtils = aggregateds.get(beanName);
 
 	if (ObjectUtils.notNull(ioUtils)) {
 	    URL jarURL = ioUtils.getAppropriatedURL(classOwnersFiles, beanName);
 	    builder.setURL(jarURL);
 	}
 	if (cloneConfig.isScanForEntities()) {
 	    Set<String> classSet;
 	    Map<String, Set<String>> annotationIndex = annotationDB
 		    .getAnnotationIndex();
 	    classSet = annotationIndex.get(Entity.class.getName());
	    String annotatedUnitName = config.getAnnotatedUnitName();
 	    if (annotatedUnitName == null) {
 		classSet = annotationIndex.get(Entity.class.getName());
 	    } else if (annotatedUnitName.equals(unitName)) {
 		Set<String> unitNamedSet = annotationIndex.get(UnitName.class
 			.getName());
 		classSet.retainAll(unitNamedSet);
 	    }
 	    if (ObjectUtils.notNull(ioUtils)) {
 		String fileNameForBean = classOwnersFiles.get(beanName);
 		filterEntitiesForJar(classSet, fileNameForBean);
 	    }
 	    List<String> classes = filterEntities(classSet, cloneConfig);
 	    builder.setClasses(classes);
 	}
 	builder.setPath(cloneConfig.getPersXmlPath())
		.setProperties(config.getPersistenceProperties())
 		.setSwapDataSource(cloneConfig.isSwapDataSource())
 		.setScanArchives(cloneConfig.isScanArchives())
 		.setClassLoader(loader).build().setConnection(unitName);
     }
 
     /**
      * Caches each archive by it's {@link URL} for deployment
      * 
      * @param ejbURLs
      * @param archiveData
      */
     private void fillArchiveURLs(Collection<URL> ejbURLs,
 	    ArchiveData archiveData, DeployData deployData) {
 
 	for (URL ejbURL : ejbURLs) {
 	    archivesURLs.put(ejbURL, archiveData);
 	    realURL.put(ejbURL, deployData);
 	}
     }
 
     /**
      * Caches each archive by it's {@link URL} for deployment and creates fill
      * {@link URL} array for scanning and finding {@link javax.ejb.Stateless}
      * annotated classes
      * 
      * @param archive
      * @param modifiedArchives
      * @throws IOException
      */
     private void fillArchiveURLs(URL archive, List<URL> modifiedArchives)
 	    throws IOException {
 
 	AbstractIOUtils ioUtils = AbstractIOUtils.getAppropriatedType(archive);
 	if (ObjectUtils.notNull(ioUtils)) {
 	    ioUtils.scan(config.isPersXmlFromJar());
 	    List<URL> ejbURLs = ioUtils.getEjbURLs();
 	    modifiedArchives.addAll(ejbURLs);
 	    ArchiveData archiveData = new ArchiveData();
 	    archiveData.setIoUtils(ioUtils);
 	    DeployData deployData = new DeployData();
 	    deployData.setType(ioUtils.getType());
 	    deployData.setUrl(archive);
 	    if (ejbURLs.isEmpty()) {
 		archivesURLs.put(archive, archiveData);
 		realURL.put(archive, deployData);
 	    } else {
 		fillArchiveURLs(ejbURLs, archiveData, deployData);
 	    }
 	}
     }
 
     /**
      * Gets {@link URL} array for all classes and jar libraries within archive
      * file for class loading policy
      * 
      * @param archives
      * @return {@link URL}[]
      * @throws IOException
      */
     private URL[] getFullArchives(URL[] archives) throws IOException {
 
 	List<URL> modifiedArchives = new ArrayList<URL>();
 	for (URL archive : archives) {
 	    fillArchiveURLs(archive, modifiedArchives);
 	}
 
 	return ObjectUtils.toArray(modifiedArchives, URL.class);
     }
 
     /**
      * Awaits for {@link Future} tasks if it set so by configuration
      * 
      * @param future
      */
     private void awaitDeployment(Future<String> future) {
 
 	if (await) {
 	    try {
 		String nameFromFuture = future.get();
 		LOG.info(String.format("Deploy processing of %s finished",
 			nameFromFuture));
 	    } catch (InterruptedException ex) {
 		LOG.error(ex.getMessage(), ex);
 	    } catch (ExecutionException ex) {
 		LOG.error(ex.getMessage(), ex);
 	    }
 	}
     }
 
     /**
      * Awaits for {@link CountDownLatch} of deployments
      */
     private void awaitDeployments() {
 	try {
 	    conn.await();
 	} catch (InterruptedException ex) {
 	    LOG.error(ex);
 	}
     }
 
     /**
      * Starts bean deployment process for bean name
      * 
      * @param beanName
      * @throws IOException
      */
     private void deployBean(String beanName) throws IOException {
 	URL currentURL = classOwnersURL.get(beanName);
 	ArchiveData archiveData = archivesURLs.get(currentURL);
 	if (archiveData == null) {
 	    archiveData = new ArchiveData();
 	}
 	AbstractIOUtils ioUtils = archiveData.getIoUtils();
 	if (ioUtils == null) {
 	    ioUtils = AbstractIOUtils.getAppropriatedType(currentURL);
 	    archiveData.setIoUtils(ioUtils);
 	}
 	ClassLoader loader = archiveData.getLoader();
 
 	// Finds appropriated ClassLoader if needed and or creates new one
 	List<File> tmpFiles = null;
 
 	if (ObjectUtils.notNull(ioUtils)) {
 	    if (loader == null) {
 		if (!ioUtils.isExecuted()) {
 		    ioUtils.scan(config.isPersXmlFromJar());
 		}
 		URL[] libURLs = ioUtils.getURLs();
 		loader = LibraryLoader.initializeLoader(libURLs);
 		archiveData.setLoader(loader);
 	    }
 	    tmpFiles = ioUtils.getTmpFiles();
 	    aggregateds.put(beanName, ioUtils);
 	}
 
 	// Archive file url which contains this bean
 	DeployData deployData;
 	if (ObjectUtils.available(realURL)) {
 	    deployData = realURL.get(currentURL);
 	} else {
 	    deployData = null;
 	}
 	// Initializes and fills BeanLoader.BeanParameters class to deploy
 	// stateless ejb bean
 	BeanLoader.BeanParameters parameters = new BeanLoader.BeanParameters();
 	parameters.creator = this;
 	parameters.className = beanName;
 	parameters.loader = loader;
 	parameters.tmpFiles = tmpFiles;
 	parameters.conn = conn;
 	parameters.deployData = deployData;
 	parameters.config = config;
 
 	Future<String> future = BeanLoader.loadBean(parameters);
 	awaitDeployment(future);
 	if (ObjectUtils.available(tmpFiles)) {
 	    tmpResources.addFile(tmpFiles);
 	}
     }
 
     /**
      * Deploys single bean by class name
      * 
      * @param beanNames
      */
     private void deployBeans(Set<String> beanNames) {
 	conn = new CountDownLatch(beanNames.size());
 	for (String beanName : beanNames) {
 	    LOG.info(String.format("deploing bean %s", beanName));
 	    try {
 		deployBean(beanName);
 	    } catch (IOException ex) {
 		LOG.error(String.format("Could not deploy bean %s", beanName),
 			ex);
 	    }
 	}
 	awaitDeployments();
 	if (MetaContainer.hasRest()) {
 	    RestUtils.reload();
 	}
 	boolean hotDeployment = config.isHotDeployment();
 	boolean watchStatus = config.isWatchStatus();
 	if (hotDeployment && ObjectUtils.notTrue(watchStatus)) {
 	    Watcher.startWatch();
 	    watchStatus = Boolean.TRUE;
 	}
     }
 
     /**
      * Scan application for find all {@link javax.ejb.Stateless} beans and
      * {@link Remote} or {@link Local} proxy interfaces
      * 
      * @param archives
      * @throws IOException
      * @throws ClassNotFoundException
      */
     public void scanForBeans(URL[] archives) throws IOException {
 
 	synchronized (this) {
 	    if (config == null && ObjectUtils.available(archives)) {
 		config = MetaContainer.getConfig(archives);
 	    }
 	    try {
 		// starts RPC server if configured as remote and server
 		if (config.isRemote() && Configuration.isServer()) {
 		    RpcListener.startServer(config);
 		} else if (config.isRemote()) {
 		    RPCall.configure(config);
 		}
 		String[] libraryPaths = config.getLibraryPaths();
 		// Loads libraries from specified path
 		if (ObjectUtils.notNull(libraryPaths)) {
 		    LibraryLoader.loadLibraries(libraryPaths);
 		}
 		// Gets and caches class loader
 		current = LibraryLoader.getContextClassLoader();
 		archivesURLs = new HashMap<URL, ArchiveData>();
 		if (ObjectUtils.available(archives)) {
 		    realURL = new HashMap<URL, DeployData>();
 		}
 		URL[] fullArchives = getFullArchives(archives);
 		annotationDB = new AnnotationDB();
 		annotationDB.setScanFieldAnnotations(Boolean.FALSE);
 		annotationDB.setScanParameterAnnotations(Boolean.FALSE);
 		annotationDB.setScanMethodAnnotations(Boolean.FALSE);
 		annotationDB.scanArchives(fullArchives);
 		Set<String> beanNames = annotationDB.getAnnotationIndex().get(
 			Stateless.class.getName());
 		classOwnersURL = annotationDB.getClassOwnersURLs();
 		DataSourceInitializer.initializeDataSources(config);
 		if (ObjectUtils.available(beanNames)) {
 		    deployBeans(beanNames);
 		}
 	    } finally {
 
 		// Caches configuration
 		MetaContainer.putConfig(archives, config);
 		// clears cached resources
 		clear();
 		// gets rid from all created temporary files
 		tmpResources.removeTempFiles();
 	    }
 	}
     }
 
     /**
      * Scan application for find all {@link javax.ejb.Stateless} beans and
      * {@link Remote} or {@link Local} proxy interfaces
      * 
      * @throws ClassNotFoundException
      * @throws IOException
      */
     public void scanForBeans(File[] jars) throws IOException {
 	List<URL> urlList = new ArrayList<URL>();
 	URL url;
 	for (File file : jars) {
 	    url = file.toURI().toURL();
 	    urlList.add(url);
 	}
 	URL[] archives = ObjectUtils.toArray(urlList, URL.class);
 	scanForBeans(archives);
     }
 
     /**
      * Scan application for find all {@link javax.ejb.Stateless} beans and
      * {@link Remote} or {@link Local} proxy interfaces
      * 
      * @throws ClassNotFoundException
      * @throws IOException
      */
     public void scanForBeans(String... paths) throws IOException {
 
 	if (ObjectUtils.notAvailable(paths)
 		&& ObjectUtils.available(config.getDeploymentPath())) {
 
 	    Set<DeploymentDirectory> deployments = config.getDeploymentPath();
 	    List<String> pathList = new ArrayList<String>();
 	    File deployFile;
 	    for (DeploymentDirectory deployment : deployments) {
 		deployFile = new File(deployment.getPath());
 		if (deployment.isScan()) {
 		    String[] subDeployments = deployFile.list();
 		    if (ObjectUtils.available(subDeployments)) {
 			pathList.addAll(Arrays.asList(subDeployments));
 		    }
 		}
 	    }
 	    paths = ObjectUtils.toArray(pathList, String.class);
 	}
 	List<URL> urlList = new ArrayList<URL>();
 	List<URL> archive;
 	for (String path : paths) {
 	    archive = FileUtils.toURLWithClasspath(path);
 	    urlList.addAll(archive);
 	}
 	URL[] archives = ObjectUtils.toArray(urlList, URL.class);
 	scanForBeans(archives);
     }
 
     public ClassLoader getCurrent() {
 
 	return current;
     }
 
     /**
      * Closes all existing connections
      */
     public static void closeAllConnections() {
 	closeConnections();
     }
 
     public void clear() {
 
 	if (ObjectUtils.available(realURL)) {
 	    realURL.clear();
 	    realURL = null;
 	}
 
 	if (ObjectUtils.available(aggregateds)) {
 	    aggregateds.clear();
 	}
 
 	if (ObjectUtils.available(archivesURLs)) {
 	    archivesURLs.clear();
 	    archivesURLs = null;
 	}
 
 	if (ObjectUtils.available(classOwnersURL)) {
 	    classOwnersURL.clear();
 	    classOwnersURL = null;
 	}
 
 	config = null;
     }
 
     /**
      * Closes all connections clears all caches
      */
     public static void close() {
 
 	closeConnections();
 	MetaContainer.clear();
     }
 
     /**
      * Builder class to provide properties for lightmare application and
      * initialize {@link MetaCreator} instance
      * 
      * @author levan
      * 
      */
     public static class Builder {
 
 	private MetaCreator creator;
 
 	public Builder(boolean cloneConfiguration) throws IOException {
 
 	    creator = MetaCreator.get();
 	    Configuration config = creator.config;
 	    if (cloneConfiguration && ObjectUtils.notNull(config)) {
 		try {
 		    creator.config = (Configuration) config.clone();
 		} catch (CloneNotSupportedException ex) {
 		    throw new IOException(ex);
 		}
 	    } else {
 		creator.config = new Configuration();
 	    }
 	}
 
 	public Builder() throws IOException {
 	    this(Boolean.FALSE);
 	}
 
 	private void initPoolProperties() {
 
 	    if (PoolConfig.poolProperties == null) {
 		PoolConfig.poolProperties = new HashMap<Object, Object>();
 	    }
 	}
 
 	/**
 	 * Sets additional persistence properties
 	 * 
 	 * @param properties
 	 * @return {@link Builder}
 	 */
 	public Builder setPersistenceProperties(Map<String, String> properties) {
 
 	    if (ObjectUtils.available(properties)) {
 		Map<Object, Object> persistenceProperties = new HashMap<Object, Object>();
 		persistenceProperties.putAll(properties);
 		creator.config.setPersistenceProperties(persistenceProperties);
 	    }
 
 	    return this;
 	}
 
 	/**
 	 * Adds property to scan for {@link javax.persistence.Entity} annotated
 	 * classes from deployed archives
 	 * 
 	 * @param scanForEnt
 	 * @return {@link Builder}
 	 */
 	public Builder setScanForEntities(boolean scanForEnt) {
 	    creator.config.setScanForEntities(scanForEnt);
 
 	    return this;
 	}
 
 	/**
 	 * Adds property to use only {@link org.lightmare.annotations.UnitName}
 	 * annotated entities for which
 	 * {@link org.lightmare.annotations.UnitName#value()} matches passed
 	 * unit name
 	 * 
 	 * @param unitName
 	 * @return {@link Builder}
 	 */
 	public Builder setUnitName(String unitName) {
 	    creator.config.setAnnotatedUnitName(unitName);
 
 	    return this;
 	}
 
 	/**
 	 * Sets path for persistence.xml file
 	 * 
 	 * @param path
 	 * @return {@link Builder}
 	 */
 	public Builder setPersXmlPath(String path) {
 	    creator.config.setPersXmlPath(path);
 	    creator.config.setScanArchives(Boolean.FALSE);
 
 	    return this;
 	}
 
 	/**
 	 * Adds path for additional libraries to load at start time
 	 * 
 	 * @param libPaths
 	 * @return {@link Builder}
 	 */
 	public Builder setLibraryPath(String... libPaths) {
 	    creator.config.setLibraryPaths(libPaths);
 
 	    return this;
 	}
 
 	/**
 	 * Sets boolean checker to scan persistence.xml files from appropriated
 	 * jar files
 	 * 
 	 * @param xmlFromJar
 	 * @return {@link Builder}
 	 */
 	public Builder setXmlFromJar(boolean xmlFromJar) {
 	    creator.config.setPersXmlFromJar(xmlFromJar);
 
 	    return this;
 	}
 
 	/**
 	 * Sets boolean checker to swap jta data source value with non jta data
 	 * source value
 	 * 
 	 * @param swapDataSource
 	 * @return {@link Builder}
 	 */
 	public Builder setSwapDataSource(boolean swapDataSource) {
 	    creator.config.setSwapDataSource(swapDataSource);
 
 	    return this;
 	}
 
 	/**
 	 * Adds path for data source file
 	 * 
 	 * @param dataSourcePath
 	 * @return {@link Builder}
 	 */
 	public Builder addDataSourcePath(String dataSourcePath) {
 	    creator.config.addDataSourcePath(dataSourcePath);
 
 	    return this;
 	}
 
 	/**
 	 * This method is deprecated should use
 	 * {@link MetaCreator.Builder#addDataSourcePath(String)} instead
 	 * 
 	 * @param dataSourcePath
 	 * @return {@link MetaCreator.Builder}
 	 */
 	@Deprecated
 	public Builder setDataSourcePath(String dataSourcePath) {
 	    creator.config.addDataSourcePath(dataSourcePath);
 
 	    return this;
 	}
 
 	/**
 	 * Sets boolean checker to scan {@link javax.persistence.Entity}
 	 * annotated classes from appropriated deployed archive files
 	 * 
 	 * @param scanArchives
 	 * @return {@link Builder}
 	 */
 	public Builder setScanArchives(boolean scanArchives) {
 	    creator.config.setScanArchives(scanArchives);
 
 	    return this;
 	}
 
 	/**
 	 * Sets boolean checker to block deployment processes
 	 * 
 	 * @param await
 	 * @return {@link Builder}
 	 */
 	public Builder setAwaitDeploiment(boolean await) {
 	    creator.await = await;
 
 	    return this;
 	}
 
 	/**
 	 * Sets property is server or not in embedded mode
 	 * 
 	 * @param remote
 	 * @return {@link Builder}
 	 */
 	public Builder setRemote(boolean remote) {
 	    creator.config.setRemote(remote);
 
 	    return this;
 	}
 
 	/**
 	 * Sets property is application server or just client for other remote
 	 * server
 	 * 
 	 * @param server
 	 * @return {@link Builder}
 	 */
 	public Builder setServer(boolean server) {
 	    Configuration.setServer(server);
 	    creator.config.setClient(!server);
 
 	    return this;
 	}
 
 	/**
 	 * Sets boolean check is application in just client mode or not
 	 * 
 	 * @param client
 	 * @return {@link Builder}
 	 */
 	public Builder setClient(boolean client) {
 	    creator.config.setClient(client);
 	    Configuration.setServer(!client);
 
 	    return this;
 	}
 
 	/**
 	 * To add any additional property
 	 * 
 	 * @param key
 	 * @param property
 	 * @return {@link Builder}
 	 */
 	public Builder setProperty(String key, String property) {
 	    creator.config.putValue(key, property);
 
 	    return this;
 	}
 
 	/**
 	 * File path for administrator user name and password
 	 * 
 	 * @param property
 	 * @return {@link Builder}
 	 */
 	public Builder setAdminUsersPth(String property) {
 	    Configuration.setAdminUsersPath(property);
 
 	    return this;
 	}
 
 	/**
 	 * Sets specific IP address in case when application is in remote server
 	 * mode
 	 * 
 	 * @param property
 	 * @return {@link Builder}
 	 */
 	public Builder setIpAddress(String property) {
 	    creator.config.putValue(Configuration.IP_ADDRESS, property);
 
 	    return this;
 	}
 
 	/**
 	 * Sets specific port in case when applicatin is in remore server mode
 	 * 
 	 * @param property
 	 * @return {@link Builder}
 	 */
 	public Builder setPort(String property) {
 	    creator.config.putValue(Configuration.PORT, property);
 
 	    return this;
 	}
 
 	/**
 	 * Sets amount for network master threads in case when application is in
 	 * remote server mode
 	 * 
 	 * @param property
 	 * @return {@link Builder}
 	 */
 	public Builder setMasterThreads(String property) {
 	    creator.config.putValue(Configuration.BOSS_POOL, property);
 
 	    return this;
 	}
 
 	/**
 	 * Sets amount of worker threads in case when application is in remote
 	 * server mode
 	 * 
 	 * @param property
 	 * @return {@link Builder}
 	 */
 	public Builder setWorkerThreads(String property) {
 	    creator.config.putValue(Configuration.WORKER_POOL, property);
 
 	    return this;
 	}
 
 	/**
 	 * Adds deploy file path to application with boolean checker if file is
 	 * directory to scan this directory for deployment files list
 	 * 
 	 * @param deploymentPath
 	 * @param scan
 	 * @return {@link Builder}
 	 */
 	public Builder addDeploymentPath(String deploymentPath, boolean scan) {
 	    String clearPath = WatchUtils.clearPath(deploymentPath);
 	    creator.config.addDeploymentPath(clearPath, scan);
 
 	    return this;
 	}
 
 	/**
 	 * Adds deploy file path to application
 	 * 
 	 * @param deploymentPath
 	 * @return {@link Builder}
 	 */
 	public Builder addDeploymentPath(String deploymentPath) {
 	    addDeploymentPath(deploymentPath, Boolean.FALSE);
 
 	    return this;
 	}
 
 	/**
 	 * Adds timeout for connection in case when application is in remote
 	 * server or client mode
 	 * 
 	 * @param property
 	 * @return {@link Builder}
 	 */
 	public Builder setTimeout(String property) {
 	    creator.config.putValue(Configuration.CONNECTION_TIMEOUT, property);
 
 	    return this;
 	}
 
 	/**
 	 * Added boolean check if application is using pooled data source
 	 * 
 	 * @param dsPooledType
 	 * @return {@link Builder}
 	 */
 	public Builder setDataSourcePooledType(boolean dsPooledType) {
 	    JPAManager.pooledDataSource = dsPooledType;
 
 	    return this;
 	}
 
 	/**
 	 * Sets which data source pool provider should use application by
 	 * {@link PoolProviderType} parameter
 	 * 
 	 * @param poolProviderType
 	 * @return {@link Builder}
 	 */
 	public Builder setPoolProviderType(PoolProviderType poolProviderType) {
 	    PoolConfig.poolProviderType = poolProviderType;
 
 	    return this;
 	}
 
 	/**
 	 * Sets path for data source pool additional properties
 	 * 
 	 * @param path
 	 * @return {@link Builder}
 	 */
 	public Builder setPoolPropertiesPath(String path) {
 	    PoolConfig.poolPath = path;
 
 	    return this;
 	}
 
 	/**
 	 * Sets data source pool additional properties
 	 * 
 	 * @param properties
 	 * @return {@link Builder}
 	 */
 	public Builder setPoolProperties(Properties properties) {
 	    initPoolProperties();
 	    PoolConfig.poolProperties.putAll(properties);
 
 	    return this;
 	}
 
 	/**
 	 * Adds instance property for pooled data source
 	 * 
 	 * @param key
 	 * @param value
 	 * @return {@link Builder}
 	 */
 	public Builder addPoolProperty(Object key, Object value) {
 	    initPoolProperties();
 	    PoolConfig.poolProperties.put(key, value);
 
 	    return this;
 	}
 
 	/**
 	 * Sets boolean check is application in hot deployment (with watch
 	 * service on deployment directories) or not
 	 * 
 	 * @param hotDeployment
 	 * @return {@link Builder}
 	 */
 	public Builder setHotDeployment(boolean hotDeployment) {
 	    creator.config.setHotDeployment(hotDeployment);
 
 	    return this;
 	}
 
 	public MetaCreator build() throws IOException {
 	    creator.config.configure();
 	    LOG.info("Lightmare application starts working");
 
 	    return creator;
 	}
     }
 }
