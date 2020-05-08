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
 package org.eclipse.mylyn.docs.intent.client.ui.ide.generatedelementlistener;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.mylyn.docs.intent.client.synchronizer.listeners.AbstractGeneratedElementListener;
 
 /**
  * Listens all the generated element (must be on the workspace) and warn the synchronizer when one of them
  * change.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class IDEGeneratedElementListener extends AbstractGeneratedElementListener implements IResourceChangeListener {
 
 	private Collection<URI> resourcesToIgnore;
 
 	/**
 	 * IDEGeneratedElementListener constructor.
 	 */
 	public IDEGeneratedElementListener() {
 		super();
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		workspace.addResourceChangeListener(this);
 		resourcesToIgnore = new ArrayList<URI>();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
 	 */
 	public void resourceChanged(IResourceChangeEvent event) {
 		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
 			final IResourceDelta rootDelta = event.getDelta();
 			// We get the delta related to the Repository (if any)
 
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
 
 	/**
 	 * Analyzes the given IResourceDelta in a new thread ; reloads the resources if needed and send
 	 * notification to the registered Session listeners.
 	 * 
 	 * @param repositoryDelta
 	 *            the IResourceDelta to analyse
 	 */
 	private void analyseWorkspaceDelta(IResourceDelta repositoryDelta) {
 
 		// We first create a DeltaVisitor on the repository Path
 		final IDEGeneratedElementListenerDeltaVisitor visitor = new IDEGeneratedElementListenerDeltaVisitor(
 				this.listenedElementsURIs);
 		try {
 			// We visit the given delta using this visitor
 			repositoryDelta.accept(visitor);
 
 			// We get the changed and removed Resources
 			Collection<URI> removedResources = new ArrayList<URI>();
 			Collection<URI> changedResources = new ArrayList<URI>();
 
 			if (!visitor.getRemovedResources().isEmpty()) {
 				removedResources.addAll(visitor.getRemovedResources());
 			}
 
 			for (URI changedResource : visitor.getChangedResources()) {
 				// If the resource is contained in the resourcesToIgnore list, it means
 				// that we should ignore this notification ; however we remove this resource
 				// from this list so that we'll treat the next notifications
 				if (!resourcesToIgnore.contains(changedResource)) {
 					changedResources.add(changedResource);
 					// resourcesToIgnore.add(changedResource);
 				} else {
 					resourcesToIgnore.remove(changedResource);
 				}
 			}
 
 			// Finally, we treat each removed or changed resource.
			treatChangedResources(changedResources);
 
 		} catch (CoreException e) {
 			// TODO define a standard reaction to this exception :
 			// - relaunch the session
 			// - try to visit the delta again
 			// - do nothing
 		}
 	}
 
 	/**
 	 * Sends a notification to the synchronizer for each detected changedResource.
 	 * 
 	 * @param changedResources
 	 *            the list of listened resources that have changed
 	 */
	private void treatChangedResources(Collection<URI> changedResources) {
 		// TODO construct a proper change Notification
 		if (!changedResources.isEmpty()) {
 			this.synchronizer.handleChangeNotification(null);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.client.synchronizer.listeners.GeneratedElementListener#dispose()
 	 */
 	public void dispose() {
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		workspace.removeResourceChangeListener(this);
 	}
 }
