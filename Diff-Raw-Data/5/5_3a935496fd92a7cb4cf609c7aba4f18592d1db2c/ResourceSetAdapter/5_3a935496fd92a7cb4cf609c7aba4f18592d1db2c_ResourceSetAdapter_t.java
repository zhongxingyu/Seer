 /*******************************************************************************
  * Copyright (c) 2010 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.api.notify;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class ResourceSetAdapter extends EContentAdapter {
 
 	/**
 	 * The eClassifiers listeners.
 	 */
 	private Map<EClassifier, List<PropertiesEditingSemanticListener>> eClassifierListeners;
 
 	/**
 	 * The features listeners.
 	 */
 	private Map<EStructuralFeature, List<PropertiesEditingSemanticListener>> eStructuralFeatureListeners;
 
 	/**
 	 * The resource set we listen to.
 	 */
 	private ResourceSet resourceSet;
 
 	/**
 	 * Constructor.
 	 */
 	public ResourceSetAdapter(ResourceSet resourceSet) {
 		super();
 		this.resourceSet = resourceSet;
 		eClassifierListeners = new HashMap<EClassifier, List<PropertiesEditingSemanticListener>>();
 		eStructuralFeatureListeners = new HashMap<EStructuralFeature, List<PropertiesEditingSemanticListener>>();
 	}
 
 	/**
 	 * Activates the resource set adapter.
 	 */
 	public void activate() {
 		resourceSet.eAdapters().add(this);
 	}
 
 
 	/**
 	 * Removes the current adapter from the resource set adapters.
 	 */
 	public void deactivate() {
 		resourceSet.eAdapters().remove(this);
 	}
 
 	/**
 	 * Adds the given listener to the registered listeners.
 	 * 
 	 * @param listener
 	 *            the listener to remove
 	 */
 	public void addEditingSemanticListener(PropertiesEditingSemanticListener listener) {
 		for (NotificationFilter filter : listener.getFilters()) {
 			if (filter instanceof EStructuralFeatureNotificationFilter) {
 				for (EStructuralFeature feature : ((EStructuralFeatureNotificationFilter)filter)
 						.getFeatures()) {
 					addEditingSemanticListener(feature, listener);
 				}
 			} else if (filter instanceof EClassifierNotificationFilter) {
 				for (EClassifier eClassifier : ((EClassifierNotificationFilter)filter).getEClassifiers()) {
 					addEditingSemanticListener(eClassifier, listener);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Adds the given listener to the registered listeners.
 	 * 
 	 * @param feature
 	 *            the feature key
 	 * @param listener
 	 *            the listener to remove
 	 */
 	private void addEditingSemanticListener(EStructuralFeature feature,
 			PropertiesEditingSemanticListener listener) {
 		List<PropertiesEditingSemanticListener> listeners = eStructuralFeatureListeners.get(feature);
 		if (listeners == null) {
 			listeners = new ArrayList<PropertiesEditingSemanticListener>();
 			eStructuralFeatureListeners.put(feature, listeners);
 		}
 		listeners.add(listener);
 	}
 
 	/**
 	 * Adds the given listener to the registered listeners.
 	 * 
 	 * @param eClassifier
 	 *            the eClassifier key
 	 * @param listener
 	 *            the listener to remove
 	 */
 	private void addEditingSemanticListener(EClassifier eClassifier,
 			PropertiesEditingSemanticListener listener) {
 		List<PropertiesEditingSemanticListener> listeners = eClassifierListeners.get(eClassifier);
 		if (listeners == null) {
 			listeners = new ArrayList<PropertiesEditingSemanticListener>();
 			eClassifierListeners.put(eClassifier, listeners);
 		}
 		listeners.add(listener);
 	}
 
 	/**
 	 * Removes the given listener from the registered listeners.
 	 * 
 	 * @param listener
 	 *            the listener to remove
 	 */
 	public void removeEditingSemanticListener(PropertiesEditingSemanticListener listener) {
 		for (NotificationFilter filter : listener.getFilters()) {
 			if (filter instanceof EStructuralFeatureNotificationFilter) {
 				for (EStructuralFeature feature : ((EStructuralFeatureNotificationFilter)filter)
 						.getFeatures()) {
 					removeEditingSemanticListener(feature, listener);
 				}
 			} else if (filter instanceof EClassifierNotificationFilter) {
 				for (EClassifier eClassifier : ((EClassifierNotificationFilter)filter).getEClassifiers()) {
 					removeEditingSemanticListener(eClassifier, listener);
 				}
 			}
 		}
 
 		// if there are no listeners left we remove the adapter
 		if (eClassifierListeners.size() + eStructuralFeatureListeners.size() == 0)
 			deactivate();
 	}
 	/**
 	 * Removes the given listener from the registered listeners.
 	 * 
 	 * @param feature
 	 *            the feature key
 	 * @param listener
 	 *            the listener to remove
 	 */
 	private void removeEditingSemanticListener(EStructuralFeature feature,
 			PropertiesEditingSemanticListener listener) {
 		List<PropertiesEditingSemanticListener> listeners = eStructuralFeatureListeners.get(feature);
 		if (listeners != null) {
 			listeners.remove(listener);
 			if (listeners.isEmpty()) {
 				eStructuralFeatureListeners.remove(feature);
 			}
 		}
 	}
 
 	/**
 	 * Removes the given listener from the registered listeners.
 	 * 
 	 * @param eClassifier
 	 *            the classifier key
 	 * @param listener
 	 *            the listener to remove
 	 */
 	private void removeEditingSemanticListener(EClassifier eClassifier,
 			PropertiesEditingSemanticListener listener) {
 		List<PropertiesEditingSemanticListener> listeners = eClassifierListeners.get(eClassifier);
 		if (listeners != null) {
 			listeners.remove(listener);
 			if (listeners.isEmpty()) {
 				eClassifierListeners.remove(eClassifier);
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.ecore.util.EContentAdapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
 	 */
 	public void notifyChanged(Notification notification) {
 		super.notifyChanged(notification);
 		if (notification.getNotifier() instanceof EObject) {
 			EObject notifier = (EObject)notification.getNotifier();
 			EClass eClass = notifier.eClass();
 			if (eClassifierListeners.get(eClass) != null) {
 				List<PropertiesEditingSemanticListener> listeners = eClassifierListeners.get(eClass);
				for (PropertiesEditingSemanticListener listener : new ArrayList<PropertiesEditingSemanticListener>(listeners)) {
 					listener.notifyChanged(notification);
 				}
 			}
 		}
 		if (notification.getFeature() instanceof EStructuralFeature) {
 			EStructuralFeature feature = (EStructuralFeature)notification.getFeature();
 			if (eStructuralFeatureListeners.get(feature) != null) {
 				List<PropertiesEditingSemanticListener> listeners = eStructuralFeatureListeners.get(feature);
				for (PropertiesEditingSemanticListener listener : new ArrayList<PropertiesEditingSemanticListener>(listeners)) {
 					listener.notifyChanged(notification);
 				}
 			}
 		}
 	}
 
 }
