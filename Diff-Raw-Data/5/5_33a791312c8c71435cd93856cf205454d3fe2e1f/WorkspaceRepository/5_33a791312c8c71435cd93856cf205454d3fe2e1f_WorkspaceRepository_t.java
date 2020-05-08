 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.collab.ide.repository;
 
 import com.google.common.base.Predicate;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage.Registry;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.transaction.impl.InternalTransactionalEditingDomain;
 import org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryClient;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryStructurer;
 import org.eclipse.mylyn.docs.intent.collab.ide.adapters.WorkspaceAdapter;
 import org.eclipse.mylyn.docs.intent.collab.repository.Repository;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
 
 /**
  * Representation of a Workspace as a repository.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class WorkspaceRepository implements Repository {
 
 	/**
 	 * The default extension for any element stored on the repository.
 	 */
 	private static final String WORKSPACE_RESOURCE_EXTENSION = "repomodel";
 
 	/**
 	 * Represents this repository configuration.
 	 */
 	private WorkspaceConfig workspaceConfig;
 
 	/**
 	 * Session that will notify any listening entities about changes on Workspace resources corresponding to
 	 * repository resources.
 	 */
 	private WorkspaceSession session;
 
 	/**
 	 * Indicates if the repository resource set has been initialized.
 	 */
 	private boolean isResourceSetLoaded;
 
 	/**
 	 * The repository editing domain.
 	 */
 	private TransactionalEditingDomain editingDomain;
 
 	/**
 	 * The repository structurer.
 	 */
 	private RepositoryStructurer repositoryStructurer;
 
 	private EClass[] unloadableTypes;
 
 	/**
 	 * WorkspaceRepository constructor.
 	 * 
 	 * @param workspaceConfig
 	 *            this repository configuration
 	 * @param unloadableTypes
 	 *            the list of types which cannot be unloaded
 	 */
 	public WorkspaceRepository(WorkspaceConfig workspaceConfig, EClass... unloadableTypes) {
 		this.unloadableTypes = unloadableTypes;
 		this.workspaceConfig = workspaceConfig;
 		this.editingDomain = TransactionalEditingDomain.Factory.INSTANCE.createEditingDomain();
 		isResourceSetLoaded = false;
 	}
 
 	/**
 	 * Initializes the resource set using the repository content ; if the repository content is empty, creates
 	 * all the declared indexes.
 	 * 
 	 * @throws CoreException
 	 *             it the resourceSet cannot be initialized
 	 */
 	private void initializeResourceSet() throws CoreException {
 
 		// We first get the project on which the repository is defined
 		IProject project = workspaceConfig.getProject();
 		if (!project.exists()) {
 			project.create(null);
 		}
 		if (!project.isOpen()) {
 			project.open(null);
 		}
 		IFolder folder = project.getFolder(workspaceConfig.getRepositoryRelativePath());
 		// We created a WorkspaceRepositoryLoader using the indexPathList
 		WorkspaceRepositoryLoader loader = new WorkspaceRepositoryLoader(this, this.getWorkspaceConfig()
 				.getIndexesPathList());
 		// If the repository folder exists
 		if (folder.exists()) {
 			// We load the resourceSet
 			loader.loadResourceSet();
 		} else {
 			// If the repository folder doesn't exist
 			// We create it
 			folder.create(IResource.NONE, true, null);
 			// And use the RepositoryLoader to initialize the resource set with empty content
 			// loader.loadResourceSet();
 			// loader.createResourceSet();
 
 		}
 		isResourceSetLoaded = true;
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.repository.Repository#register(org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryClient)
 	 */
 	public void register(RepositoryClient client) {
 		// No need to register
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.repository.Repository#unregister(org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryClient)
 	 */
 	public void unregister(RepositoryClient client) {
 		// No need to unregister
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.repository.Repository#getOrCreateSession()
 	 */
	public synchronized Object getOrCreateSession() throws RepositoryConnectionException {
 
 		// We first initialize the resource set if needed
 		if (!isResourceSetLoaded) {
 			try {
 				initializeResourceSet();
 			} catch (CoreException e) {
 				throw new RepositoryConnectionException(e.getMessage());
 			}
 		}
 		if (this.session == null) {
 			// Creation of a Workspace session that will send notifications when the workspace's resources
 			// corresponding to repository resources change
 			this.session = new WorkspaceSession(this);
 			IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			workspace.addResourceChangeListener(session);
 		}
 		return this.session;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.repository.Repository#closeSession()
 	 */
	public synchronized void closeSession() throws RepositoryConnectionException {
 		if (this.session != null) {
 			ResourcesPlugin.getWorkspace().removeResourceChangeListener(session);
 			this.session.close();
 			this.session = null;
 		}
 
 		// If a transaction is being executed, we abort it
 		if (((InternalTransactionalEditingDomain)this.editingDomain).getActiveTransaction() != null) {
 			((InternalTransactionalEditingDomain)this.editingDomain).getActiveTransaction().abort(
 					Status.CANCEL_STATUS);
 		}
 		this.editingDomain.dispose();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.repository.Repository#getPackageRegistry()
 	 */
 	public Registry getPackageRegistry() throws RepositoryConnectionException {
 		// Return the resourceset's package registry
 		return getResourceSet().getPackageRegistry();
 	}
 
 	// ---- SPECIFIC BEHAVIORS
 	/**
 	 * Indicates if the given path represents a location in the repository.
 	 * 
 	 * @param path
 	 *            the path to study
 	 * @return true if the given path represents a location in the repository, false otherwise
 	 */
 	public boolean isInRepositoryPath(String path) {
 		return path.startsWith(this.workspaceConfig.getRepositoryRelativePath());
 	}
 
 	/**
 	 * Indicates if the given resource is included in the given path.
 	 * 
 	 * @param path
 	 *            is the path
 	 * @param resource
 	 *            the resource to determine if it's included in the given path
 	 * @return true if the resource's associated path starts with or is equals to the given path, false
 	 *         otherwise
 	 */
 	public boolean isIncludedInPath(String path, Resource resource) {
 		// We first get the complete URI of the given path
 		URI uriPath = getURIMatchingPath(path);
 		// We compare the two URI
 		return resource.getURI().toString().startsWith(uriPath.toString());
 	}
 
 	/**
 	 * Returns the Repository URI corresponding to the given path.
 	 * <p>
 	 * The complete path is calculated by prefixing the given path by this repository path and postfixing it
 	 * by the default file extension.
 	 * </p>
 	 * 
 	 * @param path
 	 *            is the path
 	 * @return the Repository URI corresponding to the given path
 	 */
 	public URI getURIMatchingPath(String path) {
 		String completePath = this.getWorkspaceConfig().getRepositoryAbsolutePath() + path;
 		// If the path don't ends with '/' (i.e isn't a folder), we add the file extension
 		if (!path.endsWith("/")) {
 			completePath += "." + WORKSPACE_RESOURCE_EXTENSION;
 		}
 		completePath = completePath.trim();
 		URI uri = URI.createPlatformResourceURI(completePath, false);
 		return uri;
 	}
 
 	/**
 	 * Returns the EMF ResourceSet of this Workspace repository.
 	 * <p>
 	 * It will handle resource creation, package registry management...
 	 * </p>
 	 * 
 	 * @return the resourceSet the EMF ResourceSet of this Workspace repository
 	 */
 	public ResourceSet getResourceSet() {
 		return editingDomain.getResourceSet();
 	}
 
 	/**
 	 * Returns this repository configuration.
 	 * 
 	 * @return this repository configuration
 	 */
 	public WorkspaceConfig getWorkspaceConfig() {
 		return workspaceConfig;
 	}
 
 	/**
 	 * Returns the default extension for any element stored on the repository.
 	 * 
 	 * @return the default extension for any element stored on the repository
 	 */
 	public static String getWorkspaceResourceExtension() {
 		return WORKSPACE_RESOURCE_EXTENSION;
 	}
 
 	public TransactionalEditingDomain getEditingDomain() {
 		return editingDomain;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.repository.Repository#setRepositoryStructurer(org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryStructurer)
 	 */
 	public void setRepositoryStructurer(RepositoryStructurer structurer) {
 		this.repositoryStructurer = structurer;
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.repository.Repository#createRepositoryAdapter()
 	 */
 	public RepositoryAdapter createRepositoryAdapter() {
 		WorkspaceAdapter workspaceAdapter = new WorkspaceAdapter(this);
 		if (this.repositoryStructurer != null) {
 			workspaceAdapter.attachRepositoryStructurer(this.repositoryStructurer);
 		}
 		// We set a new unloadable Resource Predicate
 		workspaceAdapter.setUnloadableResourcePredicate(new Predicate<Resource>() {
 
 			public boolean apply(Resource resource) {
 				// The Intent index should never be unloaded
 				if (!resource.getContents().isEmpty()) {
 					boolean res = false;
 					EObject root = resource.getContents().iterator().next();
 					for (EClass unloadableType : unloadableTypes) {
 						if (root.eClass().equals(unloadableType)) {
 							res = true;
 						}
 					}
 					return res;
 				}
 				return false;
 			}
 		});
 		return workspaceAdapter;
 	}
 
 }
