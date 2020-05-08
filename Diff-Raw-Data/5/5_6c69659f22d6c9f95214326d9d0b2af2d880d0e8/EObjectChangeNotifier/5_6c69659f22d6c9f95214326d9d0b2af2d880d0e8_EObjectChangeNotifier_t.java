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
 package org.eclipse.emf.emfstore.common.model.util;
 
import java.util.List;
 import java.util.Stack;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.emf.emfstore.common.model.impl.NotifiableIdEObjectCollectionImpl;
 
 /**
  * Notifies a about changes in its containment hierarchy.
  * 
  * @author koegel
  * @author emueller
  */
 public class EObjectChangeNotifier extends EContentAdapter {
 
 	private final NotifiableIdEObjectCollectionImpl collection;
 	private boolean isInitializing;
 	private Stack<Notification> currentNotifications;
 	private Stack<EObject> removedModelElements;
 	private int reentrantCallToAddAdapterCounter;
 	private boolean notificationDisabled;
 
 	/**
 	 * Constructor. Attaches an Adapter to the given {@link Notifier} and forwards notifications to the given
 	 * NotifiableIdEObjectCollection, that reacts appropriately.
 	 * 
 	 * @param notifiableCollection
 	 *            a NotifiableIdEObjectCollection
 	 * @param notifier
 	 *            the {@link Notifier} to listen to
 	 */
 	public EObjectChangeNotifier(NotifiableIdEObjectCollectionImpl notifiableCollection, Notifier notifier) {
 		this.collection = notifiableCollection;
 		isInitializing = true;
 		currentNotifications = new Stack<Notification>();
 		removedModelElements = new Stack<EObject>();
 		notifier.eAdapters().add(this);
 		isInitializing = false;
 		reentrantCallToAddAdapterCounter = 0;
 		notificationDisabled = false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.ecore.util.EContentAdapter#addAdapter(org.eclipse.emf.common.notify.Notifier)
 	 */
 	@Override
 	protected void addAdapter(Notifier notifier) {
 
 		try {
 			reentrantCallToAddAdapterCounter += 1;
 			if (!notifier.eAdapters().contains(this)) {
 				super.addAdapter(notifier);
 			}
 		} finally {
 			reentrantCallToAddAdapterCounter -= 1;
 		}
 		if (reentrantCallToAddAdapterCounter > 0 || currentNotifications.isEmpty()) {
 			// any other than the first call in re-entrant calls to addAdapter
 			// are going to call the project
 			return;
 		}
 
 		Notification currentNotification = currentNotifications.peek();
 
 		if (currentNotification != null && !currentNotification.isTouch() && !isInitializing
 			&& notifier instanceof EObject && !ModelUtil.isIgnoredDatatype((EObject) notifier)) {
 			EObject modelElement = (EObject) notifier;
 			if (!collection.containsInstance(modelElement) && isInCollection(modelElement)) {
 				collection.modelElementAdded(collection, modelElement);
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.ecore.util.EContentAdapter#removeAdapter(org.eclipse.emf.common.notify.Notifier)
 	 */
 	@Override
 	protected void removeAdapter(Notifier notifier) {
 		if (isInitializing || currentNotifications.isEmpty()) {
 			return;
 		}
 
 		Notification currentNotification = currentNotifications.peek();
 
 		if (currentNotification != null && currentNotification.isTouch()) {
 			return;
 		}
 
 		if (currentNotification != null && currentNotification.getFeature() instanceof EReference) {
 			EReference eReference = (EReference) currentNotification.getFeature();
 			if (eReference.isContainment() && eReference.getEOpposite() != null
 				&& !eReference.getEOpposite().isTransient()) {
 				return;
 			}
 		}
 
 		if (notifier instanceof EObject) {
 			EObject modelElement = (EObject) notifier;
 			if (!isInCollection(modelElement)
 				&& (collection.containsInstance(modelElement) || collection.getDeletedModelElementId(modelElement) != null)
 				&& removedModelElements.size() > 0) {
 				removedModelElements.pop();
 				removedModelElements.push(modelElement);
 			}
 
 		}
 	}
 
 	/**
 	 * Checks whether the given {@link EObject} is within the collection.
 	 * 
 	 * @param modelElement
 	 *            the {@link EObject} whose containment should be checked
 	 * @return true, if the {@link EObject} is contained in the collection,
 	 *         false otherwise
 	 */
 	private boolean isInCollection(EObject modelElement) {
 		EObject parent = modelElement.eContainer();
 		if (parent == null) {
 			return false;
 		}
 
 		if (parent == collection) {
 			return true;
 		}
 
 		if (collection.containsInstance(parent)) {
 			return true;
 		}
 
 		return isInCollection(parent);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.ecore.util.EContentAdapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 
 		if (notificationDisabled) {
 			return;
 		}
 
 		currentNotifications.push(notification);
 		// push null to ensure that stack has same size as call stack
 		// will be replaced with an actual removed element by removeAdapter method if there is any
 		removedModelElements.push(null);
 		Object feature = notification.getFeature();
 		Object notifier = notification.getNotifier();
 
 		if (feature instanceof EReference) {
 			EReference eReference = (EReference) feature;
 
 			// Do not create notifications for transient features
 			if (eReference.isTransient()) {
 				return;
 			}
 
 			if (eReference.isContainer()) {
 				handleContainer(notification, eReference);
 			}
 		}
 
 		super.notifyChanged(notification);
 
 		// detect if the notification is about a reference to an object outside of the collection => notify
 		// project
 		if (feature instanceof EReference) {
 			Object newValue = notification.getNewValue();
 			if (newValue instanceof EObject) {
 				EObject newEObject = (EObject) newValue;
 				if (!collection.containsInstance(newEObject)) {
 					collection.addCutElement(newEObject);
 				}
				// FIXME: handle multirefs here
			} else if (newValue instanceof List<?>) {
				// iterate new values
 			}
 		}
 
 		currentNotifications.pop();
 
 		if (!notification.isTouch()
 			&& notifier instanceof EObject
 			&& (collection.getModelElementId((EObject) notifier) != null || collection
 				.getDeletedModelElementId((EObject) notifier) != null)) {
 			collection.notify(notification, collection, (EObject) notifier);
 
 		}
 		EObject removedElement = removedModelElements.pop();
 		if (removedElement != null) {
 			collection.modelElementRemoved(collection, removedElement);
 		}
 	}
 
 	/**
 	 * @param notification
 	 */
 	private void handleContainer(Notification notification, EReference eReference) {
 		if (notification.getEventType() == Notification.SET) {
 			Object newValue = notification.getNewValue();
 			Object oldValue = notification.getOldValue();
 			if (newValue == null && oldValue != null) {
 				removeAdapter((Notifier) notification.getNotifier());
 			}
 		}
 	}
 
 	/**
 	 * @param notificationDisabled
 	 *            the notificationDisabled to set
 	 */
 	public void disableNotifications(boolean notificationDisabled) {
 		this.notificationDisabled = notificationDisabled;
 	}
 
 }
