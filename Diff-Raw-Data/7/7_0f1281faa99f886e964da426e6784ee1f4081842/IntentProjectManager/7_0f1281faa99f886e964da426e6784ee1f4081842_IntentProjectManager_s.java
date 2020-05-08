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
 package org.eclipse.mylyn.docs.intent.client.ui.ide.projectmanager;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.mylyn.docs.intent.client.compiler.launcher.CompilerCreator;
 import org.eclipse.mylyn.docs.intent.client.compiler.repositoryconnection.CompilerRepositoryClient;
 import org.eclipse.mylyn.docs.intent.client.indexer.IndexerRepositoryClient;
 import org.eclipse.mylyn.docs.intent.client.indexer.launcher.IndexerCreator;
 import org.eclipse.mylyn.docs.intent.client.linkresolver.repository.LinkResolverClient;
 import org.eclipse.mylyn.docs.intent.client.linkresolver.repository.LinkResolverCreator;
 import org.eclipse.mylyn.docs.intent.client.synchronizer.SynchronizerRepositoryClient;
 import org.eclipse.mylyn.docs.intent.client.synchronizer.launcher.SynchronizerCreator;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentDocumentProvider;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditor;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.builder.IntentNature;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.generatedelementlistener.IDEGeneratedElementListener;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.navigator.ProjectExplorerRefresher;
 import org.eclipse.mylyn.docs.intent.collab.common.logger.IIntentLogger.LogType;
 import org.eclipse.mylyn.docs.intent.collab.common.logger.IntentLogger;
 import org.eclipse.mylyn.docs.intent.collab.common.repository.IntentRepositoryManager;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.ReadOnlyException;
 import org.eclipse.mylyn.docs.intent.collab.repository.Repository;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * Handles an Intent Project lifecycle :
  * <ul>
  * <li>Create/Delete the Repository</li>
  * <li>Launch/Stop Intent Clients</li>.
  * </ul>
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public final class IntentProjectManager {
 
 	private CompilerRepositoryClient compilerClient;
 
 	private SynchronizerRepositoryClient synchronizerClient;
 
 	private IndexerRepositoryClient indexerClient;
 
 	private ProjectExplorerRefresher refresher;
 
 	private LinkResolverClient linkResolverClient;
 
 	/**
 	 * The project associated to this IntentProjectManager (must be associated to the Intent nature).
 	 */
 	private IProject project;
 
 	private Repository repository;
 
 	private boolean isConnected;
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param project
 	 *            the project to associate to this IntentProjectManager (must be associated to the Intent
 	 *            nature)
 	 */
 	public IntentProjectManager(IProject project) {
 		this.project = project;
 	}
 
 	/**
 	 * Creates and Launch all the clients needed by the Intent application :
 	 * <ul>
 	 * <li>Compiler</li>
 	 * <li>Synchronizer</li>
 	 * <li>Indexer</li>
 	 * <li>Project Explorer Refresher.</li>
 	 * </ul>
 	 * 
 	 * @throws RepositoryConnectionException
 	 *             if the {@link Repository} cannot be created or accessed
 	 */
 	public synchronized void connect() throws RepositoryConnectionException {
 		try {
 			repository = IntentRepositoryManager.INSTANCE.getRepository(project.getName());
 			IntentLogger.getInstance().log(LogType.LIFECYCLE,
 					"[IntentProjectManager] Connecting to project " + project.getName());
 			if (project.isAccessible() && project.getNature(IntentNature.NATURE_ID) != null) {
				repository.getOrCreateSession();
 
 				// Clients creation (if needed)
 
 				// Compiler
 				if (compilerClient == null) {
 					compilerClient = CompilerCreator.createCompilerClient(repository);
 				}
 
 				// Link resolver
 				if (linkResolverClient == null) {
 					linkResolverClient = LinkResolverCreator.createLinkResolverClient(repository);
 				}
 
 				// Synchronizer
 				if (synchronizerClient == null) {
 					synchronizerClient = SynchronizerCreator.createSynchronizer(repository,
 							new IDEGeneratedElementListener());
 				}
 
 				// Indexer
 				if (indexerClient == null) {
 					indexerClient = IndexerCreator.createIndexer(repository);
 				}
 
 				// Project explorer refresher
 				if (refresher == null) {
 					refresher = ProjectExplorerRefresher.createProjectExplorerRefresher(project);
 				}
 
 				// notifies the clients
 
 				// launch the indexer in order to allow navigation within the document
 				indexerClient.handleChangeNotification(null);
 
 				// launch the compiler to detect eventual existing issues
 				compilerClient.handleChangeNotification(null);
 
 				// launch the link resolver to detect eventual existing issues
 				linkResolverClient.handleChangeNotification(null);
 
 			} else {
 				throw new RepositoryConnectionException("Cannot create Repository on project "
 						+ project.getName());
 			}
 		} catch (CoreException e) {
 			throw new RepositoryConnectionException(e.getMessage());
 		} catch (ReadOnlyException e) {
 			throw new RepositoryConnectionException(e.getMessage());
 		}
 		isConnected = true;
 	}
 
 	/**
 	 * Disconnects the given project by closing the session and the repository.
 	 * 
 	 * @throws RepositoryConnectionException
 	 *             if the {@link Repository} cannot be deleted or accessed
 	 */
 	public synchronized void disconnect() throws RepositoryConnectionException {
 		if (isConnected) {
 			closeRelatedEditors();
 
 			if (compilerClient != null) {
 				compilerClient.dispose();
 				compilerClient = null;
 			}
 			if (linkResolverClient != null) {
 				linkResolverClient.dispose();
 				linkResolverClient = null;
 			}
 			if (synchronizerClient != null) {
 				synchronizerClient.dispose();
 				synchronizerClient = null;
 			}
 			if (indexerClient != null) {
 				indexerClient.dispose();
 				indexerClient = null;
 			}
 			if (refresher != null) {
 				refresher.dispose();
 				refresher = null;
 			}
 
 			repository.closeSession();
 		}
 		IntentRepositoryManager.INSTANCE.deleteRepository(project.getName());
 	}
 
 	/**
 	 * Closes editors.
 	 */
 	private void closeRelatedEditors() {
 		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
 						.getActiveWorkbenchWindow();
 				if (activeWorkbenchWindow != null) {
 					IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
 					IEditorReference[] references = page.getEditorReferences();
 					for (IEditorReference reference : references) {
 						IEditorPart part = reference.getEditor(false);
 						if (part instanceof IntentEditor) {
 							IntentEditor editor = (IntentEditor)part;
 							IntentDocumentProvider provider = (IntentDocumentProvider)editor
 									.getDocumentProvider();
 							if (repository.equals(provider.getRepository())) {
 								editor.close(editor.isSaveOnCloseNeeded()); // this will dispose clients
 							}
 						}
 					}
 				}
 
 			}
 		});
 	}
 }
