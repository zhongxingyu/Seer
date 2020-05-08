 package org.lightmare.ejb.startup;
 
 import static org.lightmare.ejb.meta.MetaContainer.closeConnections;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
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
 import org.lightmare.config.Configuration;
 import org.lightmare.ejb.meta.ArchiveData;
 import org.lightmare.ejb.meta.MetaData;
 import org.lightmare.ejb.meta.TmpResources;
 import org.lightmare.jpa.JPAManager;
 import org.lightmare.jpa.datasource.DataSourceInitializer;
 import org.lightmare.libraries.LibraryLoader;
 import org.lightmare.remote.rpc.RpcListener;
 import org.lightmare.scannotation.AnnotationDB;
 import org.lightmare.utils.AbstractIOUtils;
 import org.lightmare.utils.shutdown.ShutDown;
 
 /**
  * Determines and saves in cache ejb beans {@link MetaData} on startup
  * 
  * @author Levan
  * 
  */
 public class MetaCreator {
 
 	private static AnnotationDB annotationDB;
 
 	private Map<String, String> prop;
 
 	private boolean scanForEntities;
 
 	private String annotatedUnitName;
 
 	private String persXmlPath;
 
 	private String[] libraryPaths;
 
 	private boolean persXmlFromJar;
 
 	private boolean swapDataSource;
 
 	private String dataSourcePath;
 
 	private boolean scanArchives;
 
 	private TmpResources tmpResources;
 
 	private boolean await;
 
 	private CountDownLatch conn;
 
 	/**
 	 * {@link Configuration} container class for server
 	 */
 	public static final Configuration CONFIG = new Configuration();
 
 	// Data for cache at deploy time
 	private Map<String, AbstractIOUtils> aggregateds = new HashMap<String, AbstractIOUtils>();
 
 	private Map<URL, ArchiveData> archivesURLs;
 
 	private Map<String, URL> classOwnersURL;
 
 	private static final Logger LOG = Logger.getLogger(MetaCreator.class);
 
 	private MetaCreator() {
 		tmpResources = new TmpResources();
 		ShutDown.setHook(tmpResources);
 	}
 
 	public AnnotationDB getAnnotationDB() {
 		return annotationDB;
 	}
 
 	private boolean checkForUnitName(String className) throws IOException {
 		boolean isValid = false;
 		Class<?> entityClass;
 		try {
 			entityClass = Class.forName(className);
 		} catch (ClassNotFoundException ex) {
 			throw new IOException(ex);
 		}
 		UnitName annotation = entityClass.getAnnotation(UnitName.class);
 		isValid = annotation.value().equals(annotatedUnitName);
 
 		return isValid;
 	}
 
 	private List<String> translateToList(Set<String> classSet) {
 		return Arrays.asList(classSet.toArray(new String[classSet.size()]));
 	}
 
 	private void filterEntitiesForJar(Set<String> classSet,
 			String fileNameForBean) {
 
 		Map<String, String> classOwnersFiles = annotationDB
 				.getClassOwnersFiles();
 
 		String fileNameForEntity;
 		for (String entityName : classSet) {
 			fileNameForEntity = classOwnersFiles.get(entityName);
 			if (fileNameForEntity != null && fileNameForBean != null
 					&& !fileNameForEntity.equals(fileNameForBean)) {
 				classSet.remove(entityName);
 			}
 		}
 	}
 
 	private List<String> filterEntities(Set<String> classSet)
 			throws IOException {
 		List<String> classes;
 		if (annotatedUnitName == null) {
 			classes = translateToList(classSet);
 		} else {
 			Set<String> filtereds = new HashSet<String>();
 			for (String className : classSet) {
 				if (checkForUnitName(className)) {
 					filtereds.add(className);
 				}
 			}
 			classes = translateToList(filtereds);
 		}
 
 		return classes;
 	}
 
 	protected void configureConnection(String unitName, String beanName)
 			throws IOException {
 
 		JPAManager.Builder builder = new JPAManager.Builder();
 		Map<String, String> classOwnersFiles = annotationDB
 				.getClassOwnersFiles();
 		AbstractIOUtils ioUtils = aggregateds.get(beanName);
 
 		if (ioUtils != null) {
 			URL jarURL = ioUtils.getAppropriatedURL(classOwnersFiles, beanName);
 			builder.setURL(jarURL);
 		}
 		if (scanForEntities) {
 			Set<String> classSet;
 			Map<String, Set<String>> annotationIndex = annotationDB
 					.getAnnotationIndex();
 			classSet = annotationIndex.get(Entity.class.getName());
 			if (annotatedUnitName == null) {
 				classSet = annotationIndex.get(Entity.class.getName());
 			} else if (annotatedUnitName.equals(unitName)) {
 				Set<String> unitNamedSet = annotationIndex.get(UnitName.class
 						.getName());
 				classSet.retainAll(unitNamedSet);
 			}
 			if (ioUtils != null) {
 				String fileNameForBean = classOwnersFiles.get(beanName);
 				filterEntitiesForJar(classSet, fileNameForBean);
 			}
 			List<String> classes = filterEntities(classSet);
 			builder.setClasses(classes);
 		}
 		builder.setPath(persXmlPath).setProperties(prop)
 				.setSwapDataSource(swapDataSource)
 				.setScanArchives(scanArchives).build().setConnection(unitName);
 	}
 
 	private URL[] getFullArchives(URL[] archives) throws IOException {
 		List<URL> modifiedArchives = new ArrayList<URL>();
 		AbstractIOUtils ioUtils;
 		List<URL> ejbURLs;
 		ArchiveData archiveData;
 		for (URL archive : archives) {
 			ioUtils = AbstractIOUtils.getAppropriatedType(archive);
 			if (ioUtils != null) {
 				ioUtils.scan(persXmlFromJar);
 				ejbURLs = ioUtils.getEjbURLs();
 				modifiedArchives.addAll(ejbURLs);
 				archiveData = new ArchiveData();
 				archiveData.setIoUtils(ioUtils);
 				if (ejbURLs.isEmpty()) {
 					archivesURLs.put(archive, archiveData);
 				} else {
 					for (URL ejbURL : ejbURLs) {
 						archivesURLs.put(ejbURL, archiveData);
 					}
 				}
 			}
 		}
 
 		return modifiedArchives.toArray(new URL[modifiedArchives.size()]);
 	}
 
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
 
 	private void awaitDeployments() {
 		try {
 			conn.await();
 		} catch (InterruptedException ex) {
 			LOG.error(ex);
 		}
 	}
 
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
 		if (ioUtils != null) {
 			if (!ioUtils.isExecuted()) {
 				ioUtils.scan(persXmlFromJar);
 			}
 			URL[] libURLs = ioUtils.getURLs();
			if (loader == null) {
 				loader = LibraryLoader.getEnrichedLoader(libURLs);
 				archiveData.setLoader(loader);
 			}
 			aggregateds.put(beanName, ioUtils);
 		}
 		List<File> tmpFiles = ioUtils.getTmpFiles();
 
 		Future<String> future = BeanLoader.loadBean(this, beanName, loader,
 				tmpFiles, conn);
 		awaitDeployment(future);
 		if (tmpFiles != null) {
 			tmpResources.addFile(tmpFiles);
 		}
 	}
 
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
 	}
 
 	/**
 	 * Scan application for find all {@link Stateless} beans and {@link Remote}
 	 * or {@link Local} proxy interfaces
 	 * 
 	 * @param archives
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	public void scanForBeans(URL[] archives) throws IOException {
 
 		try {
 			// starts RPC server if configured as remote and server
 			if (CONFIG.isRemote() && CONFIG.isServer()) {
 				RpcListener.startServer();
 			}
 			// Loads libraries from specified path
 			if (libraryPaths != null) {
 				LibraryLoader.loadLibraries(libraryPaths);
 			}
 			archivesURLs = new HashMap<URL, ArchiveData>();
 			URL[] fullArchives = getFullArchives(archives);
 			annotationDB = new AnnotationDB();
 			annotationDB.setScanFieldAnnotations(false);
 			annotationDB.setScanParameterAnnotations(false);
 			annotationDB.setScanMethodAnnotations(false);
 			annotationDB.scanArchives(fullArchives);
 			Set<String> beanNames = annotationDB.getAnnotationIndex().get(
 					Stateless.class.getName());
 			classOwnersURL = annotationDB.getClassOwnersURLs();
 			DataSourceInitializer.initializeDataSource(dataSourcePath);
 			if (beanNames != null) {
 				deployBeans(beanNames);
 			}
 		} finally {
 			// gets rid from all created temporary files
 			tmpResources.removeTempFiles();
 		}
 	}
 
 	/**
 	 * Scan application for find all {@link Stateless} beans and {@link Remote}
 	 * or {@link Local} proxy interfaces
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
 		URL[] archives = urlList.toArray(new URL[urlList.size()]);
 		scanForBeans(archives);
 	}
 
 	/**
 	 * Scan application for find all {@link Stateless} beans and {@link Remote}
 	 * or {@link Local} proxy interfaces
 	 * 
 	 * @throws ClassNotFoundException
 	 * @throws IOException
 	 */
 	public void scanForBeans(String... paths) throws IOException {
 		List<URL> urlList = new ArrayList<URL>();
 		URL archive;
 		File file;
 		for (String path : paths) {
 			file = new File(path);
 			archive = file.toURI().toURL();
 			urlList.add(archive);
 		}
 		URL[] archives = urlList.toArray(new URL[urlList.size()]);
 		scanForBeans(archives);
 	}
 
 	/**
 	 * Closes all existing connections
 	 */
 	public static void closeAllConnections() {
 		closeConnections();
 	}
 
 	/**
 	 * Builder class to provide properties for lightmare
 	 * 
 	 * @author levan
 	 * 
 	 */
 	public static class Builder {
 
 		private MetaCreator creator;
 
 		public Builder() {
 			creator = new MetaCreator();
 		}
 
 		public Builder setPersistenceProperties(Map<String, String> properties) {
 			creator.prop = properties;
 			return this;
 		}
 
 		public Builder setScanForEntities(boolean scanForEnt) {
 			creator.scanForEntities = scanForEnt;
 			return this;
 		}
 
 		public Builder setUnitName(String unitName) {
 			creator.annotatedUnitName = unitName;
 			return this;
 		}
 
 		public Builder setPersXmlPath(String path) {
 			creator.persXmlPath = path;
 			creator.scanArchives = false;
 			return this;
 		}
 
 		public Builder setLibraryPath(String... libPaths) {
 			creator.libraryPaths = libPaths;
 			return this;
 		}
 
 		public Builder setXmlFromJar(boolean xmlFromJar) {
 			creator.persXmlFromJar = xmlFromJar;
 			return this;
 		}
 
 		public Builder setSwapDataSource(boolean swapDataSource) {
 			creator.swapDataSource = swapDataSource;
 			return this;
 		}
 
 		public Builder setDataSourcePath(String dataSourcePath) {
 			creator.dataSourcePath = dataSourcePath;
 			return this;
 		}
 
 		public Builder setScanArchives(boolean scanArchives) {
 			creator.scanArchives = scanArchives;
 			return this;
 		}
 
 		public Builder setAwaitSeploiment(boolean await) {
 			creator.await = await;
 			return this;
 		}
 
 		public Builder setRemote(boolean remote) {
 			CONFIG.setRemote(remote);
 			return this;
 		}
 
 		public Builder setServer(boolean server) {
 			CONFIG.setServer(server);
 			CONFIG.setClient(!server);
 			return this;
 		}
 
 		public Builder setClient(boolean client) {
 			CONFIG.setClient(client);
 			CONFIG.setServer(!client);
 			return this;
 		}
 
 		public Builder setProperty(String key, String property) {
 			CONFIG.putValue(key, property);
 			return this;
 		}
 
 		public Builder setIpAddress(String property) {
 			CONFIG.putValue(Configuration.IP_ADDRESS, property);
 			return this;
 		}
 
 		public Builder setPort(String property) {
 			CONFIG.putValue(Configuration.PORT, property);
 			return this;
 		}
 
 		public Builder setMasterThreads(String property) {
 			CONFIG.putValue(Configuration.BOSS_POOL, property);
 			return this;
 		}
 
 		public Builder setWorkerThreads(String property) {
 			CONFIG.putValue(Configuration.WORKER_POOL, property);
 			return this;
 		}
 
 		public Builder setTimeout(String property) {
 			CONFIG.putValue(Configuration.CONNECTION_TIMEOUT, property);
 			return this;
 		}
 
 		public MetaCreator build() {
 			return creator;
 		}
 
 	}
 }
