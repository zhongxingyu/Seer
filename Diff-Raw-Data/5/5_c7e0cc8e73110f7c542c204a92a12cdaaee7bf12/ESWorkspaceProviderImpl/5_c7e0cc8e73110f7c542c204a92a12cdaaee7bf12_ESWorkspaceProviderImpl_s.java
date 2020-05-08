 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Maximilian Koegel - initial API and implementation
  * Johannes Faltermeier - EMFStore specific URI migration
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 import org.eclipse.emf.emfstore.client.ESLocalProject;
 import org.eclipse.emf.emfstore.client.ESWorkspaceProvider;
 import org.eclipse.emf.emfstore.client.observer.ESCheckoutObserver;
 import org.eclipse.emf.emfstore.client.observer.ESCommitObserver;
 import org.eclipse.emf.emfstore.client.observer.ESShareObserver;
 import org.eclipse.emf.emfstore.client.observer.ESUpdateObserver;
 import org.eclipse.emf.emfstore.client.observer.ESWorkspaceInitObserver;
 import org.eclipse.emf.emfstore.client.provider.ESEditingDomainProvider;
 import org.eclipse.emf.emfstore.client.sessionprovider.ESAbstractSessionProvider;
 import org.eclipse.emf.emfstore.client.util.ESClientURIUtil;
 import org.eclipse.emf.emfstore.client.util.RunESCommand;
 import org.eclipse.emf.emfstore.common.ESResourceSetProvider;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionElement;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionPoint;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionPointException;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESPriorityComparator;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.commands.EMFStoreBasicCommandStack;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.AdminConnectionManager;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.ConnectionManager;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.KeyStoreManager;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.SessionManager;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.xmlrpc.XmlRpcAdminConnectionManager;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.xmlrpc.XmlRpcConnectionManager;
 import org.eclipse.emf.emfstore.internal.client.model.impl.WorkspaceImpl;
 import org.eclipse.emf.emfstore.internal.client.model.impl.api.ESWorkspaceImpl;
 import org.eclipse.emf.emfstore.internal.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.internal.client.model.util.WorkspaceUtil;
 import org.eclipse.emf.emfstore.internal.client.provider.ClientXMIResourceSetProvider;
 import org.eclipse.emf.emfstore.internal.client.startup.ClientHrefMigrator;
