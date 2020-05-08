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
 package org.eclipse.mylyn.docs.intent.client.ui.test.util;
 
 import com.google.common.collect.Sets;
 
 import java.util.Set;
 
 import junit.framework.AssertionFailedError;
 
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.AbstractRepositoryClient;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification;
 
 /**
  * A {@link org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryClient} used to detect if an event
  * happened or not on the repository.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class RepositoryListenerForTests extends AbstractRepositoryClient {
 
 	/**
 	 * Delay to wait before checking again that an event occurred.
 	 */
	private static final int WAITING_STEP_DELAY = 600;
 
 	/**
 	 * Delay to wait before considering that an expected event never occurred.
 	 */
	private static final long TIME_OUT_DELAY = 20000;
 
 	/**
 	 * The list of modified elements since the last call to
 	 * {@link RepositoryListenerForTests#startRecording()}.
 	 */
 	private Set<EObject> modifiedElements = Sets.newLinkedHashSet();
 
 	/**
 	 * The URIs of modified resources since the last call to
 	 * {@link RepositoryListenerForTests#startRecording()}.
 	 */
 	private Set<URI> modifiedResourcesURI = Sets.newLinkedHashSet();
 
 	/**
 	 * Indicates whether this Repository listener is recording or not.
 	 */
 	private boolean isRecording;
 
 	/**
 	 * Indicates whether the expected event occurred.
 	 */
 	private boolean expectedEventOccured;
 
 	/**
 	 * Removes all registered notifications and start listening to the repository.
 	 */
 	public void startRecording() {
 		isRecording = true;
 		expectedEventOccured = false;
 		modifiedElements.clear();
 		modifiedResourcesURI.clear();
 	}
 
 	/**
 	 * Waits for a modification on the intent resource located at the given path. Returns true if the
 	 * modification occurred, false if it did not after a certain delay.
 	 * 
 	 * @param resourcePath
 	 *            the path of the intent resource which is expected to be modified
 	 * @return true if the modification occurred, false if it did not after a certain delay
 	 */
 	public boolean waitForModificationOn(final String resourcePath) {
 
 		long startTime = System.currentTimeMillis();
 		boolean timeOutDetected = false;
 		try {
 			while (!resourceHasBeenModified(resourcePath) && !timeOutDetected) {
 
 				Thread.sleep(WAITING_STEP_DELAY);
 				timeOutDetected = System.currentTimeMillis() - startTime > TIME_OUT_DELAY;
 
 			}
 			Thread.sleep(WAITING_STEP_DELAY);
 			return !timeOutDetected;
 		} catch (InterruptedException e) {
 			return false;
 		}
 	}
 
 	/**
 	 * Indicates if the repository resource located at the given path has been modified since
 	 * {@link RepositoryListenerForTests#startRecording()} has been called.
 	 * 
 	 * @param resourcePath
 	 *            the relative path of the resource (from the root of the repository)
 	 * @return true if the repository resource located at the given path has been modified since
 	 *         {@link RepositoryListenerForTests#startRecording()} has been called, false otherwise
 	 */
 	private boolean resourceHasBeenModified(String resourcePath) {
 		if (!isRecording) {
 			throw new AssertionFailedError(
 					"The Repository listener has not started recording. Please call the startRecording() method before trying to determine which actions have been determined");
 		}
 		if (!modifiedResourcesURI.isEmpty()) {
 			URI expectedModifiedResourceURI = this.getRepositoryObjectHandler().getRepositoryAdapter()
 					.getResource(resourcePath).getURI();
 			return modifiedResourcesURI.contains(expectedModifiedResourceURI);
 		}
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.impl.AbstractRepositoryClient#handleChangeNotification(org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification)
 	 */
 	@Override
 	public void handleChangeNotification(RepositoryChangeNotification notification) {
 		if (isRecording) {
 			// We registered the modified elements and the URI of their resource
 			modifiedElements.addAll(notification.getRightRoots());
 			for (EObject modifiedElement : notification.getRightRoots()) {
 				modifiedResourcesURI.add(modifiedElement.eResource().getURI());
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.impl.AbstractRepositoryClient#createNotificationJob(org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification)
 	 */
 	@Override
 	protected Job createNotificationJob(RepositoryChangeNotification notification) {
 		// nothing to do, as we override handleChangeNotification this method will not be called
 		return null;
 	}
 
 }
