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
 package org.eclipse.emf.emfstore.client.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.emfstore.client.model.changeTracking.commands.EMFStoreBasicCommandStack;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.AdminConnectionManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.ConnectionManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.KeyStoreManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.SessionManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.xmlrpc.XmlRpcAdminConnectionManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.xmlrpc.XmlRpcConnectionManager;
 import org.eclipse.emf.emfstore.client.model.impl.WorkspaceImpl;
 import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.client.model.util.EditingDomainProvider;
 import org.eclipse.emf.emfstore.client.model.util.WorkspaceUtil;
 import org.eclipse.emf.emfstore.common.CommonUtil;
 import org.eclipse.emf.emfstore.common.IReinitializable;
 import org.eclipse.emf.emfstore.common.ResourceFactoryRegistry;
 import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionElement;
 import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionPoint;
 import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionPointException;
 import org.eclipse.emf.emfstore.common.model.ModelVersion;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.util.FileUtil;
 import org.eclipse.emf.emfstore.common.model.util.MalformedModelVersionException;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.common.observer.IObserver;
 import org.eclipse.emf.emfstore.common.observer.ObserverBus;
 import org.eclipse.emf.emfstore.migration.EMFStoreMigrationException;
 import org.eclipse.emf.emfstore.migration.EMFStoreMigratorUtil;
 
 /**
  * Controller for workspaces. Workspace Manager is a singleton.
  * 
  * @author Maximilian Koegel
  * @generated NOT
  */
 public final class WorkspaceManager implements IReinitializable {
 
 	private static WorkspaceManager instance;
 
 	private Workspace currentWorkspace;
 	private SessionManager sessionManager;
 	private ObserverBus observerBus;
 
 	private ConnectionManager connectionManager;
 	private AdminConnectionManager adminConnectionManager;
 
 	private ResourceSet resourceSet;
 
 	/**
 	 * Get an instance of the workspace manager. Will create an instance if no
 	 * workspace manager is present.
 	 * 
 	 * @return the workspace manager singleton
 	 * @generated NOT
 	 */
 	public static synchronized WorkspaceManager getInstance() {
 		if (instance == null) {
 			try {
 				instance = new WorkspaceManager();
 				instance.initialize();
 				// BEGIN SUPRESS CATCH EXCEPTION
 			} catch (RuntimeException e) {
 				// END SURPRESS CATCH EXCEPTION
 				ModelUtil.logException("Workspace Initialization failed, shutting down", e);
 				throw e;
 			}
 
 			// init ecore packages
 			CommonUtil.getAllModelElementEClasses();
 
 			// notify post workspace observers
 			instance.notifyPostWorkspaceInitiators();
 		}
 		return instance;
 	}
 
 	/**
 	 * Initialize the Workspace Manager singleton.
 	 */
 	public static synchronized void init() {
 		getInstance();
 	}
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @generated NOT
 	 */
 	private WorkspaceManager() {
 	}
 
 	private void initialize() {
 		initializeObserverBus();
 		this.connectionManager = initConnectionManager();
 		this.adminConnectionManager = initAdminConnectionManager();
 		this.sessionManager = new SessionManager();
 		reinit();
 	}
 
 	private void initializeObserverBus() {
 		this.observerBus = new ObserverBus();
 
 		for (ExtensionElement element : new ExtensionPoint("org.eclipse.emf.emfstore.client.observers", true)
 			.getExtensionElements()) {
 			try {
 				observerBus.register(element.getClass("ObserverClass", IObserver.class));
 			} catch (ExtensionPointException e) {
 				WorkspaceUtil.logException(e.getMessage(), e);
 			}
 		}
 	}
 
 	private void notifyPostWorkspaceInitiators() {
 		for (ExtensionElement element : new ExtensionPoint("org.eclipse.emf.emfstore.client.notify.postinit", true)
 			.getExtensionElements()) {
 			try {
 				element.getClass("class", PostWorkspaceInitiator.class).workspaceInitComplete(currentWorkspace);
 			} catch (ExtensionPointException e) {
 				WorkspaceUtil.logException(e.getMessage(), e);
 			}
 		}
 	}
 
 	/**
 	 * Initialize the connection manager of the workspace. The connection
 	 * manager connects the workspace with the emf store.
 	 * 
 	 * @return the connection manager
 	 * @generated NOT
 	 */
 	private ConnectionManager initConnectionManager() {
 		KeyStoreManager.getInstance().setupKeys();
 		// return new RMIConnectionManagerImpl();
 		return new XmlRpcConnectionManager();
 	}
 
 	/**
 	 * Initialize the connection manager of the workspace. The connection
 	 * manager connects the workspace with the emf store.
 	 * 
 	 * @return the admin connection manager
 	 * @generated NOT
 	 */
 	private AdminConnectionManager initAdminConnectionManager() {
 		// return new RMIAdminConnectionManagerImpl();
 		return new XmlRpcAdminConnectionManager();
 	}
 
 	/**
 	 * (Re-)Initializes the workspace. Loads workspace from persistent storage if
 	 * present. There is always one current Workspace.
 	 */
 	public void reinit() {
 
 		if (!isDisposed()) {
 			return;
 		}
 
 		resourceSet = new ResourceSetImpl();
 		resourceSet.setResourceFactoryRegistry(new ResourceFactoryRegistry());
 		((ResourceSetImpl) resourceSet).setURIResourceMap(new HashMap<URI, Resource>());
 		resourceSet.getLoadOptions().putAll(ModelUtil.getResourceLoadOptions());
 
 		// register an editing domain on the resource
 		Configuration.setEditingDomain(createEditingDomain(resourceSet));
 
 		URI fileURI = URI.createFileURI(Configuration.getWorkspacePath());
 		File workspaceFile = new File(Configuration.getWorkspacePath());
 		final Workspace workspace;
 		final Resource resource;
 		if (!workspaceFile.exists()) {
 
 			workspace = createNewWorkspace(resourceSet, fileURI);
 
 		} else {
 			// file exists load it
 			// check if a migration is needed
 			migrateModel(resourceSet);
 
 			// resource = resourceSet.getResource(fileURI, true);
 			resource = resourceSet.createResource(fileURI);
 
 			try {
 				resource.load(ModelUtil.getResourceLoadOptions());
 			} catch (IOException e) {
 				WorkspaceUtil.logException("Error while loading workspace.", e);
 			}
 
 			EList<EObject> directContents = resource.getContents();
 			// MK cast
 			workspace = (Workspace) directContents.get(0);
 		}
 
 		workspace.setConnectionManager(this.connectionManager);
 		workspace.setResourceSet(resourceSet);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				workspace.init();
 			}
 		}.run(true);
 
 		currentWorkspace = workspace;
 
 	}
 
 	private EditingDomain createEditingDomain(ResourceSet resourceSet) {
 		EditingDomainProvider domainProvider = getDomainProvider();
 		if (domainProvider != null) {
 			return domainProvider.getEditingDomain(resourceSet);
 		} else {
 			AdapterFactoryEditingDomain domain = new AdapterFactoryEditingDomain(new ComposedAdapterFactory(
 				ComposedAdapterFactory.Descriptor.Registry.INSTANCE), new EMFStoreBasicCommandStack(), resourceSet);
 			resourceSet.eAdapters().add(new AdapterFactoryEditingDomain.EditingDomainProvider(domain));
 			return domain;
 		}
 	}
 
 	private EditingDomainProvider getDomainProvider() {
 		// TODO EXPT PRIO
 		return new ExtensionPoint("org.eclipse.emf.emfstore.client.editingDomainProvider").getClass("class",
 			EditingDomainProvider.class);
 	}
 
 	private Workspace createNewWorkspace(ResourceSet resourceSet, URI fileURI) {
 		final Workspace workspace;
 		final Resource resource;
 		// no workspace content found, create a workspace
 		resource = resourceSet.createResource(fileURI);
 		workspace = ModelFactory.eINSTANCE.createWorkspace();
 		workspace.getServerInfos().addAll(Configuration.getDefaultServerInfos());
 		EList<Usersession> usersessions = workspace.getUsersessions();
 		for (ServerInfo serverInfo : workspace.getServerInfos()) {
 			Usersession lastUsersession = serverInfo.getLastUsersession();
 			if (lastUsersession != null) {
 				usersessions.add(lastUsersession);
 			}
 		}
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				resource.getContents().add(workspace);
 			}
 		}.run(true);
 
 		try {
			resource.save(Configuration.getResourceSaveOptions());
 		} catch (IOException e) {
 			WorkspaceUtil.logException(
 				"Creating new workspace failed! Delete workspace folder: " + Configuration.getWorkspaceDirectory(), e);
 		}
 		int modelVersionNumber;
 		try {
 			modelVersionNumber = ModelUtil.getModelVersionNumber();
 			stampCurrentVersionNumber(modelVersionNumber);
 		} catch (MalformedModelVersionException e1) {
 			WorkspaceUtil.logException("Loading model version failed!", e1);
 		}
 		return workspace;
 	}
 
 	private void stampCurrentVersionNumber(int modelReleaseNumber) {
 		URI versionFileUri = URI.createFileURI(Configuration.getModelReleaseNumberFileName());
 		Resource versionResource = new ResourceSetImpl().createResource(versionFileUri);
 		ModelVersion modelVersion = org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE.createModelVersion();
 		modelVersion.setReleaseNumber(modelReleaseNumber);
 		versionResource.getContents().add(modelVersion);
 		try {
			versionResource.save(Configuration.getResourceSaveOptions());
 		} catch (IOException e) {
 			WorkspaceUtil.logException(
 				"Version stamping workspace failed! Delete workspace folder: " + Configuration.getWorkspaceDirectory(),
 				e);
 		}
 	}
 
 	private void migrateModel(ResourceSet resourceSet) {
 		ModelVersion workspaceModelVersion = getWorkspaceModelVersion();
 		int modelVersionNumber;
 		try {
 			modelVersionNumber = ModelUtil.getModelVersionNumber();
 			stampCurrentVersionNumber(modelVersionNumber);
 		} catch (MalformedModelVersionException e1) {
 			WorkspaceUtil.logException("Loading model version failed, migration skipped!", e1);
 			return;
 		}
 		if (workspaceModelVersion.getReleaseNumber() == modelVersionNumber) {
 			return;
 		} else if (workspaceModelVersion.getReleaseNumber() > modelVersionNumber) {
 			backupAndRecreateWorkspace(resourceSet);
 			WorkspaceUtil.logException("Model conforms to a newer version, update client! New workspace was backuped!",
 				new IllegalStateException());
 			return;
 		}
 
 		// we need to migrate
 		if (!EMFStoreMigratorUtil.isMigratorAvailable()) {
 			WorkspaceUtil.logException("Model requires migration, but no migrators are registered!",
 				new IllegalStateException());
 			return;
 		}
 
 		backupWorkspace(false);
 		File workspaceFile = new File(Configuration.getWorkspaceDirectory());
 		for (File file : workspaceFile.listFiles()) {
 			if (file.getName().startsWith(Configuration.getProjectSpaceDirectoryPrefix())) {
 				String projectFilePath = file.getAbsolutePath() + File.separatorChar
 					+ Configuration.getProjectFolderName() + File.separatorChar + 0
 					+ Configuration.getProjectFragmentFileExtension();
 				URI projectURI = URI.createFileURI(projectFilePath);
 				String operationsFilePath = null;
 				File[] listFiles = file.listFiles();
 				if (listFiles == null) {
 					WorkspaceUtil.logException("The migration of the project in projectspace at " + projectFilePath
 						+ " failed!", new IllegalStateException("Broken projectSpace!"));
 					continue;
 				}
 				for (File subDirFile : listFiles) {
 					if (subDirFile.getName().endsWith(Configuration.getLocalChangePackageFileExtension())) {
 						operationsFilePath = subDirFile.getAbsolutePath();
 					}
 				}
 				if (operationsFilePath == null) {
 					WorkspaceUtil.logException("The migration of the project in projectspace at " + projectFilePath
 						+ " failed!", new IllegalStateException("Broken workspace!"));
 					backupAndRecreateWorkspace(resourceSet);
 				}
 				URI operationsURI = URI.createFileURI(operationsFilePath);
 				try {
 					migrate(projectURI, operationsURI, workspaceModelVersion.getReleaseNumber());
 				} catch (EMFStoreMigrationException e) {
 					WorkspaceUtil.logException("The migration of the project in projectspace at " + projectFilePath
 						+ " failed!", e);
 					backupAndRecreateWorkspace(resourceSet);
 				}
 			}
 		}
 
 		stampCurrentVersionNumber(modelVersionNumber);
 	}
 
 	public void migrate(String absoluteFilename) {
 		URI projectURI = URI.createFileURI(absoluteFilename);
 
 		List<URI> modelURIs = new ArrayList<URI>();
 		modelURIs.add(projectURI);
 
 		ModelVersion workspaceModelVersion = getWorkspaceModelVersion();
 		if (!EMFStoreMigratorUtil.isMigratorAvailable()) {
 			ModelUtil.logWarning("No Migrator available to migrate imported file");
 			return;
 		}
 
 		try {
 			EMFStoreMigratorUtil.getEMFStoreMigrator().migrate(modelURIs, workspaceModelVersion.getReleaseNumber() - 1,
 				new NullProgressMonitor());
 		} catch (EMFStoreMigrationException e) {
 			WorkspaceUtil.logWarning("The migration of the project in the file " + absoluteFilename + " failed!", e);
 		}
 	}
 
 	private void backupAndRecreateWorkspace(ResourceSet resourceSet) {
 		backupWorkspace(true);
 		URI fileURI = URI.createFileURI(Configuration.getWorkspacePath());
 		createNewWorkspace(resourceSet, fileURI);
 	}
 
 	private void backupWorkspace(boolean move) {
 		String workspaceDirectory = Configuration.getWorkspaceDirectory();
 		File workspacePath = new File(workspaceDirectory);
 
 		// TODO: if you want the date included in the backup folder you should
 		// change the format. the default format
 		// does not work with every os due to : and other characters.
 		String newWorkspaceDirectory = Configuration.getLocationProvider().getBackupDirectory() + "emfstore_backup_"
 			+ System.currentTimeMillis();
 
 		File workspacebackupPath = new File(newWorkspaceDirectory);
 		if (move) {
 			workspacePath.renameTo(workspacebackupPath);
 		} else {
 			try {
 				FileUtil.copyDirectory(workspacePath, workspacebackupPath);
 			} catch (IOException e) {
 				WorkspaceUtil.logException("Workspace backup failed!", e);
 			}
 		}
 	}
 
 	private ModelVersion getWorkspaceModelVersion() {
 		// check for legacy workspace
 		File versionFile = new File(Configuration.getModelReleaseNumberFileName());
 		if (!versionFile.exists()) {
 			int modelVersionNumber;
 			try {
 				modelVersionNumber = ModelUtil.getModelVersionNumber();
 				stampCurrentVersionNumber(modelVersionNumber);
 			} catch (MalformedModelVersionException e1) {
 				WorkspaceUtil.logException("Loading model version failed!", e1);
 			}
 		}
 
 		// check if we need to migrate
 		URI versionFileUri = URI.createFileURI(Configuration.getModelReleaseNumberFileName());
 		ResourceSet resourceSet = new ResourceSetImpl();
 		try {
 			Resource resource = resourceSet.getResource(versionFileUri, true);
 			EList<EObject> directContents = resource.getContents();
 			ModelVersion modelVersion = (ModelVersion) directContents.get(0);
 			return modelVersion;
 			// BEGIN SUPRESS CATCH EXCEPTION
 		} catch (RuntimeException e) {
 			// END SUPRESS CATCH EXCEPTION
 			// resource can not be loaded, assume version number before
 			// metamodel split
 			ModelVersion modelVersion = org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE
 				.createModelVersion();
 			modelVersion.setReleaseNumber(4);
 			return modelVersion;
 		}
 	}
 
 	/**
 	 * Migrate the model instance if neccessary.
 	 * 
 	 * @param projectURI
 	 *            the uri of the project state
 	 * @param changesURI
 	 *            the uri of the local changes of the project state
 	 * @param sourceModelReleaseNumber
 	 * @throws EMFStoreMigrationException
 	 */
 	private void migrate(URI projectURI, URI changesURI, int sourceModelReleaseNumber)
 		throws EMFStoreMigrationException {
 		List<URI> modelURIs = new ArrayList<URI>();
 		modelURIs.add(projectURI);
 		modelURIs.add(changesURI);
 		EMFStoreMigratorUtil.getEMFStoreMigrator().migrate(modelURIs, sourceModelReleaseNumber,
 			new NullProgressMonitor());
 	}
 
 	/**
 	 * Get the current workspace. There is always one current workspace.
 	 * 
 	 * @return the workspace
 	 */
 	public Workspace getCurrentWorkspace() {
 		return currentWorkspace;
 	}
 
 	/**
 	 * Get the connection manager. Return the connection manager for this
 	 * workspace.
 	 * 
 	 * @return the connectionManager
 	 */
 	public ConnectionManager getConnectionManager() {
 		return connectionManager;
 	}
 
 	/**
 	 * Set the connectionmanager.
 	 * 
 	 * @param manager
 	 *            connection manager.
 	 */
 	public void setConnectionManager(ConnectionManager manager) {
 		connectionManager = manager;
 	}
 
 	/**
 	 * Get the admin connection manager. Return the admin connection manager for
 	 * this workspace.
 	 * 
 	 * @return the connectionManager
 	 */
 	public AdminConnectionManager getAdminConnectionManager() {
 		return adminConnectionManager;
 	}
 
 	/**
 	 * Retrieve the project space for a model element.
 	 * 
 	 * @param modelElement
 	 *            the model element
 	 * @return the project space
 	 */
 	public static ProjectSpace getProjectSpace(EObject modelElement) {
 
 		if (modelElement == null) {
 			throw new IllegalArgumentException("The model element is null");
 		} else if (modelElement instanceof ProjectSpace) {
 			return (ProjectSpace) modelElement;
 		}
 
 		Project project = ModelUtil.getProject(modelElement);
 
 		if (project == null) {
 			throw new IllegalArgumentException("The model element " + modelElement + " has no project");
 		}
 		return getProjectSpace(project);
 	}
 
 	/**
 	 * Retrieve the project space for a project.
 	 * 
 	 * @param project
 	 *            the project
 	 * @return the project space
 	 */
 	public static ProjectSpace getProjectSpace(Project project) {
 		if (project == null) {
 			throw new IllegalArgumentException("The project is null");
 		}
 		// check if my container is a project space
 		if (ModelPackage.eINSTANCE.getProjectSpace().isInstance(project.eContainer())) {
 			return (ProjectSpace) project.eContainer();
 		} else {
 			throw new IllegalStateException("Project is not contained by any project space");
 		}
 	}
 
 	/**
 	 * Returns the {@link ObserverBus}.
 	 * 
 	 * @return observer bus
 	 */
 	public static ObserverBus getObserverBus() {
 		return getInstance().observerBus;
 	}
 
 	/**
 	 * Returns the {@link SessionManager}.
 	 * 
 	 * @return session manager
 	 */
 	public SessionManager getSessionManager() {
 		return sessionManager;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.IDisposable#dispose()
 	 */
 	public void dispose() {
 		if (currentWorkspace != null) {
 			((WorkspaceImpl) currentWorkspace).dispose();
 			currentWorkspace = null;
 			instance = null;
 		}
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.IReinitializable#isDisposed()
 	 */
 	public boolean isDisposed() {
 		return currentWorkspace == null;
 	}
 }
