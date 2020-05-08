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
 package org.eclipse.mylyn.docs.intent.collab.ide.notification;
 
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotificationFactoryHolder;
 
 /**
  * Listens for any change that occur on an instance of the given types, an notify the associated
  * TypeNotificator.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class WorkspaceTypeListener {
 
 	/**
 	 * The type notificator to notify if changes are detected.
 	 */
 	private final Notificator typeNotificator;
 
 	/**
 	 * List of the types to listen.
 	 */
 	private final Set<EStructuralFeature> listenedTypes;
 
 	/**
 	 * WorkspaceTypeListener constructor.
 	 * 
 	 * @param typeNotificator
 	 *            The type notificator to notify if changes are detected
 	 * @param typesToListen
 	 *            List of the types to listen
 	 */
 	public WorkspaceTypeListener(Notificator typeNotificator, Set<EStructuralFeature> typesToListen) {
 		this.typeNotificator = typeNotificator;
 		this.listenedTypes = new LinkedHashSet<EStructuralFeature>();
 		this.listenedTypes.addAll(typesToListen);
 	}
 
 	/**
 	 * This method is called by the Workspace Session any time a resource located on the repository changes ;
 	 * must notify the notificator if the resource contained one of the listened types.
 	 * 
 	 * @param resource
 	 *            the resource that has changed
 	 */
 	public void notifyResourceChanged(Resource resource) {
 		boolean changesDetected = false;
 
 		// We determine if any of the roots contained in the resource is a listened type
 		Iterator<EObject> contentIterator = resource.getContents().iterator();
 		while (contentIterator.hasNext() && !changesDetected) {
 			EObject nextRoot = contentIterator.next();
 
 			Iterator<EStructuralFeature> rootFeaturesIterator = nextRoot.eClass().getEAllStructuralFeatures()
 					.iterator();
 			while (rootFeaturesIterator.hasNext() && !changesDetected) {
 				EStructuralFeature next = rootFeaturesIterator.next();
				changesDetected = isListennedTypeModification(next);
 			}
 		}
 
 		// If this type Listener must notify its associated notificator
 		if (changesDetected) {
 			RepositoryChangeNotification newNotification = RepositoryChangeNotificationFactoryHolder
 					.getChangeNotificationFactory().createRepositoryChangeNotification(resource);
 
 			this.typeNotificator.notifyHandlers(newNotification);
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
 		for (Iterator<EStructuralFeature> iterator = listenedTypes.iterator(); iterator.hasNext()
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
