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
 package org.eclipse.mylyn.docs.intent.client.ui.ide.builder;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.launcher.IntentProjectManager;
 import org.eclipse.mylyn.docs.intent.client.ui.logger.IntentUiLogger;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
 
 /**
  * A {@link IResourceChangeListener} that reacts to the creation or opening of Intent projects by creating
  * Repository and launching clients.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class IntentProjectListener implements IResourceChangeListener {
 
 	/**
 	 * Default constructor.
 	 */
 	public IntentProjectListener() {
 		// We first treat the existing projects
		treatExistingIntentrojects();
 	}
 
 	/**
 	 * Treats all IProjects already opened when the plugin get activated, if they are associated to the intent
 	 * Nature.
 	 */
	public void treatExistingIntentrojects() {
 		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 		for (IProject project : allProjects) {
 			try {
 				if (project.isAccessible() && project.hasNature(IntentNature.NATURE_ID)) {
 					handleOpenedProject(project);
 				}
 			} catch (CoreException e) {
 				IntentUiLogger.logError(e);
 				try {
 					// Close the project : Intent cannot work correctly
 					project.close(new NullProgressMonitor());
 				} catch (CoreException e1) {
 					IntentUiLogger.logError(e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
 	 */
 	public void resourceChanged(IResourceChangeEvent event) {
 		if (event.getType() == IResourceChangeEvent.PRE_CLOSE
 				|| event.getType() == IResourceChangeEvent.PRE_DELETE) {
 			IResource resource = event.getResource();
 			try {
 				// TODO check if there is a repository associated to this project, even if not accessible
 				if (resource instanceof IProject && resource.isAccessible()
 						&& ((IProject)resource).hasNature(IntentNature.NATURE_ID)) {
 					handleClosedProject((IProject)resource);
 				}
 			} catch (CoreException e) {
 				IntentUiLogger.logError(e);
 			}
 		} else {
 			// We want to be notified AFTER any changed that occurred
 			if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
 				final IResourceDelta rootDelta = event.getDelta();
 				// If any resource of the repository has changed
 				if (rootDelta != null) {
 					// We launch the analysis of the delta in a new thread
 					Runnable runnable = new Runnable() {
 						public void run() {
 							analyseWorkspaceDelta(rootDelta);
 						}
 					};
 					Thread t = new Thread(runnable);
 					t.start();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Analyzes the given {@link IResourceDelta}.
 	 * 
 	 * @param repositoryDelta
 	 *            the {@link IResourceDelta} to analyze
 	 */
 	protected void analyseWorkspaceDelta(IResourceDelta repositoryDelta) {
 		try {
 			// Step 1 : We visit the given delta using a IntentBuilderDeltaVisitor visitor
 			final IntentBuilderDeltaVisitor visitor = new IntentBuilderDeltaVisitor();
 			repositoryDelta.accept(visitor);
 
 			// Step 2 : if any project has been opened, we handle this creation
 			for (IProject project : visitor.getOpenedProjects()) {
 				handleOpenedProject(project);
 			}
 
 			// Step 3 : if any project has been closed, we handle this creation
 			for (IProject project : visitor.getClosedProjects()) {
 				handleClosedProject(project);
 			}
 
 		} catch (CoreException e) {
 			IntentUiLogger.logError(e);
 		}
 
 	}
 
 	/**
 	 * Handles the creation or opening of Intent projects by launching all clients.
 	 * 
 	 * @param project
 	 *            the created or opened project to handle
 	 */
 	public void handleOpenedProject(IProject project) {
 		IntentProjectManager projectManager = IntentProjectManager.getInstance(project, true);
 		try {
 			projectManager.connect();
 		} catch (RepositoryConnectionException e) {
 			IntentUiLogger.logError(e);
 		}
 	}
 
 	/**
 	 * Handles the deletion or closing of Intent projects by stopping all clients and closing repository.
 	 * 
 	 * @param project
 	 *            the deleted or closed project to handle
 	 */
 	public void handleClosedProject(IProject project) {
 		IntentProjectManager projectManager = IntentProjectManager.getInstance(project, false);
 		if (projectManager != null) { // should not happen
 			try {
 				projectManager.disconnect();
 			} catch (RepositoryConnectionException e) {
 				IntentUiLogger.logError(e);
 			}
 		}
 	}
 
 }
