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
 package org.eclipse.mylyn.docs.intent.collab.cdo.adapters;
 
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.eclipse.emf.cdo.common.revision.CDORevisionKey;
 import org.eclipse.emf.cdo.common.revision.delta.CDOFeatureDelta;
 import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
 import org.eclipse.emf.cdo.session.CDOSessionInvalidationEvent;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotificationFactoryHolder;
 import org.eclipse.net4j.util.event.IEvent;
 import org.eclipse.net4j.util.event.IListener;
 
 /**
  * Listens for any change that occur on an instance of the given types, an notify the associated
  * TypeNotificator.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class CDOTypeListener implements IListener {
 
 	/**
 	 * The type notificator to notify if changes are detected.
 	 */
 	private final Notificator listeningTypeNotificator;
 
 	/**
 	 * List of the types to listen.
 	 */
 	private final Set<EStructuralFeature> listennedTypes;
 
 	/**
 	 * CDOTypeListener constructor.
 	 * 
 	 * @param typeNotificator
 	 *            The TypeNotificator to notify if changes are detected.
 	 * @param types
 	 *            List of the types to listen
 	 */
 	public CDOTypeListener(Notificator typeNotificator, Set<EStructuralFeature> types) {
 		this.listeningTypeNotificator = typeNotificator;
 		this.listennedTypes = new LinkedHashSet<EStructuralFeature>();
 		this.listennedTypes.addAll(types);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.net4j.util.event.IListener#notifyEvent(org.eclipse.net4j.util.event.IEvent)
 	 */
 	public void notifyEvent(IEvent event) {
 
 		// For each CDOSessionInvalidationEvent sent by the session whenever a "invalidate" message is
 		// received (see CDO documentation).
 		if (event instanceof CDOSessionInvalidationEvent) {
 			CDOSessionInvalidationEvent invalidationEvent = (CDOSessionInvalidationEvent)event;
 			boolean changesDetected = false;
 
 			// We determine if this event concerns a type it listens.
 			for (CDORevisionKey key : invalidationEvent.getChangedObjects()) {
 				if (changesDetected) {
 					break;
 				}
 				CDORevisionDelta revisionDelta = (CDORevisionDelta)key;
 
 				for (CDOFeatureDelta delta : revisionDelta.getFeatureDeltas()) {
 
					if (isListennedTypeModification(delta.getFeature())) {
 						changesDetected = true;
 						break;
 					}
 				}
 			}
 
 			// If relevant changes have been detected, we send a notification to the typeNotificator
 			if (changesDetected) {
 				RepositoryChangeNotification newNotification = RepositoryChangeNotificationFactoryHolder
 						.getChangeNotificationFactory().createRepositoryChangeNotification(invalidationEvent);
 
 				this.listeningTypeNotificator.notifyHandlers(newNotification);
 			}
 
 		}
 	}
 
 	/**
 	 * Returns true if the given feature matches any listened types of this typeListener.
 	 * 
 	 * @param feature
 	 *            the feature to test.
 	 * @return true if the given feature matches any listened types of this typeListener, false otherwise.
 	 */
	private boolean isListennedTypeModification(EStructuralFeature feature) {
 		boolean isListennedType = false;
 
 		for (Iterator<EStructuralFeature> iterator = listennedTypes.iterator(); iterator.hasNext()
 				&& !isListennedType;) {
 			ENamedElement listennedtype = iterator.next();
 			if (listennedtype instanceof EStructuralFeature) {
 
 				isListennedType = listennedtype.equals(feature);
 			}
 			if (listennedtype instanceof EClass) {
 
 				isListennedType = ((EClass)listennedtype).getEAllStructuralFeatures().contains(feature);
 			}
 
 		}
 
 		return isListennedType;
 	}
 }