import org.eclipse.emf.emfstore.internal.common.CommonUtil;
 import org.eclipse.emf.emfstore.internal.common.ESDisposable;
 import org.eclipse.emf.emfstore.internal.common.model.Project;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.common.observer.ObserverBus;
 import org.eclipse.emf.emfstore.internal.migration.EMFStoreMigrationException;
 import org.eclipse.emf.emfstore.internal.migration.EMFStoreMigrator;
 import org.eclipse.emf.emfstore.internal.migration.EMFStoreMigratorUtil;
 import org.eclipse.emf.emfstore.server.model.ESChangePackage;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec;
 
 /**
  * Controller for workspaces. Workspace Manager is a singleton.
  * 
  * @author mkoegel
  * @author jfaltermeier
  */
 public final class ESWorkspaceProviderImpl implements ESWorkspaceProvider, ESCommitObserver, ESUpdateObserver,
 	ESShareObserver, ESCheckoutObserver, ESDisposable {
 
 	private static ESWorkspaceProviderImpl instance;
 
 	private AdminConnectionManager adminConnectionManager;
 	private ConnectionManager connectionManager;
 	private EditingDomain editingDomain;
 	private ObserverBus observerBus;
 	private ResourceSet resourceSet;
 	private SessionManager sessionManager;
 	private Workspace currentWorkspace;
 
 	/**
 	 * Get an instance of the workspace manager. Will create an instance if no
 	 * workspace manager is present.
 	 * 
 	 * @return the workspace manager singleton
 	 * @generated NOT
 	 */
 	public static synchronized ESWorkspaceProviderImpl getInstance() {
 		if (instance == null) {
 			try {
 				instance = new ESWorkspaceProviderImpl();
 				instance.initialize();
 				// BEGIN SUPRESS CATCH EXCEPTION
 			} catch (final RuntimeException e) {
 				// END SURPRESS CATCH EXCEPTION
 				ModelUtil.logException(Messages.ESWorkspaceProviderImpl_WorkspaceInit_Failed, e);
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
 	 * Retrieve the editing domain.
 	 * 
 	 * @return the workspace editing domain
 	 */
 	public EditingDomain getEditingDomain() {
 		if (editingDomain == null) {
 			ESWorkspaceProviderImpl.getInstance();
 		}
 		return editingDomain;
 	}
 
 	/**
 	 * Sets the EditingDomain.
 	 * 
 	 * @param editingDomain
 	 *            new domain.
 	 */
 	public void setEditingDomain(EditingDomain editingDomain) {
 		this.editingDomain = editingDomain;
 	}
 
 	/**
 	 * Private constructor.
 	 * 
 	 */
 	private ESWorkspaceProviderImpl() {
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.common.ESDisposable#dispose()
 	 */
 	public void dispose() {
 		if (currentWorkspace != null) {
 			RunESCommand.run(new Callable<Void>() {
 				public Void call() throws Exception {
 					((WorkspaceImpl) currentWorkspace).dispose();
 					return null;
 				}
 			});
 			currentWorkspace = null;
 		}
 	}
 
 	/**
 	 * Whether the current workspace is disposed.
 	 * 
 	 * @return {@code true} if the current workspace is disposed, {@code false} otherwise
 	 */
 	public boolean isDisposed() {
 		return currentWorkspace == null;
 	}
 
 	/**
 	 * (Re-)Initializes the workspace. Loads workspace from persistent storage
 	 * if present. There is always one current Workspace.
 	 */
 	public void load() {
 		final ESExtensionPoint extensionPoint = new ESExtensionPoint(
 			"org.eclipse.emf.emfstore.client.resourceSetProvider", //$NON-NLS-1$
 			true, new ESPriorityComparator("priority", true)); //$NON-NLS-1$
 
 		final ESResourceSetProvider resourceSetProvider = extensionPoint.getElementWithHighestPriority().getClass(
 			"class", //$NON-NLS-1$
 			ESResourceSetProvider.class);
 
 		resourceSet = resourceSetProvider.getResourceSet();
 
 		// register an editing domain on the resource
 		setEditingDomain(createEditingDomain(resourceSet));
 
 		final URI workspaceURI = ESClientURIUtil.createWorkspaceURI();
 		final Workspace workspace;
 		final Resource resource;
 
 		if (!resourceSet.getURIConverter().exists(workspaceURI, null)) {
 			workspace = createNewWorkspace(resourceSet, workspaceURI);
 		} else {
 			// hrefs are persisted differently in 1.1+ in comparison to 1.0
 			// migrate, if needed, before loading
 			if (resourceSetProvider instanceof ClientXMIResourceSetProvider) {
 				if (!new ClientHrefMigrator().migrate()) {
 					throw new RuntimeException(Messages.ESWorkspaceProviderImpl_Migration_Failed);
 				}
 			}
 
 			// file exists, load it,
 			// check if a migration is needed
 			migrateModelIfNeeded(resourceSet);
 
 			resource = resourceSet.createResource(workspaceURI);
 
 			try {
 				resource.load(ModelUtil.getResourceLoadOptions());
 			} catch (final IOException e) {
 				WorkspaceUtil.logException(Messages.ESWorkspaceProviderImpl_Workspace_Loading_Failed, e);
 			}
 
 			final EList<EObject> directContents = resource.getContents();
 			workspace = (Workspace) directContents.get(0);
 		}
 
 		workspace.setResourceSet(resourceSet);
 
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				workspace.init();
 				return null;
 			}
 		});
 
 		currentWorkspace = workspace;
 
 		getObserverBus().register(this);
 	}
 
 	/**
 	 * Flushes the command stack.
 	 */
 	public void flushCommandStack() {
 		getEditingDomain().getCommandStack().flush();
 	}
 
 	public void migrate(String absoluteFilename) {
 		// FIXME JF
 		// final URI projectURI = URI.createFileURI(absoluteFilename);
 		//
 		// final List<URI> modelURIs = new ArrayList<URI>();
 		// modelURIs.add(projectURI);
 		//
 		// final ModelVersion workspaceModelVersion = getWorkspaceModelVersion();
 		// if (!EMFStoreMigratorUtil.isMigratorAvailable()) {
 		// ModelUtil.logWarning("No Migrator available to migrate imported file");
 		// return;
 		// }
 		//
 		// try {
 		// EMFStoreMigratorUtil.getEMFStoreMigrator().migrate(modelURIs, workspaceModelVersion.getReleaseNumber() - 1,
 		// new NullProgressMonitor());
 		// } catch (final EMFStoreMigrationException e) {
 		// WorkspaceUtil.logWarning("The migration of the project in the file " + absoluteFilename + " failed!", e);
 		// }
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
 	 * Set the admin connection manager.
 	 * 
 	 * @param adminConnectionManager
 	 *            the new {@link AdminConnectionManager} to be set
 	 */
 	public void setAdminConnectionManager(AdminConnectionManager adminConnectionManager) {
 		this.adminConnectionManager = adminConnectionManager;
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
 			throw new IllegalArgumentException(Messages.ESWorkspaceProviderImpl_ModelElement_Is_Null);
 		} else if (modelElement instanceof ProjectSpace) {
 			return (ProjectSpace) modelElement;
 		}
 
 		final Project project = ModelUtil.getProject(modelElement);
 
 		if (project == null) {
 			throw new IllegalArgumentException(MessageFormat.format(
 				Messages.ESWorkspaceProviderImpl_ModelElement_Has_No_Project, modelElement));
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
 			throw new IllegalArgumentException(Messages.ESWorkspaceProviderImpl_Project_Is_Null);
 		}
 		// check if my container is a project space
 		if (ModelPackage.eINSTANCE.getProjectSpace().isInstance(project.eContainer())) {
 			return (ProjectSpace) project.eContainer();
 		}
 
 		throw new IllegalStateException(Messages.ESWorkspaceProviderImpl_Project_Not_Contained_By_ProjectSpace);
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
 	 * Get the current workspace. There is always one current workspace.
 	 * 
 	 * @return the workspace
 	 */
 	public ESWorkspaceImpl getWorkspace() {
 		return getInternalWorkspace().toAPI();
 	}
 
 	public Workspace getInternalWorkspace() {
 
 		if (currentWorkspace == null) {
 			load();
 		}
 
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
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see ESWorkspaceProvider#setSessionProvider(ESAbstractSessionProvider)
 	 */
 	public void setSessionProvider(ESAbstractSessionProvider sessionProvider) {
 		getSessionManager().setSessionProvider(sessionProvider);
 	}
 
 	private void initialize() {
 		observerBus = new ObserverBus();
 		connectionManager = initConnectionManager();
 		adminConnectionManager = initAdminConnectionManager();
 		sessionManager = new SessionManager();
 		load();
 	}
 
 	private void notifyPostWorkspaceInitiators() {
 		for (final ESExtensionElement element : new ESExtensionPoint("org.eclipse.emf.emfstore.client.notify.postinit", //$NON-NLS-1$
 			true)
 			.getExtensionElements()) {
 			try {
 				element.getClass("class", ESWorkspaceInitObserver.class).workspaceInitComplete( //$NON-NLS-1$
 					currentWorkspace
 						.toAPI());
 			} catch (final ESExtensionPointException e) {
 				WorkspaceUtil.logException(e.getMessage(), e);
 			}
 		}
 	}
 
 	/**
 	 * Initialize the connection manager of the workspace. The connection
 	 * manager connects the workspace with EMFStore.
 	 * 
 	 * @return the connection manager
 	 */
 	private ConnectionManager initConnectionManager() {
 		KeyStoreManager.getInstance().setupKeys();
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
 		return new XmlRpcAdminConnectionManager();
 	}
 
 	private EditingDomain createEditingDomain(ResourceSet resourceSet) {
 		final ESEditingDomainProvider domainProvider = getDomainProvider();
 		if (domainProvider != null) {
 			return domainProvider.getEditingDomain(resourceSet);
 		}
 
 		AdapterFactory adapterFactory = new ComposedAdapterFactory(
 			ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
 		adapterFactory = new ComposedAdapterFactory(new AdapterFactory[] { adapterFactory,
 			new ReflectiveItemProviderAdapterFactory() });
 
 		final AdapterFactoryEditingDomain domain = new AdapterFactoryEditingDomain(adapterFactory,
 			new EMFStoreBasicCommandStack(), resourceSet);
 		resourceSet.eAdapters().add(new AdapterFactoryEditingDomain.EditingDomainProvider(domain));
 		return domain;
 	}
 
 	private ESEditingDomainProvider getDomainProvider() {
 		// TODO EXPT PRIO
 		return new ESExtensionPoint("org.eclipse.emf.emfstore.client.editingDomainProvider") //$NON-NLS-1$
 			.getClass("class", //$NON-NLS-1$
 				ESEditingDomainProvider.class);
 	}
 
 	private Workspace createNewWorkspace(ResourceSet resourceSet, URI fileURI) {
 		final Workspace workspace;
 		final Resource resource;
 		// no workspace content found, create a workspace
 		resource = resourceSet.createResource(fileURI);
 		workspace = ModelFactory.eINSTANCE.createWorkspace();
 		workspace.getServerInfos().addAll(Configuration.getClientBehavior().getDefaultServerInfos());
 		final EList<Usersession> usersessions = workspace.getUsersessions();
 		for (final ServerInfo serverInfo : workspace.getServerInfos()) {
 			final Usersession lastUsersession = serverInfo.getLastUsersession();
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
 			resource.save(ModelUtil.getResourceSaveOptions());
 		} catch (final IOException e) {
 			WorkspaceUtil.logException(
 				Messages.ESWorkspaceProviderImpl_Create_Workspace_Failed
 					+ Configuration.getFileInfo().getWorkspaceDirectory(), e);
 		}
 		return workspace;
 	}
 
 	private void migrateModelIfNeeded(ResourceSet resourceSet) {
 		// FIXME JF
 		EMFStoreMigrator migrator = null;
 		try {
 			migrator = EMFStoreMigratorUtil.getEMFStoreMigrator();
 		} catch (final EMFStoreMigrationException e2) {
 			WorkspaceUtil.logWarning(e2.getMessage(), null);
 			return;
 		}
 
 		for (final List<URI> curModels : getPhysicalURIsForMigration()) {
 			// TODO logging?
 			if (!migrator.canHandle(curModels)) {
 				return;
 			}
 
 			if (!migrator.needsMigration(curModels)) {
 				return;
 			}
 
 			try {
 				migrator.migrate(curModels, new NullProgressMonitor());
 			} catch (final EMFStoreMigrationException e) {
 				WorkspaceUtil.logException(MessageFormat.format(
 					Messages.ESWorkspaceProviderImpl_Migration_Of_Project_Failed, curModels.get(0)), e);
 			}
 		}
 	}
 
 	private List<List<URI>> getPhysicalURIsForMigration() {
 		final ESExtensionPoint extensionPoint = new ESExtensionPoint(
 			"org.eclipse.emf.emfstore.client.resourceSetProvider", //$NON-NLS-1$
 			true, new ESPriorityComparator("priority", true)); //$NON-NLS-1$
 
 		final ESResourceSetProvider resourceSetProvider = extensionPoint.getElementWithHighestPriority().getClass(
 			"class", //$NON-NLS-1$
 			ESResourceSetProvider.class);
 
 		final ResourceSet migrationResourceSet = resourceSetProvider.getResourceSet();
 		final Resource resource = migrationResourceSet.createResource(ESClientURIUtil.createWorkspaceURI());
 
 		try {
 			resource.load(ModelUtil.getResourceLoadOptions());
 		} catch (final IOException e) {
 			WorkspaceUtil.logException(Messages.ESWorkspaceProviderImpl_Workspace_Loading_Failed, e);
 		}
 
 		final List<List<URI>> physicalURIs = new ArrayList<List<URI>>();
 
 		final EList<EObject> directContents = resource.getContents();
 		final Workspace workspace = (Workspace) directContents.get(0);
 		for (final ProjectSpace ps : workspace.getProjectSpaces()) {
 			final List<URI> uris = new ArrayList<URI>();
 			final URI projectURI = migrationResourceSet.getURIConverter().normalize(
 				ps.getProject().eResource().getURI());
 			final URI operationsURI = migrationResourceSet.getURIConverter()
 				.normalize(ps.getLocalChangePackage().eResource().getURI());
 			uris.add(projectURI);
 			uris.add(operationsURI);
 			physicalURIs.add(uris);
 		}
 		return physicalURIs;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.observer.ESCheckoutObserver#checkoutDone(org.eclipse.emf.emfstore.client.ESLocalProject)
 	 */
 	public void checkoutDone(ESLocalProject project) {
 		flushCommandStack();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.observer.ESShareObserver#shareDone(org.eclipse.emf.emfstore.client.ESLocalProject)
 	 */
 	public void shareDone(ESLocalProject localProject) {
 		flushCommandStack();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.observer.ESUpdateObserver#inspectChanges(org.eclipse.emf.emfstore.client.ESLocalProject,
 	 *      java.util.List, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public boolean inspectChanges(ESLocalProject project, List<ESChangePackage> changePackages, IProgressMonitor monitor) {
 		return true;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.observer.ESUpdateObserver#updateCompleted(org.eclipse.emf.emfstore.client.ESLocalProject,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void updateCompleted(ESLocalProject project, IProgressMonitor monitor) {
 		flushCommandStack();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.observer.ESCommitObserver#inspectChanges(org.eclipse.emf.emfstore.client.ESLocalProject,
 	 *      org.eclipse.emf.emfstore.server.model.ESChangePackage, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public boolean inspectChanges(ESLocalProject project, ESChangePackage changePackage, IProgressMonitor monitor) {
 		return true;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.observer.ESCommitObserver#commitCompleted(org.eclipse.emf.emfstore.client.ESLocalProject,
 	 *      org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void commitCompleted(ESLocalProject project, ESPrimaryVersionSpec newRevision, IProgressMonitor monitor) {
 		flushCommandStack();
 	}
 }
