 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.server;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Timer;
 
 import org.eclipse.core.runtime.ILogListener;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.emfstore.common.ResourceFactoryRegistry;
 import org.eclipse.emf.emfstore.common.model.util.FileUtil;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.server.accesscontrol.AccessControlImpl;
 import org.eclipse.emf.emfstore.server.connection.ConnectionHandler;
 import org.eclipse.emf.emfstore.server.connection.xmlrpc.XmlRpcAdminConnectionHander;
 import org.eclipse.emf.emfstore.server.connection.xmlrpc.XmlRpcConnectionHandler;
 import org.eclipse.emf.emfstore.server.core.AdminEmfStoreImpl;
 import org.eclipse.emf.emfstore.server.core.EmfStoreImpl;
 import org.eclipse.emf.emfstore.server.core.helper.HistoryCache;
 import org.eclipse.emf.emfstore.server.exceptions.FatalEmfStoreException;
 import org.eclipse.emf.emfstore.server.exceptions.StorageException;
 import org.eclipse.emf.emfstore.server.model.ModelFactory;
 import org.eclipse.emf.emfstore.server.model.ServerSpace;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.ACUser;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.AccesscontrolFactory;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.roles.RolesFactory;
 import org.eclipse.emf.emfstore.server.startup.EmfStoreValidator;
 import org.eclipse.emf.emfstore.server.startup.ExtensionManager;
 import org.eclipse.emf.emfstore.server.startup.MigrationManager;
 import org.eclipse.emf.emfstore.server.storage.ResourceStorage;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 
 /**
  * The {@link EmfStoreController} is controlling startup and shutdown of the
  * EmfStore.
  * 
  * @author koegel
  * @author wesendonk
  */
 public class EmfStoreController implements IApplication, Runnable {
 
 	/**
 	 * The period of time in seconds between executing the clean memory task.
 	 */
 	private static final int CLEAN_MEMORY_TASK_PERIOD = 60;
 	private static EmfStoreController instance;
 
 	private EmfStore emfStore;
 	private AdminEmfStore adminEmfStore;
 	private AccessControlImpl accessControl;
 	private Set<ConnectionHandler<? extends EmfStoreInterface>> connectionHandlers;
 	private Properties properties;
 	private ServerSpace serverSpace;
 	private Resource resource;
 	private HistoryCache historyCache;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
 	 */
 	public synchronized Object start(IApplicationContext context) throws FatalEmfStoreException {
 		run(true);
 		instance = null;
 		ModelUtil.logInfo("Server is STOPPED.");
 		return IApplication.EXIT_OK;
 	}
 
 	/**
 	 * Run the server.
 	 * 
 	 * @param waitForTermination
 	 *            true if the server should force the calling thread to wait for
 	 *            its termination
 	 * @throws FatalEmfStoreException
 	 *             if the server fails fatally
 	 */
 	public synchronized void run(boolean waitForTermination) throws FatalEmfStoreException {
 		if (instance != null) {
 			throw new FatalEmfStoreException("Another EmfStore Controller seems to be running already!");
 		}
 
 		instance = this;
 
 		serverHeader();
 
 		initLogging();
 
 		// copy es.properties file to workspace if not existent
 		copyFileToWorkspace(ServerConfiguration.getConfFile(), "es.properties",
 			"Couldn't copy es.properties file to config folder.",
 			"Default es.properties file was copied to config folder.");
 
 		properties = initProperties();
 
 		new MigrationManager().migrateModel();
 		this.serverSpace = initServerSpace();
 
 		handleStartupListener();
 
 		historyCache = initHistoryCache();
 
 		accessControl = initAccessControl(serverSpace);
 		emfStore = new EmfStoreImpl(serverSpace, accessControl);
 		adminEmfStore = new AdminEmfStoreImpl(serverSpace, accessControl);
 
 		// copy keystore file to workspace if not existent
 		copyFileToWorkspace(ServerConfiguration.getServerKeyStorePath(), ServerConfiguration.SERVER_KEYSTORE_FILE,
 			"Failed to copy keystore.", "Keystore was copied to server workspace.");
 
 		connectionHandlers = initConnectionHandlers();
 
 		if (Boolean.parseBoolean(ServerConfiguration.getProperties().getProperty(
 			ServerConfiguration.PERFORM_CLEAN_MEMORY_TASK, ServerConfiguration.PERFORM_CLEAN_MEMORY_TASK_DEFAULT))) {
 			new Timer().schedule(new CleanMemoryTask(serverSpace.eResource().getResourceSet()),
 				CLEAN_MEMORY_TASK_PERIOD * 1000, CLEAN_MEMORY_TASK_PERIOD * 1000);
 		}
 
 		handlePostStartupListener();
 
 		ModelUtil.logInfo("Initialitation COMPLETE.");
		ModelUtil.logInfo("Server is RUNNING...");
 		if (waitForTermination) {
 			waitForTermination();
 		}
 
 	}
 
 	private void initLogging() {
 		Platform.getLog(Platform.getBundle("org.eclipse.emf.emfstore.common.model")).addLogListener(new ILogListener() {
 
 			public void logging(IStatus status, String plugin) {
 				if (status.getSeverity() == IStatus.INFO) {
 					System.out.println(status.getMessage());
 				} else if (!status.isOK()) {
 					System.err.println(status.getMessage());
 					Throwable exception = status.getException();
 					if (exception != null) {
 						exception.printStackTrace(System.err);
 					}
 				}
 			}
 
 		});
 	}
 
 	private void handleStartupListener() {
 		String property = ServerConfiguration.getProperties().getProperty(ServerConfiguration.LOAD_STARTUP_LISTENER,
 			ServerConfiguration.LOAD_STARTUP_LISTENER_DEFAULT);
 		if (ServerConfiguration.TRUE.equals(property)) {
 			ModelUtil.logInfo("Notifying startup listener");
 			ExtensionManager.notifyStartupListener(serverSpace.getProjects());
 		}
 	}
 
 	private void handlePostStartupListener() {
 		String property = ServerConfiguration.getProperties().getProperty(
 			ServerConfiguration.LOAD_POST_STARTUP_LISTENER, ServerConfiguration.LOAD_STARTUP_LISTENER_DEFAULT);
 		if (ServerConfiguration.TRUE.equals(property)) {
 			ModelUtil.logInfo("Notifying post startup listener");
 			ExtensionManager.notifyPostStartupListener(serverSpace, accessControl, connectionHandlers);
 		}
 	}
 
 	private void copyFileToWorkspace(String target, String source, String failure, String success) {
 		File keyStore = new File(target);
 		if (!keyStore.exists()) {
 			try {
 				FileUtil.copyFile(getClass().getResourceAsStream(source), keyStore);
 			} catch (IOException e) {
 				ModelUtil.logWarning("Copy of file from " + source + " to " + target + " failed", e);
 			}
 		}
 	}
 
 	private HistoryCache initHistoryCache() {
 		HistoryCache cache = new HistoryCache();
 		cache.initCache(serverSpace.getProjects());
 		ModelUtil.logInfo("History cache has been initialized.");
 		return cache;
 	}
 
 	private Set<ConnectionHandler<? extends EmfStoreInterface>> initConnectionHandlers() throws FatalEmfStoreException {
 		Set<ConnectionHandler<? extends EmfStoreInterface>> connectionHandlers = new HashSet<ConnectionHandler<? extends EmfStoreInterface>>();
 
 		// crate XML RPC connection handlers
 		XmlRpcConnectionHandler xmlRpcConnectionHander = new XmlRpcConnectionHandler();
 		xmlRpcConnectionHander.init(emfStore, accessControl);
 		connectionHandlers.add(xmlRpcConnectionHander);
 
 		XmlRpcAdminConnectionHander xmlRpcAdminConnectionHander = new XmlRpcAdminConnectionHander();
 		xmlRpcAdminConnectionHander.init(adminEmfStore, accessControl);
 		connectionHandlers.add(xmlRpcAdminConnectionHander);
 
 		return connectionHandlers;
 	}
 
 	/**
 	 * Returns the history cache.
 	 * 
 	 * @return the history cache.
 	 */
 	public HistoryCache getHistoryCache() {
 		return historyCache;
 	}
 
 	private ServerSpace initServerSpace() throws FatalEmfStoreException {
 		ResourceStorage storage = initStorage();
 		URI resourceUri = storage.init(properties);
 		ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.setResourceFactoryRegistry(new ResourceFactoryRegistry());
 		resourceSet.getLoadOptions().putAll(ModelUtil.getResourceLoadOptions());
 		resource = resourceSet.createResource(resourceUri);
 		try {
 			resource.load(ModelUtil.getResourceLoadOptions());
 
 			if (properties.getProperty(ServerConfiguration.VALIDATE_SERVERSPACE_ON_SERVERSTART, "true").equals("true")) {
 				ModelUtil.logInfo("Validating serverspace ...");
 				validateServerSpace(resource);
 				ModelUtil.logInfo("Validation complete.");
 			}
 		} catch (IOException e) {
 			throw new FatalEmfStoreException(StorageException.NOLOAD, e);
 		}
 
 		ServerSpace result = null;
 		EList<EObject> contents = resource.getContents();
 		for (EObject content : contents) {
 			if (content instanceof ServerSpace) {
 				result = (ServerSpace) content;
 				break;
 			}
 		}
 
 		if (result != null) {
 			result.setResource(resource);
 		} else {
 			// if no serverspace can be loaded, create one
 			ModelUtil.logInfo("Creating initial server space...");
 			result = ModelFactory.eINSTANCE.createServerSpace();
 
 			result.setResource(resource);
 			resource.getContents().add(result);
 
 			try {
 				result.save();
 			} catch (IOException e) {
 				throw new FatalEmfStoreException(StorageException.NOSAVE, e);
 			}
 		}
 
 		return result;
 	}
 
 	private void validateServerSpace(Resource resource) throws FatalEmfStoreException {
 		EList<EObject> contents = resource.getContents();
 		for (EObject object : contents) {
 			if (object instanceof ServerSpace) {
 				EmfStoreValidator emfStoreValidator = new EmfStoreValidator((ServerSpace) object);
 				String[] excludedProjects = ServerConfiguration.getSplittedProperty(
 					ServerConfiguration.VALIDATION_PROJECT_EXCLUDE,
 					ServerConfiguration.VALIDATION_PROJECT_EXCLUDE_DEFAULT);
 				emfStoreValidator.setExcludedProjects(Arrays.asList(excludedProjects));
 				try {
 					String level = ServerConfiguration.getProperties().getProperty(
 						ServerConfiguration.VALIDATION_LEVEL, ServerConfiguration.VALIDATION_LEVEL_DEFAULT);
 					emfStoreValidator.validate(Integer.parseInt(level));
 				} catch (NumberFormatException e) {
 					emfStoreValidator.validate(Integer.parseInt(ServerConfiguration.VALIDATION_LEVEL_DEFAULT));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Return the singleton instance of EmfStoreControler.
 	 * 
 	 * @return the instance
 	 */
 	public static EmfStoreController getInstance() {
 		return instance;
 	}
 
 	private ResourceStorage initStorage() throws FatalEmfStoreException {
 		String className = properties.getProperty(ServerConfiguration.RESOURCE_STORAGE,
 			ServerConfiguration.RESOURCE_STORAGE_DEFAULT);
 
 		ResourceStorage resourceStorage;
 		final String failMessage = "Failed loading ressource storage!";
 		try {
 			ModelUtil.logInfo("Using RessourceStorage \"" + className + "\".");
 			resourceStorage = (ResourceStorage) Class.forName(className).getConstructor().newInstance();
 			return resourceStorage;
 		} catch (IllegalArgumentException e) {
 			ModelUtil.logException(failMessage, e);
 			throw new FatalEmfStoreException(failMessage, e);
 		} catch (SecurityException e) {
 			ModelUtil.logException(failMessage, e);
 			throw new FatalEmfStoreException(failMessage, e);
 		} catch (InstantiationException e) {
 			ModelUtil.logException(failMessage, e);
 			throw new FatalEmfStoreException(failMessage, e);
 		} catch (IllegalAccessException e) {
 			ModelUtil.logException(failMessage, e);
 			throw new FatalEmfStoreException(failMessage, e);
 		} catch (InvocationTargetException e) {
 			ModelUtil.logException(failMessage, e);
 			throw new FatalEmfStoreException(failMessage, e);
 		} catch (NoSuchMethodException e) {
 			ModelUtil.logException(failMessage, e);
 			throw new FatalEmfStoreException(failMessage, e);
 		} catch (ClassNotFoundException e) {
 			ModelUtil.logException(failMessage, e);
 			throw new FatalEmfStoreException(failMessage, e);
 		}
 	}
 
 	private AccessControlImpl initAccessControl(ServerSpace serverSpace) throws FatalEmfStoreException {
 		setSuperUser(serverSpace);
 		return new AccessControlImpl(serverSpace);
 	}
 
 	private void setSuperUser(ServerSpace serverSpace) throws FatalEmfStoreException {
 		String superuser = ServerConfiguration.getProperties().getProperty(ServerConfiguration.SUPER_USER,
 			ServerConfiguration.SUPER_USER_DEFAULT);
 		for (ACUser user : serverSpace.getUsers()) {
 			if (user.getName().equals(superuser)) {
 				return;
 			}
 		}
 		ACUser superUser = AccesscontrolFactory.eINSTANCE.createACUser();
 		superUser.setName(superuser);
 		superUser.setFirstName("super");
 		superUser.setLastName("user");
 		superUser.setDescription("default server admin (superuser)");
 		superUser.getRoles().add(RolesFactory.eINSTANCE.createServerAdmin());
 		serverSpace.getUsers().add(superUser);
 		try {
 			serverSpace.save();
 		} catch (IOException e) {
 			throw new FatalEmfStoreException(StorageException.NOSAVE, e);
 		}
 		ModelUtil.logInfo("added superuser " + superuser);
 	}
 
 	private Properties initProperties() {
 		File propertyFile = new File(ServerConfiguration.getConfFile());
 		Properties properties = new Properties();
 		try {
 			FileInputStream fis = new FileInputStream(propertyFile);
 			properties.load(fis);
 			ServerConfiguration.setProperties(properties);
 			fis.close();
 			ModelUtil.logInfo("Property file read. (" + propertyFile.getAbsolutePath() + ")");
 		} catch (IOException e) {
 			ModelUtil.logWarning("Property initialization failed, using default properties.", e);
 		}
 		return properties;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.equinox.app.IApplication#stop()
 	 */
 	public void stop() {
 		wakeForTermination();
 		for (ConnectionHandler<? extends EmfStoreInterface> handler : connectionHandlers) {
 			handler.stop(false);
 		}
 		ModelUtil.logInfo("Server was stopped.");
 		instance = null;
 		wakeForTermination();
 	}
 
 	/**
 	 * Shutdown EmfStore due to an fatal exception.
 	 * 
 	 * @param exception
 	 *            the fatal exception that triggered the shutdown
 	 * @generated NOT
 	 */
 	public void shutdown(FatalEmfStoreException exception) {
 		ModelUtil.logWarning("Stopping all connection handlers...");
 		for (ConnectionHandler<? extends EmfStoreInterface> handler : connectionHandlers) {
 			ModelUtil.logWarning("Stopping connection handler \"" + handler.getName() + "\".");
 			handler.stop(true);
 			ModelUtil.logWarning("Connection handler \"" + handler.getName() + "\" stopped.");
 		}
 		ModelUtil.logException("Server was forcefully stopped.", exception);
 		ModelUtil.logException("Cause for server shutdown: ", exception.getCause());
 		wakeForTermination();
 	}
 
 	private synchronized void waitForTermination() {
 		try {
 			wait();
 		} catch (InterruptedException e) {
 			ModelUtil.logWarning("Waiting for termination was interrupted", e);
 		}
 	}
 
 	private synchronized void wakeForTermination() {
 		notify();
 	}
 
 	private void serverHeader() {
 		InputStream inputStream = getClass().getResourceAsStream("emfstore.txt");
 		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
 		String line;
 		try {
 			while ((line = reader.readLine()) != null) {
 				System.out.println(line);
 			}
 		} catch (IOException e) {
 		}
 
 		System.out.println("*----------*");
 		System.out.println("| EmfStore |");
 		System.out.println("*----------*");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see java.lang.Runnable#run()
 	 */
 	public void run() {
 		try {
 			run(false);
 		} catch (FatalEmfStoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * starts the server a new thread.
 	 * 
 	 * @throws FatalEmfStoreException
 	 *             in case of failure
 	 */
 	public static void runAsNewThread() throws FatalEmfStoreException {
 		Thread thread = new Thread(new EmfStoreController());
 		thread.start();
 		try {
 			thread.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 }
