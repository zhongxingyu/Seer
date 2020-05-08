 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * wesendonk
  * koegel
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.server;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.eclipse.core.runtime.ILogListener;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionElement;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionPoint;
 import org.eclipse.emf.emfstore.internal.common.ResourceFactoryRegistry;
 import org.eclipse.emf.emfstore.internal.common.model.util.FileUtil;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.server.accesscontrol.AccessControl;
 import org.eclipse.emf.emfstore.internal.server.accesscontrol.AccessControlImpl;
 import org.eclipse.emf.emfstore.internal.server.connection.ConnectionHandler;
 import org.eclipse.emf.emfstore.internal.server.connection.xmlrpc.XmlRpcAdminConnectionHandler;
 import org.eclipse.emf.emfstore.internal.server.connection.xmlrpc.XmlRpcConnectionHandler;
 import org.eclipse.emf.emfstore.internal.server.core.AdminEmfStoreImpl;
 import org.eclipse.emf.emfstore.internal.server.core.EMFStoreImpl;
 import org.eclipse.emf.emfstore.internal.server.core.MonitorProvider;
 import org.eclipse.emf.emfstore.internal.server.core.helper.EPackageHelper;
 import org.eclipse.emf.emfstore.internal.server.core.helper.ResourceHelper;
 import org.eclipse.emf.emfstore.internal.server.exceptions.FatalESException;
 import org.eclipse.emf.emfstore.internal.server.exceptions.StorageException;
 import org.eclipse.emf.emfstore.internal.server.model.ModelFactory;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectHistory;
 import org.eclipse.emf.emfstore.internal.server.model.ServerSpace;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACUser;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.AccesscontrolFactory;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.roles.RolesFactory;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.BranchInfo;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersioningFactory;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.Versions;
 import org.eclipse.emf.emfstore.internal.server.startup.EmfStoreValidator;
 import org.eclipse.emf.emfstore.internal.server.startup.MigrationManager;
 import org.eclipse.emf.emfstore.internal.server.startup.PostStartupListener;
 import org.eclipse.emf.emfstore.internal.server.startup.StartupListener;
 import org.eclipse.emf.emfstore.internal.server.storage.ResourceStorage;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 
 /**
  * The {@link EMFStoreController} is controlling startup and shutdown of the
  * EmfStore.
  * 
  * @author koegel
  * @author wesendonk
  */
 public class EMFStoreController implements IApplication, Runnable {
 
 	/**
 	 * The period of time in seconds between executing the clean memory task.
 	 */
 	private static EMFStoreController instance;
 
 	private EMFStore emfStore;
 	private AdminEmfStore adminEmfStore;
 	private AccessControlImpl accessControl;
 	private Set<ConnectionHandler<? extends EMFStoreInterface>> connectionHandlers;
 	private Properties properties;
 	private ServerSpace serverSpace;
 	private Resource resource;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
 	 */
 	public synchronized Object start(IApplicationContext context) throws FatalESException {
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
 	 * @throws FatalESException
 	 *             if the server fails fatally
 	 */
 	public synchronized void run(boolean waitForTermination) throws FatalESException {
 		if (instance != null) {
 			throw new FatalESException("Another EmfStore Controller seems to be running already!");
 		}
 
 		instance = this;
 
 		serverHeader();
 
 		initLogging();
 
 		// copy es.properties file to workspace if not existent
 		copyFileToWorkspace(ServerConfiguration.getConfFile(), "es.properties",
 			"Couldn't copy es.properties file to config folder.",
 			"Default es.properties file was copied to config folder.");
 
 		properties = initProperties();
 
 		logGeneralInformation();
 
 		loadDynamicModels();
 
 		new MigrationManager().migrateModel();
 		serverSpace = initServerSpace();
 
 		initializeBranchesIfRequired(serverSpace);
 
 		handleStartupListener();
 
 		accessControl = initAccessControl(serverSpace);
 		emfStore = EMFStoreImpl.createInterface(serverSpace, accessControl);
 		adminEmfStore = new AdminEmfStoreImpl(serverSpace, accessControl);
 
 		// copy keystore file to workspace if not existent
 		copyFileToWorkspace(ServerConfiguration.getServerKeyStorePath(), ServerConfiguration.SERVER_KEYSTORE_FILE,
 			"Failed to copy keystore.", "Keystore was copied to server workspace.");
 
 		connectionHandlers = initConnectionHandlers();
 
 		handlePostStartupListener();
 		registerShutdownHook();
 
 		ModelUtil.logInfo("Initialitation COMPLETE.");
 		ModelUtil.logInfo("Server is RUNNING...Time to relax...");
 		if (waitForTermination) {
 			waitForTermination();
 		}
 
 	}
 
 	/**
 	 * 
 	 */
 	private void registerShutdownHook() {
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			@Override
 			public void run() {
 				stopServer();
 			}
 		});
 	}
 
 	private void logGeneralInformation() {
 		ModelUtil.logInfo("Server data home location: " + ServerConfiguration.getServerHome());
 		ModelUtil.logInfo("JVM Max Memory: " + Runtime.getRuntime().maxMemory() / 1000000 + " MByte");
 	}
 
 	private void initializeBranchesIfRequired(ServerSpace serverSpace) throws FatalESException {
 		for (final ProjectHistory project : serverSpace.getProjects()) {
 			if (project.getBranches().size() == 0) {
 				// create branch information
 				final BranchInfo branchInfo = VersioningFactory.eINSTANCE.createBranchInfo();
 				branchInfo.setName(VersionSpec.BRANCH_DEFAULT_NAME);
 
 				branchInfo.setHead(ModelUtil.clone(project.getLastVersion().getPrimarySpec()));
 				// set branch source to 0 since no branches can have existed
 				branchInfo.setSource(ModelUtil.clone(Versions.createPRIMARY(VersionSpec.BRANCH_DEFAULT_NAME, 0)));
 				project.getBranches().add(branchInfo);
 				new ResourceHelper(serverSpace).save(project);
 			}
 		}
 	}
 
 	// loads the ".ecore"-files from the dynamic-models-folder
 	private void loadDynamicModels() {
 		ServerConfiguration.getServerHome();
 
 		// TODO: retrieve path from configuration-file
 		final File dir = new File(ServerConfiguration.getServerHome() + "dynamic-models");
 		File[] files = null;
 
 		files = dir.listFiles(new FilenameFilter() {
 			public boolean accept(File d, String name) {
 				return name.endsWith(".ecore");
 			}
 		});
 		if (files != null) {
 			for (final File file : files) {
 				final ResourceSet resourceSet = new ResourceSetImpl();
 				resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 					.put("ecore", new EcoreResourceFactoryImpl());
 				final Resource resource = resourceSet.getResource(URI.createFileURI(file.getAbsolutePath()), true);
 				final EPackage model = (EPackage) resource.getContents().get(0);
 				EPackage.Registry.INSTANCE.put(model.getNsURI(), model);
 				final List<EPackage> packages = EPackageHelper.getAllSubPackages(model);
 				for (final EPackage subPkg : packages) {
 					EPackage.Registry.INSTANCE.put(subPkg.getNsURI(), subPkg);
 				}
 				ModelUtil.logInfo("Dynamic Model \"" + model.getNsURI() + "\" loaded.");
 			}
 		}
 	}
 
 	private void initLogging() {
 		Platform.getLog(Platform.getBundle("org.eclipse.emf.emfstore.common.model")).addLogListener(new
 			ILogListener() {
 
 				public void logging(IStatus status, String plugin) {
 					if (status.getSeverity() == IStatus.INFO) {
 						System.out.println(status.getMessage());
 					} else if (!status.isOK()) {
 						System.err.println(status.getMessage());
 						final Throwable exception = status.getException();
 						if (exception != null) {
 							exception.printStackTrace(System.err);
 						}
 					}
 				}
 
 			});
 	}
 
 	private void handleStartupListener() {
 		final String property = ServerConfiguration.getProperties().getProperty(
 			ServerConfiguration.LOAD_STARTUP_LISTENER,
 			ServerConfiguration.LOAD_STARTUP_LISTENER_DEFAULT);
 		if (ServerConfiguration.TRUE.equals(property)) {
 			ModelUtil.logInfo("Notifying startup listener");
 			for (final StartupListener listener : ServerConfiguration.getStartupListeners()) {
 				listener.startedUp(serverSpace.getProjects());
 			}
 		}
 	}
 
 	private void handlePostStartupListener() {
 		final String property = ServerConfiguration.getProperties().getProperty(
 			ServerConfiguration.LOAD_POST_STARTUP_LISTENER, ServerConfiguration.LOAD_STARTUP_LISTENER_DEFAULT);
 		if (ServerConfiguration.TRUE.equals(property)) {
 			ModelUtil.logInfo("Notifying post startup listener");
 			for (final PostStartupListener listener : ServerConfiguration.getPostStartupListeners()) {
 				listener.postStartUp(serverSpace, accessControl, connectionHandlers);
 			}
 		}
 	}
 
 	private void copyFileToWorkspace(String target, String source, String failure, String success) {
 
 		final File targetFile = new File(target);
 
 		if (!targetFile.exists()) {
 			// check if the custom configuration resources are provided and if,
 			// copy them to place
 			final ESExtensionPoint extensionPoint = new ESExtensionPoint(
 				"org.eclipse.emf.emfstore.server.configurationResource");
 			final ESExtensionElement element = extensionPoint.getFirst();
 
 			if (element != null) {
 
 				final String attribute = element.getAttribute(targetFile.getName());
 
 				if (attribute != null) {
 					try {
 						FileUtil.copyFile(new URL("platform:/plugin/"
 							+ element.getIConfigurationElement().getNamespaceIdentifier() + "/" + attribute)
 							.openConnection().getInputStream(), targetFile);
 						return;
 					} catch (final IOException e) {
 						ModelUtil.logWarning("Copy of file from " + source + " to " + target + " failed", e);
 					}
 				}
 			}
 			// Guess not, lets copy the default configuration resources
 			try {
 				FileUtil.copyFile(getClass().getResourceAsStream(source), targetFile);
 			} catch (final IOException e) {
 				ModelUtil.logWarning("Copy of file from " + source + " to " + target + " failed", e);
 			}
 		}
 
 	}
 
 	private Set<ConnectionHandler<? extends EMFStoreInterface>> initConnectionHandlers() throws FatalESException {
 		final Set<ConnectionHandler<? extends EMFStoreInterface>> connectionHandlers = new LinkedHashSet<ConnectionHandler<? extends EMFStoreInterface>>();
 
 		// crate XML RPC connection handlers
 		final XmlRpcConnectionHandler xmlRpcConnectionHander = new XmlRpcConnectionHandler();
 		xmlRpcConnectionHander.init(emfStore, accessControl);
 		connectionHandlers.add(xmlRpcConnectionHander);
 
 		final XmlRpcAdminConnectionHandler xmlRpcAdminConnectionHander = new XmlRpcAdminConnectionHandler();
 		xmlRpcAdminConnectionHander.init(adminEmfStore, accessControl);
 		connectionHandlers.add(xmlRpcAdminConnectionHander);
 
 		return connectionHandlers;
 	}
 
 	private ServerSpace initServerSpace() throws FatalESException {
 		final ResourceStorage storage = initStorage();
 		final URI resourceUri = storage.init(properties);
 		final ResourceSet resourceSet = new ResourceSetImpl();
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
 		} catch (final IOException e) {
 			throw new FatalESException(StorageException.NOLOAD, e);
 		}
 
 		ServerSpace result = null;
 		final EList<EObject> contents = resource.getContents();
 		for (final EObject content : contents) {
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
 			} catch (final IOException e) {
 				throw new FatalESException(StorageException.NOSAVE, e);
 			}
 		}
 
 		return result;
 	}
 
 	private void validateServerSpace(Resource resource) throws FatalESException {
 		final EList<EObject> contents = resource.getContents();
 		for (final EObject object : contents) {
 			if (object instanceof ServerSpace) {
 				final EmfStoreValidator emfStoreValidator = new EmfStoreValidator((ServerSpace) object);
 				final String[] excludedProjects = ServerConfiguration.getSplittedProperty(
 					ServerConfiguration.VALIDATION_PROJECT_EXCLUDE,
 					ServerConfiguration.VALIDATION_PROJECT_EXCLUDE_DEFAULT);
 				emfStoreValidator.setExcludedProjects(Arrays.asList(excludedProjects));
 				try {
 					final String level = ServerConfiguration.getProperties().getProperty(
 						ServerConfiguration.VALIDATION_LEVEL, ServerConfiguration.VALIDATION_LEVEL_DEFAULT);
 					emfStoreValidator.validate(Integer.parseInt(level));
 				} catch (final NumberFormatException e) {
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
 	public static EMFStoreController getInstance() {
 		return instance;
 	}
 
 	private ResourceStorage initStorage() throws FatalESException {
 		final String className = properties.getProperty(ServerConfiguration.RESOURCE_STORAGE,
 			ServerConfiguration.RESOURCE_STORAGE_DEFAULT);
 
 		ResourceStorage resourceStorage;
 		final String failMessage = "Failed loading ressource storage!";
 		try {
 			ModelUtil.logInfo("Using RessourceStorage \"" + className + "\".");
 			resourceStorage = (ResourceStorage) Class.forName(className).getConstructor().newInstance();
 			return resourceStorage;
 		} catch (final IllegalArgumentException e) {
 			ModelUtil.logException(failMessage, e);
 			throw new FatalESException(failMessage, e);
 		} catch (final SecurityException e) {
 			ModelUtil.logException(failMessage, e);
 			throw new FatalESException(failMessage, e);
 		} catch (final InstantiationException e) {
 			ModelUtil.logException(failMessage, e);
 			throw new FatalESException(failMessage, e);
 		} catch (final IllegalAccessException e) {
 			ModelUtil.logException(failMessage, e);
 			throw new FatalESException(failMessage, e);
 		} catch (final InvocationTargetException e) {
 			ModelUtil.logException(failMessage, e);
 			throw new FatalESException(failMessage, e);
 		} catch (final NoSuchMethodException e) {
 			ModelUtil.logException(failMessage, e);
 			throw new FatalESException(failMessage, e);
 		} catch (final ClassNotFoundException e) {
 			ModelUtil.logException(failMessage, e);
 			throw new FatalESException(failMessage, e);
 		}
 	}
 
 	private AccessControlImpl initAccessControl(ServerSpace serverSpace) throws FatalESException {
 		setSuperUser(serverSpace);
 		return new AccessControlImpl(serverSpace);
 	}
 
 	private void setSuperUser(ServerSpace serverSpace) throws FatalESException {
 		final String superuser = ServerConfiguration.getProperties().getProperty(ServerConfiguration.SUPER_USER,
 			ServerConfiguration.SUPER_USER_DEFAULT);
 		for (final ACUser user : serverSpace.getUsers()) {
 			if (user.getName().equals(superuser)) {
 				return;
 			}
 		}
 		final ACUser superUser = AccesscontrolFactory.eINSTANCE.createACUser();
 		superUser.setName(superuser);
 		superUser.setFirstName("super");
 		superUser.setLastName("user");
 		superUser.setDescription("default server admin (superuser)");
 		superUser.getRoles().add(RolesFactory.eINSTANCE.createServerAdmin());
 		serverSpace.getUsers().add(superUser);
 		try {
 			serverSpace.save();
 		} catch (final IOException e) {
 			throw new FatalESException(StorageException.NOSAVE, e);
 		}
 		ModelUtil.logInfo("added superuser " + superuser);
 	}
 
 	private Properties initProperties() {
 		final File propertyFile = new File(ServerConfiguration.getConfFile());
 		final Properties properties = new Properties();
 		FileInputStream fis = null;
 		try {
 			fis = new FileInputStream(propertyFile);
 			properties.load(fis);
 			ServerConfiguration.setProperties(properties, false);
 			ModelUtil.logInfo("Property file read. (" + propertyFile.getAbsolutePath() + ")");
 		} catch (final IOException e) {
 			ModelUtil.logWarning("Property initialization failed, using default properties.", e);
 		} finally {
 			try {
 				if (fis != null) {
 					fis.close();
 				}
 			} catch (final IOException e) {
 				ModelUtil.logWarning("Closing of properties file failed.", e);
 			}
 		}
 
 		return properties;
 	}
 
 	/**
 	 * Stops the EMFStore gracefully.
 	 */
 	public void stopServer() {
 		wakeForTermination();
 		// connection handlers may be null in case an exception has been thrown
 		// while starting
 		if (connectionHandlers != null) {
 
 			final Object monitor = MonitorProvider.getInstance().getMonitor();
 
 			synchronized (monitor) {
 				for (final ConnectionHandler<? extends EMFStoreInterface> handler : connectionHandlers) {
 					handler.stop();
 				}
 			}
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
 	 */
 	public void shutdown(FatalESException exception) {
 		ModelUtil.logWarning("Stopping all connection handlers...");
 		if (connectionHandlers != null) {
 			for (final ConnectionHandler<? extends EMFStoreInterface> handler : connectionHandlers) {
 				ModelUtil.logWarning("Stopping connection handler \"" + handler.getName() + "\".");
 				handler.stop();
 				ModelUtil.logWarning("Connection handler \"" + handler.getName() + "\" stopped.");
 			}
 		}
 		ModelUtil.logException("Server was forcefully stopped.", exception);
 		ModelUtil.logException("Cause for server shutdown: ", exception.getCause());
 		wakeForTermination();
 	}
 
 	private synchronized void waitForTermination() {
 		try {
 			wait();
 		} catch (final InterruptedException e) {
 			ModelUtil.logWarning("Waiting for termination was interrupted", e);
 		}
 	}
 
 	private synchronized void wakeForTermination() {
 		notify();
 	}
 
 	private void serverHeader() {
 		final InputStream inputStream = getClass().getResourceAsStream("emfstore.txt");
 		final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
 		String line;
 		try {
 			while ((line = reader.readLine()) != null) {
 				System.out.println(line);
 			}
 		} catch (final IOException e) {
 			// ignore
 		} finally {
 			try {
 				reader.close();
 				inputStream.close();
 			} catch (final IOException e) {
 				// ignore
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see java.lang.Runnable#run()
 	 */
 	public void run() {
 		try {
 			run(false);
 		} catch (final FatalESException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Starts the server in a new thread.
 	 * 
 	 * @return an controller for the running EMFStore
 	 * @throws FatalESException
 	 *             in case of failure
 	 */
 	public static EMFStoreController runAsNewThread() throws FatalESException {
 		final EMFStoreController emfStoreController = new EMFStoreController();
 		final Thread thread = new Thread(emfStoreController);
 		thread.start();
 		try {
 			thread.join();
 		} catch (final InterruptedException e) {
 			throw new FatalESException(e);
 		}
 		return emfStoreController;
 	}
 
 	/**
 	 * Returns the {@link ServerSpace}.
 	 * 
 	 * @return the server space
 	 */
 	public ServerSpace getServerSpace() {
 		return serverSpace;
 	}
 
 	/**
 	 * Returns the {@link AccessControl} component of the EMFStore controller.
 	 * 
 	 * @return the {@link AccessControl} component
 	 */
 	public AccessControl getAccessControl() {
 		return accessControl;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.equinox.app.IApplication#stop()
 	 */
 	public void stop() {
 		stopServer();
 	}
 
 }
